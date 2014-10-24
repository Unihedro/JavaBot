package com.gmail.inverseconduit.chat;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import com.gistlabs.mechanize.Resource;
import com.gistlabs.mechanize.document.html.HtmlDocument;
import com.gistlabs.mechanize.document.html.HtmlElement;
import com.gistlabs.mechanize.document.html.form.Form;
import com.gistlabs.mechanize.document.json.JsonDocument;
import com.gistlabs.mechanize.impl.MechanizeAgent;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.bot.AbstractBot;
import com.gmail.inverseconduit.bot.BotConfig;

public class MessageRelay {
	private final static Logger LOGGER = Logger.getLogger(MessageRelay.class
			.getName());

	private static final int MAX_MESSAGE_LENGTH = 500;
	private static final EnumMap<SESite, Map<Integer, String>> chatToFkey = new EnumMap<>(
			SESite.class);
	static {
		Arrays.stream(SESite.values()).forEach(site -> {
			chatToFkey.put(site, new HashMap<>());
		});
	}
	
	private final ScheduledExecutorService threadPool = Executors
			.newSingleThreadScheduledExecutor();

	private boolean loggedIn = true;
	private final JsonChatConnection jsonChatConnection;
	private final Set<AbstractBot> listeningBots = new HashSet<>();

	private final MechanizeAgent agent;

	@SuppressWarnings("deprecation")
	public MessageRelay(AbstractBot bot) {
		this.agent = new MechanizeAgent();
		
		this.agent.getClient().setRedirectStrategy(new RedirectStrategy() {
			@Override
			public boolean isRedirected(final HttpRequest httpRequest,
					final HttpResponse httpResponse,
					final HttpContext httpContext) throws ProtocolException {
				return (httpResponse.getStatusLine().getStatusCode() == 302);
			}

			@Override
			public HttpUriRequest getRedirect(final HttpRequest httpRequest,
					final HttpResponse httpResponse,
					final HttpContext httpContext) throws ProtocolException {
				httpRequest.getRequestLine().getProtocolVersion().getProtocol();
				String host = httpRequest.getFirstHeader("Host").getValue();
				String location = httpResponse.getFirstHeader("Location")
						.getValue();
				String protocol = (httpRequest.getFirstHeader("Host")
						.getValue().equals("openid.stackexchange.com")) ? "https"
						: "http";
				if (location.startsWith("http://")
						|| location.startsWith("https://")) {
					LOGGER.info("Redirecting to " + location);
					return new HttpGet(location);
				} else {
					LOGGER.info("Redirecting to " + protocol + "://" + host
							+ location);
					return new HttpGet(protocol + "://" + host + location);
				}
			}
		});
		listeningBots.add(bot);

		jsonChatConnection = new JsonChatConnection(agent, this);
		jsonChatConnection.setEnabled(true);
		threadPool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				monitorRooms();
			}
		}, 10, 5, TimeUnit.SECONDS);
	}

	private void monitorRooms() {
		try {
			Arrays.stream(SESite.values()).forEach(
					site -> {
						chatToFkey
								.get(site)
								.keySet()
								.forEach(
										chatId -> {
											jsonChatConnection.queryEventsFor(
													site,
													chatId,
													chatToFkey.get(site).get(
															chatId));
										});
					});
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			LOGGER.severe("Exception in query-thread: " + ex);
			LOGGER.severe("Caused By: " + ex.getCause());
		}
	}

	public boolean loginWithEmailAndPass(final SESite site, final String email,
			final String password) {
		HtmlDocument rootLoginPage = agent.get(site.getLoginUrl());
		Form loginForm = rootLoginPage.form("se-login-form");

		loginForm.get("password").setValue(BotConfig.PASSWORD);
		loginForm.get("email").setValue(BotConfig.LOGIN_EMAIL);

		HtmlDocument response = loginForm.submit();
		if (response.asString().contains("class=\"profile-me")) {
			LOGGER.info("Login to site successful");
			return true;
		} else {
			LOGGER.severe("Login to site failed");
			return false;
		}
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public boolean joinChat(SESite site, int chatId) {
		if (!loggedIn) {
			LOGGER.warning("Not logged in. Cannot join chat.");
			return false;
		}
		if (chatToFkey.containsKey(site)
				&& chatToFkey.get(site).containsKey(chatId)) {
			LOGGER.warning("Already in that room.");
			return false;
		}

		Resource chatPage = agent.get(site.urlToRoom(chatId));
		if (!(chatPage instanceof HtmlDocument)) {
			throw new RuntimeException("Chat Page response was not Html!!!");
		}
		if (chatPage.asString().contains("<h2>Object moved to")) {
			LOGGER.info("Chat-page moved");
			chatPage = agent.get(((HtmlDocument) chatPage).find("a")
					.getAttribute("href"));
		}

		String fkey;
		try {
			fkey = ((HtmlDocument) chatPage).find("input[name=fkey]")
					.getValue();
		} catch (NullPointerException ex) {
			LOGGER.severe("nullpointer when parsing chatpage for fkey");
			LOGGER.severe(chatPage.asString());
			throw new RuntimeException(ex);
		}
		addChatPage(site, chatId, fkey);

		LOGGER.info("Joined room.");
		sendMessage(site, chatId, "~ JavaBot, at your service.");
		return true;
	}

	private void addChatPage(SESite site, int id, String fkey) {
		Map<Integer, String> siteMap = chatToFkey.get(site);
		if (siteMap == null) {
			siteMap = new HashMap<>();
		}
		siteMap.put(id, fkey);
		chatToFkey.put(site, siteMap);
	}

	protected void handleChatEvents(JsonChatEvents events) {
		LOGGER.finest("Handling events from chat.");
		events.getEvents()
				.stream()
				.filter(e -> e.getEvent_type() == ChatEventType.CHAT_MESSAGE)
				.map(e -> {
					return new ChatMessage(events.getSite(), e.getRoom_id(), e
							.getRoom_name(), e.getUser_name(), e.getUser_id(),
							e.getContent());
				}).forEach(m -> {
					LOGGER.finest("Enqueueing message: " + m);

					listeningBots.forEach(x -> {
						try {
							x.enqueueMessage(m);
						} catch (InterruptedException e1) {
							LOGGER.severe("Interrupted");
							e1.printStackTrace(System.err);
						}
					});
				});

	}

	public synchronized boolean sendMessage(final SESite site,
			final int chatId, final String message) {
		Objects.requireNonNull(message, "message");

		String fkey = chatToFkey.get(site).get(chatId);

		Map<String, String> parameters = new HashMap<>();
		parameters.put("text", message); // TODO: check max-length of message!
		parameters.put("fkey", fkey);

		try {
			Resource response = agent.post(String.format(
					"http://chat.%s.com/chats/%d/messages/new",
					site.getDomain(), chatId), parameters);
			LOGGER.info("title: " + response.getTitle() + "\ncontent-type: "
					+ response.getContentType());
			if (response instanceof JsonDocument) {
				// success
				return true;
			} else if (response instanceof HtmlDocument) {
				// failure
				HtmlDocument htmlDocument = (HtmlDocument) response;
				HtmlElement body = htmlDocument.find("body");
				if (body.getInnerHtml().contains(
						"You can perform this action again in")) {
					int timing = Integer.parseInt(body
							.getInnerHtml()
							.replaceAll("You can perform this action again in",
									"").replaceAll("seconds", "").trim());
					//BACK OFF and retry
				} else if (body.getInnerHtml().contains("login")) {
					LOGGER.severe("Probably not logged in!");
					LOGGER.info(body.getInnerHtml());
				}
				return false;
			} else {
				// even harder failure
				throw new IllegalStateException(
						"unexpected response, response.getClass() = "
								+ response.getClass());
			}
		} catch (UnsupportedEncodingException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
