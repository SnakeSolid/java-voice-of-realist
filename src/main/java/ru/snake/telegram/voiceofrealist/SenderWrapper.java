package ru.snake.telegram.voiceofrealist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Wrapper for {@link MessageSender} to hide builders and log exceptions.
 *
 * @author snake
 *
 */
public final class SenderWrapper {

	private static final Logger LOG = LoggerFactory.getLogger(SenderWrapper.class);

	private final MessageSender sender;

	private SenderWrapper(final MessageSender sender) {
		this.sender = sender;
	}

	/**
	 * Send photo to given chat.
	 *
	 * @param chatId
	 *            chat identifier
	 * @param audioId
	 *            voice identifier
	 * @param keyboardRows
	 *            keyboard for user
	 * @throws TelegramApiException
	 *             if error occurred
	 */
	public void sendVoice(final long chatId, final String audioId, final ReplyKeyboard keyboardRows) {
		InputFile inputFile = new InputFile(audioId);

		try {
			sender.sendVoice(SendVoice.builder().voice(inputFile).replyMarkup(keyboardRows).chatId(chatId).build());
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send voice", e);
		}
	}

	/**
	 * Send photo to given chat.
	 *
	 * @param chatId
	 *            chat identifier
	 * @param caption
	 *            photo caption
	 * @param photoId
	 *            photo identifier
	 * @param keyboardRows
	 *            keyboard for user
	 * @throws TelegramApiException
	 *             if error occurred
	 */
	public void
			sendPhoto(final long chatId, final String caption, final String photoId, final ReplyKeyboard keyboardRows) {
		InputFile inputFile = new InputFile(photoId);

		try {
			sender.sendPhoto(
				SendPhoto.builder().caption(caption).photo(inputFile).replyMarkup(keyboardRows).chatId(chatId).build()
			);
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send photo", e);
		}
	}

	/**
	 * Send photo to given chat.
	 *
	 * @param chatId
	 *            chat identifier
	 * @param caption
	 *            photo caption
	 * @param photoId
	 *            photo identifier
	 * @throws TelegramApiException
	 *             if error occurred
	 */
	public void sendPhoto(final long chatId, final String caption, final String photoId) {
		InputFile inputFile = new InputFile(photoId);

		try {
			sender.sendPhoto(SendPhoto.builder().caption(caption).photo(inputFile).chatId(chatId).build());
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send photo", e);
		}
	}

	/**
	 * Send text message to given chat.
	 *
	 * @param chatId
	 *            chat identifier
	 * @param text
	 *            message text
	 * @param keyboardRows
	 *            keyboard for user
	 * @throws TelegramApiException
	 *             if error occurred
	 */
	public void sendMessage(final long chatId, final String text, final ReplyKeyboard keyboardRows) {
		try {
			sender.execute(SendMessage.builder().text(text).replyMarkup(keyboardRows).chatId(chatId).build());
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send message", e);
		}
	}

	/**
	 * Send text message to given chat.
	 *
	 * @param chatId
	 *            chat identifier
	 * @param text
	 *            message text
	 * @throws TelegramApiException
	 *             if error occurred
	 */
	public void sendMessage(final long chatId, final String text) {
		try {
			sender.execute(SendMessage.builder().text(text).chatId(chatId).build());
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send message", e);
		}
	}

	public static SenderWrapper from(final MessageSender sender) {
		return new SenderWrapper(sender);
	}

	@Override
	public String toString() {
		return "SenderWrapper [sender=" + sender + "]";
	}

}
