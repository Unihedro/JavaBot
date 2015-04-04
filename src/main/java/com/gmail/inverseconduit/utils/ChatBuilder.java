package com.gmail.inverseconduit.utils;

import com.gmail.inverseconduit.datatype.ChatMessage;

/**
 * Helper class for building chat messages that have SO Chat markdown.
 * @author Michael Angstadt
 * @see <a href="http://chat.stackoverflow.com/faq#formatting">Formatting
 * FAQ</a>
 */
public class ChatBuilder implements CharSequence {
	private final StringBuilder sb;

	/**
	 * Creates a new chat builder.
	 */
	public ChatBuilder() {
		sb = new StringBuilder();
	}

	/**
	 * Creates a new chat builder.
	 * @param text the string to populate it with
	 */
	public ChatBuilder(String text) {
		sb = new StringBuilder(text);
	}

	/**
	 * Appends the character sequence for "fixed font".
	 * @return this
	 */
	public ChatBuilder fixed() {
		return append("    ");
	}

	/**
	 * Appends the character sequence for "bold".
	 * @return this
	 */
	public ChatBuilder bold() {
		return append("**");
	}

	/**
	 * Wraps text in "bold" formatting.
	 * @param text the text to wrap
	 * @return this
	 */
	public ChatBuilder bold(String text) {
		return bold().append(text).bold();
	}

	/**
	 * Appends the character sequence for "code".
	 * @return this
	 */
	public ChatBuilder code() {
		return append('`');
	}

	/**
	 * Wraps text in "code" formatting.
	 * @param text the text to wrap
	 * @return this
	 */
	public ChatBuilder code(String text) {
		return code().append(text).code();
	}

	/**
	 * Appends the character sequence for "italic".
	 * @return this
	 */
	public ChatBuilder italic() {
		return append('*');
	}

	/**
	 * Wraps text in "italic" formatting.
	 * @param text the text to wrap
	 * @return this
	 */
	public ChatBuilder italic(String text) {
		return italic().append(text).italic();
	}

	/**
	 * Appends a clickable link.
	 * @param display the display text
	 * @param url the URL
	 * @return this
	 */
	public ChatBuilder link(String display, String url) {
		return link(display, url, null);
	}

	/**
	 * Appends a clickable link.
	 * @param display the display text
	 * @param url the URL
	 * @param title the link title or null/empty for no title
	 * @return this
	 */
	public ChatBuilder link(String display, String url, String title) {
		append('[').append(display.trim()).append("](").append(url.trim());
		if (title != null && !title.isEmpty()) {
			append(" \"").append(title.trim()).append('"');
		}
		return append(')');
	}

	/**
	 * Appends a newline character.
	 * @return this
	 */
	public ChatBuilder nl() {
		return append('\n');
	}

	/**
	 * Appends the "reply to message" syntax.
	 * @param message the message to reply to
	 * @return this
	 */
	public ChatBuilder reply(ChatMessage message) {
		//TODO return append(':').append(message.getMessageId() + "").append(' ');
		return append('@').append(message.getUsername()).append(' ');
	}

	/**
	 * Appends the character sequence for "strike through".
	 * @return this
	 */
	public ChatBuilder strike() {
		return append("---");
	}

	/**
	 * Wraps text in "strike through" formatting.
	 * @param text the text to wrap
	 * @return this
	 */
	public ChatBuilder strike(String text) {
		return strike().append(text).strike();
	}

	/**
	 * Appends a tag.
	 * @param tag the tag name
	 * @return this
	 */
	public ChatBuilder tag(String tag) {
		return append("[tag:").append(tag).append(']');
	}

	/**
	 * Appends a raw character.
	 * @param c the character to append
	 * @return this
	 */
	public ChatBuilder append(char c) {
		sb.append(c);
		return this;
	}

	/**
	 * Appends a raw string.
	 * @param text the string to append
	 * @return this
	 */
	public ChatBuilder append(CharSequence text) {
		sb.append(text);
		return this;
	}

	@Override
	public int length() {
		return sb.length();
	}

	@Override
	public char charAt(int index) {
		return sb.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return sb.subSequence(start, end);
	}

	@Override
	public String toString() {
		return sb.toString();
	}
}
