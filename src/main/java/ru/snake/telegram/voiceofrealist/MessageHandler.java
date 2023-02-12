package ru.snake.telegram.voiceofrealist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MessageHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MessageHandler.class);

	// Name for users collection of this bot.
	private static final String USERS = "users";

	private final MessageSender sender;

	private final DBContext db;

	private final UsersDAO users;

	private final Map<Long, ChatState> chatStates;

	public MessageHandler(final MessageSender sender, final DBContext db, final long creatorId) {
		this.sender = sender;
		this.db = db;
		this.users = UsersDAO.from(db.getMap(USERS));
		this.chatStates = new HashMap<>();
	}

	public void replyToDefault(MessageContext context) {
		long chatId = context.chatId();
		Update update = context.update();

		if (update.hasCallbackQuery()) {
			CallbackQuery callbackQuery = update.getCallbackQuery();
			String data = callbackQuery.getData();

			switch (data) {
			case Constants.SUBSCRIBE:
				replyToSubscribe(context);
				break;

			case Constants.UNSUBSCRIBE:
				replyToUnsubscribe(context);
				break;

			case Constants.STATE:
				replyToStatus(context);
				break;

			case Constants.USER_LIST:
				replyToUserList(context);
				break;

			case Constants.ADMINS_ADD:
				replyToAdminsAdd(context);
				break;

			case Constants.ADMINS_REMOVE:
				replyToAdminsRemove(context);
				break;

			case Constants.WRITERS_ADD:
				replyToWritersAdd(context);
				break;

			case Constants.WRITERS_REMOVE:
				replyToWritersRemove(context);
				break;

			case Constants.PUBLISH:
				replyToPublish(context);
				break;

			case Constants.SEND:
				replyToSend(context);
				break;

			case Constants.VIEW:
				replyToView(context);
				break;

			case Constants.DELETE:
				replyToDelete(context);
				break;

			default:
				sendMessage(chatId, "Некорректные данные в обратном вызове: " + data);
			}
		} else if (update.hasMessage() && isPublishMessage(chatId)) {
			Message message = update.getMessage();
			boolean isSuccess = false;

			if (message.hasText()) {
				String text = message.getText();
				chatStates.get(chatId).setText(text);

				isSuccess = true;
			} else if (message.hasPhoto()) {
				String caption = message.getCaption();
				String photoId = downloadPhoto(message.getPhoto());
				chatStates.get(chatId).setCaption(caption);
				chatStates.get(chatId).setPhotoId(photoId);

				isSuccess = true;
			} else if (message.hasVoice()) {
				String voiceId = message.getVoice().getFileId();
				chatStates.get(chatId).setVoiceId(voiceId);

				isSuccess = true;
			}

			if (isSuccess) {
				sendMessage(chatId, "Сообщение сохранено.", getSendViewCancel());
			} else {
				sendMessage(
					chatId,
					"Ошибка, неподдерживаемый тип сообщения. Используйте только текст, фотографии или голос."
				);
			}
		} else if (update.hasMessage() && isAllowAdmins(chatId)) {
			Message message = update.getMessage();

			if (message.hasText()) {
				String text = message.getText().toLowerCase();
				Set<String> userNames = extractUserNames(text);

				users.addAdmins(userNames);
				chatStates.remove(chatId);

				sendMessage(chatId, "Список администраторов успешно обновлен.");
			} else {
				sendMessage(chatId, "Сообщение должно содержать только текст.");
			}
		} else if (update.hasMessage() && isDenyAdmins(chatId)) {
			Message message = update.getMessage();

			if (message.hasText()) {
				String text = message.getText().toLowerCase();
				Set<String> userNames = extractUserNames(text);

				users.removeAdmins(userNames);
				chatStates.remove(chatId);

				sendMessage(chatId, "Список администраторов успешно обновлен.");
			} else {
				sendMessage(chatId, "Сообщение должно содержать только текст.");
			}
		} else if (update.hasMessage() && isAllowWrite(chatId)) {
			Message message = update.getMessage();

			if (message.hasText()) {
				String text = message.getText().toLowerCase();
				Set<String> userNames = extractUserNames(text);

				users.addWriters(userNames);
				chatStates.remove(chatId);

				sendMessage(chatId, "Список писателей успешно обновлен.");
			} else {
				sendMessage(chatId, "Сообщение должно содержать только текст.");
			}
		} else if (update.hasMessage() && isDenyWrite(chatId)) {
			Message message = update.getMessage();

			if (message.hasText()) {
				String text = message.getText().toLowerCase();
				Set<String> userNames = extractUserNames(text);

				users.removeWriters(userNames);
				chatStates.remove(chatId);

				sendMessage(chatId, "Список писателей успешно обновлен.");
			} else {
				sendMessage(chatId, "Сообщение должно содержать только текст.");
			}
		} else {
			User user = context.user();
			Long userId = user.getId();

			sendMessage(chatId, "Для работы с ботом используйте следующие команды.", getUserKeyboard(userId));
		}
	}

	private Set<String> extractUserNames(String text) {
		Pattern pattern = Pattern.compile("[a-z0-9]+");
		Matcher matcher = pattern.matcher(text);
		Set<String> result = new HashSet<>();

		while (matcher.find()) {
			String login = matcher.group();

			result.add(login);
		}

		return result;
	}

	/**
	 * Download photo and return its internal identifier.
	 * 
	 * @param photos
	 *            photo list
	 * @return photo identifier
	 */
	private String downloadPhoto(List<PhotoSize> photos) {
		PhotoSize largestPhoto = null;

		for (PhotoSize photo : photos) {
			if (largestPhoto == null || largestPhoto.getFileSize() < photo.getFileSize()) {
				largestPhoto = photo;
			}
		}

		if (largestPhoto == null) {
			return null;
		}

		return largestPhoto.getFileId();
	}

	private boolean isDenyWrite(long chatId) {
		ChatState state = chatStates.get(chatId);

		return state != null && state.isDenyWrite();
	}

	private boolean isAllowWrite(long chatId) {
		ChatState state = chatStates.get(chatId);

		return state != null && state.isAllowWrite();
	}

	private boolean isDenyAdmins(long chatId) {
		ChatState state = chatStates.get(chatId);

		return state != null && state.isDenyAdmins();
	}

	private boolean isAllowAdmins(long chatId) {
		ChatState state = chatStates.get(chatId);

		return state != null && state.isAllowAdmins();
	}

	/**
	 * Check that chat in waiting publication state.
	 *
	 * @param chatId
	 *            chat identifier
	 * @return true if chat exists and in waiting message state
	 */
	private boolean isPublishMessage(long chatId) {
		ChatState state = chatStates.get(chatId);

		return state != null && state.isPublishMessage();
	}

	public void replyToStart(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			users.ensureUser(user, chatId);

			sendMessage(chatId, "Добро пожаловать в бот уведомлений. Вам доступны следующие действия:");
		});
	}

	public void replyToSubscribe(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			users.subscribeUser(userId);

			sendMessage(chatId, "Вы подписаны. Теперь вы будете получать уведомления.");
		});
	}

	public void replyToUnsubscribe(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			users.unsubscribeUser(userId);

			sendMessage(chatId, "Вы отписаны. Больше вы не будете получать уведомления.");
		});
	}

	public void replyToStatus(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			StringBuilder builder = new StringBuilder();

			if (users.isValidUser(userId)) {
				builder.append("Вы являетесь пользователем бота нотификаций.\n");
			}

			if (users.isReader(userId)) {
				builder.append("Вы подписаны на публикации данного бота.\n");
			} else {
				builder.append("Вы НЕ подписаны на публикации данного бота.\n");
			}

			if (users.isWriter(userId)) {
				builder.append("Вы можете писать публикации для пользователей бота.\n");
			}

			if (users.isAdmin(userId)) {
				builder.append("Вы можете управлять списком писателей и администраторов бота.\n");
			}

			sendMessage(chatId, builder.toString().stripTrailing(), getUserKeyboard(userId));
		});
	}

	public void replyToPublish(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			if (users.isWriter(userId)) {
				chatStates.put(chatId, ChatState.publishMessage());

				sendMessage(
					chatId,
					"Следующим сообщением напишите текст публикации, который будет отправлени всем подписанным пользователям. Поддерживаются следующие типы сообщений: текст, изображения и голос."
				);
			} else {
				sendMessage(
					chatId,
					"К сожалению, вы не можете писать публикации. Свяжитесь с администратором, чтобы получить такую возможность."
				);
			}
		});
	}

	public void replyToWritersRemove(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			chatStates.put(chatId, ChatState.denyWrite());

			sendMessage(
				chatId,
				"Следующим сообщением пришлите спилок логинов пользователей, разделенных любыми пробельными символами, запятыми или точками с запятой."
			);
		});
	}

	public void replyToWritersAdd(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			chatStates.put(chatId, ChatState.allowWrite());

			sendMessage(
				chatId,
				"Следующим сообщением пришлите спилок логинов пользователей, разделенных любыми пробельными символами, запятыми или точками с запятой."
			);
		});
	}

	public void replyToAdminsRemove(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			chatStates.put(chatId, ChatState.denyAdmin());

			sendMessage(
				chatId,
				"Следующим сообщением пришлите спилок логинов пользователей, разделенных любыми пробельными символами, запятыми или точками с запятой."
			);
		});
	}

	public void replyToAdminsAdd(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			chatStates.put(chatId, ChatState.allowAdmin());

			sendMessage(
				chatId,
				"Следующим сообщением пришлите спилок логинов пользователей, разделенных любыми пробельными символами, запятыми или точками с запятой."
			);
		});
	}

	public void replyToUserList(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			if (!users.isAdmin(userId)) {
				sendMessage(chatId, "Нужно быть администратором, чтобы выполнить это действие.");

				return;
			}

			Set<String> userNames = users.getUserNames();
			StringBuilder builder = new StringBuilder();

			for (String userName : userNames) {
				if (builder.length() > 0) {
					builder.append(", ");
				}

				builder.append(userName);

				if (builder.length() > 200) {
					sendMessage(chatId, builder.toString());
					builder.setLength(0);
				}
			}

			if (builder.length() > 0) {
				sendMessage(chatId, builder.toString());
			}
		});
	}

	public void replyToSend(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			ChatState state = chatStates.get(chatId);

			if (state != null) {
				List<Long> readers = users.allReaders();
				String text = state.getText();
				String caption = state.getCaption();
				String photoId = state.getPhotoId();
				String voiceId = state.getVoiceId();

				if (text != null) {
					readers.forEach(readerChatId -> sendMessage(readerChatId, text));
				} else if (caption != null || photoId != null) {
					readers.forEach(readerChatId -> sendPhoto(readerChatId, caption, photoId));
				} else if (voiceId != null) {
					readers.forEach(readerChatId -> sendVoice(readerChatId, voiceId, getSendViewCancel()));
				}

				sendMessage(chatId, "Сообщение отправлено");
			}

			chatStates.remove(chatId);
		});
	}

	public void replyToView(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			ChatState state = chatStates.get(chatId);

			if (state != null) {
				String text = state.getText();
				String caption = state.getCaption();
				String photoId = state.getPhotoId();
				String voiceId = state.getVoiceId();

				if (text != null) {
					sendMessage(chatId, text, getSendViewCancel());
				} else if (photoId != null) {
					sendPhoto(chatId, caption, photoId, getSendViewCancel());
				} else if (voiceId != null) {
					sendVoice(chatId, voiceId, getSendViewCancel());
				} else {
					sendMessage(chatId, "<Сообщение отсутствует>", getSendViewCancel());
				}
			}
		});
	}

	public void replyToDelete(MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			chatStates.remove(chatId);

			sendMessage(chatId, "Сообщение удалено.", getUserKeyboard(userId));
		});
	}

	/**
	 * Creates in-line keyboard for given user according its rights.
	 *
	 * @param userId
	 *            user identifier
	 * @return reply keyboard
	 */
	private ReplyKeyboard getUserKeyboard(long userId) {
		Collection<List<InlineKeyboardButton>> rows = new ArrayList<>();
		rows.add(
			Arrays.asList(
				InlineKeyboardButton.builder().text("Подписаться").callbackData(Constants.SUBSCRIBE).build(),
				InlineKeyboardButton.builder().text("Отписаться").callbackData(Constants.UNSUBSCRIBE).build(),
				InlineKeyboardButton.builder().text("Статус").callbackData(Constants.STATE).build()
			)
		);

		if (users.isAdmin(userId)) {
			rows.add(
				Arrays.asList(
					InlineKeyboardButton.builder().text("Все пользователи").callbackData(Constants.USER_LIST).build()
				)
			);
			rows.add(
				Arrays.asList(
					InlineKeyboardButton.builder()
						.text("Добавить администратора")
						.callbackData(Constants.ADMINS_ADD)
						.build(),
					InlineKeyboardButton.builder()
						.text("Исключить администратора")
						.callbackData(Constants.ADMINS_REMOVE)
						.build()
				)
			);
			rows.add(
				Arrays.asList(
					InlineKeyboardButton.builder().text("Добавить авторов").callbackData(Constants.WRITERS_ADD).build(),
					InlineKeyboardButton.builder()
						.text("Исключить авторов")
						.callbackData(Constants.WRITERS_REMOVE)
						.build()
				)
			);
		}

		if (users.isWriter(userId)) {
			rows.add(
				Arrays.asList(
					InlineKeyboardButton.builder()
						.text("Опубликовать сообщение")
						.callbackData(Constants.PUBLISH)
						.build()
				)
			);
		}

		return InlineKeyboardMarkup.builder().keyboard(rows).build();
	}

	/**
	 * Creates publication keyboard with send, view and delete buttons.
	 *
	 * @return in-line keyboard
	 */
	private ReplyKeyboard getSendViewCancel() {
		Collection<List<InlineKeyboardButton>> rows = new ArrayList<>();
		rows.add(
			Arrays.asList(
				InlineKeyboardButton.builder().text("Оптравить").callbackData(Constants.SEND).build(),
				InlineKeyboardButton.builder().text("Посмотреть").callbackData(Constants.VIEW).build(),
				InlineKeyboardButton.builder().text("Удалить").callbackData(Constants.DELETE).build()
			)
		);

		return InlineKeyboardMarkup.builder().keyboard(rows).build();
	}

	private void sendVoice(long chatId, final String audioId, final ReplyKeyboard keyboardRows) {
		InputFile inputFile = new InputFile(audioId);

		try {
			sender.sendVoice(SendVoice.builder().voice(inputFile).replyMarkup(keyboardRows).chatId(chatId).build());
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send voice", e);
		}
	}

	private void sendPhoto(long chatId, final String caption, final String photoId, final ReplyKeyboard keyboardRows) {
		InputFile inputFile = new InputFile(photoId);

		try {
			sender.sendPhoto(
				SendPhoto.builder().caption(caption).photo(inputFile).replyMarkup(keyboardRows).chatId(chatId).build()
			);
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send photo", e);
		}
	}

	private void sendPhoto(long chatId, final String caption, final String photoId) {
		InputFile inputFile = new InputFile(photoId);

		try {
			sender.sendPhoto(SendPhoto.builder().caption(caption).photo(inputFile).chatId(chatId).build());
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send photo", e);
		}
	}

	private void sendMessage(long chatId, String text, ReplyKeyboard keyboardRows) {
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
	private void sendMessage(final long chatId, final String text) {
		try {
			sender.execute(SendMessage.builder().text(text).chatId(chatId).build());
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send message", e);
		}
	}

	private void withUser(final MessageContext context, final UserCallback callback) {
		User user = context.user();

		if (user.getIsBot()) {
			return;
		}

		long userId = user.getId();
		long chatId = context.chatId();

		callback.apply(user, userId, chatId);
	}

	@Override
	public String toString() {
		return "MessageHandler [sender=" + sender + ", db=" + db + ", users=" + users + ", chatStates=" + chatStates
				+ "]";
	}

}
