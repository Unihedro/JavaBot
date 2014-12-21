package com.gmail.inverseconduit.timer;

import static com.gmail.inverseconduit.AppContext.INSTANCE;

import java.util.function.Predicate;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;

public final class TimerCommands {

	private static final String DEFAULT_MESSAGE = "Time's up!";

	public static CommandHandle timerCommand(ChatInterface chatInterface) {
		final String trigger = INSTANCE.get(BotConfig.class).getTrigger();
		Predicate<String> commandMatcher = c -> c.startsWith(trigger + "timer");
		CommandHandle handle = new CommandHandle.Builder("timer",
				commandMatcher, a -> TimerCommands.processChatMessage(a,
						chatInterface))
				.setHelpText(
						"Schedule Timers for your personal use. Syntax: timer delay(in minutes) (optional message). You can also abort timers by calling timer abort timernumber")
				.setInfoText(
						"Schedule Timers for your personal use. Syntax: timer delay(in minutes) (optional message). You can also abort timers by calling timer abort timernumber")
				.build();

		return handle;
	}

	private static String processChatMessage(ChatMessage message,
			ChatInterface chatInterface) {
		String[] arguments = message.getMessage().split(" ");
		if (2 > arguments.length || 3 < arguments.length) {
			// TODO: Clean this up after merging 23
			chatInterface
					.sendMessage(
							SeChatDescriptor.buildSeChatDescriptorFrom(message),
							"Command does not match the syntax: timer delay(in minutes) (optional message). You can also abort timers by calling timer abort timernumber");
			return "Command does not match the syntax: timer delay(in minutes) (optional message). You can also abort timers by calling timer abort timernumber";
		}

		if (arguments[1].equalsIgnoreCase("abort") && 3 == arguments.length) {
			return handleCancelRequest(message, chatInterface, arguments);
		}
		int requestedDelay;
		try {
			requestedDelay = Integer.parseInt(arguments[1]);
		} catch (NumberFormatException ex) {
			// TODO: Clean this up after merging 23
			chatInterface.sendMessage(
					SeChatDescriptor.buildSeChatDescriptorFrom(message),
					"Requested delay was not a number :(");
			return "Requested delay was not a number :(";
		}

		final int timernumber;
		if (3 == arguments.length) { // includes a custom message
			timernumber = INSTANCE.get(TimerKeeper.class).addTimer(
					() -> {
						chatInterface.sendMessage(SeChatDescriptor
								.buildSeChatDescriptorFrom(message), String
								.format("@%s %s", message.getUsername(),
										arguments[2]));
					}, requestedDelay);
		} else {
			timernumber = INSTANCE.get(TimerKeeper.class).addTimer(
					() -> {
						chatInterface.sendMessage(SeChatDescriptor
								.buildSeChatDescriptorFrom(message), String
								.format("@%s %s", message.getUsername(),
										DEFAULT_MESSAGE));
					}, requestedDelay);
		}
		String result = String.format("successfully scheduled timer #%d",
				timernumber);
		// TODO: Clean this up after merging 23
		chatInterface.sendMessage(
				SeChatDescriptor.buildSeChatDescriptorFrom(message), result);

		return result;
	}

	private static String handleCancelRequest(ChatMessage message,
			ChatInterface chatInterface, String[] arguments) {
		int timerToCancel;
		try {
			timerToCancel = Integer.parseInt(arguments[2]);
		} catch (NumberFormatException ex) {
			// TODO: Clean this up after merging 23
			chatInterface.sendMessage(
					SeChatDescriptor.buildSeChatDescriptorFrom(message),
					"Timernumber was not a number :(");
			return "Timernumber was not a number :(";
		}
		// TODO: Clean this up after merging 23
		String cancelResult = INSTANCE.get(TimerKeeper.class).cancelTimer(
				timerToCancel);
		chatInterface.sendMessage(
				SeChatDescriptor.buildSeChatDescriptorFrom(message),
				cancelResult);
		return cancelResult;
	}
}
