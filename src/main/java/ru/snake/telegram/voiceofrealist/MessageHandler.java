package ru.snake.telegram.voiceofrealist;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public class MessageHandler {

	private static final int MESSAGE_LENGTH_THRESHOLD = 200;

	// Name for users collection of this bot.
	private static final String USERS = "users";

	private final SenderWrapper sender;

	private final DBContext db;

	private final UsersDAO users;

	private final Chats chats;

	public MessageHandler(final MessageSender sender, final DBContext db, final long creatorId) {
		this.sender = SenderWrapper.from(sender);
		this.db = db;
		this.users = UsersDAO.from(creatorId, db.getMap(USERS));
		this.chats = new Chats();
	}

	public void replyToDefault(final MessageContext context) {
		long chatId = context.chatId();
		Update update = context.update();

		if (update.hasCallbackQuery()) {
			processCallback(context, chatId, update);
		} else if (update.hasMessage() && chats.isPublishMessage(chatId)) {
			processPublish(chatId, update);
		} else if (update.hasMessage() && chats.isAllowAdmins(chatId)) {
			Message message = update.getMessage();

			if (message.hasText()) {
				String text = message.getText().toLowerCase();
				Set<String> userNames = TextUtils.extractUserNames(text);

				users.addAdmins(userNames);
				chats.remove(chatId);

				sender.sendMessage(chatId, "Список администраторов успешно обновлен.");
			} else {
				sender.sendMessage(chatId, "Сообщение должно содержать только текст.");
			}
		} else if (update.hasMessage() && chats.isDenyAdmins(chatId)) {
			Message message = update.getMessage();

			if (message.hasText()) {
				String text = message.getText().toLowerCase();
				Set<String> userNames = TextUtils.extractUserNames(text);

				users.removeAdmins(userNames);
				chats.remove(chatId);

				sender.sendMessage(chatId, "Список администраторов успешно обновлен.");
			} else {
				sender.sendMessage(chatId, "Сообщение должно содержать только текст.");
			}
		} else if (update.hasMessage() && chats.isAllowWrite(chatId)) {
			Message message = update.getMessage();

			if (message.hasText()) {
				String text = message.getText().toLowerCase();
				Set<String> userNames = TextUtils.extractUserNames(text);

				users.addWriters(userNames);
				chats.remove(chatId);

				sender.sendMessage(chatId, "Список писателей успешно обновлен.");
			} else {
				sender.sendMessage(chatId, "Сообщение должно содержать только текст.");
			}
		} else if (update.hasMessage() && chats.isDenyWrite(chatId)) {
			Message message = update.getMessage();

			if (message.hasText()) {
				String text = message.getText().toLowerCase();
				Set<String> userNames = TextUtils.extractUserNames(text);

				users.removeWriters(userNames);
				chats.remove(chatId);

				sender.sendMessage(chatId, "Список писателей успешно обновлен.");
			} else {
				sender.sendMessage(chatId, "Сообщение должно содержать только текст.");
			}
		} else {
			User user = context.user();
			Long userId = user.getId();

			ReplyKeyboard keyboard = KeyboardFactory.userKeyboard(users.isAdmin(userId), users.isWriter(userId));
			sender.sendMessage(chatId, "Для работы с ботом используйте следующие команды.", keyboard);
		}
	}

	private void processPublish(final long chatId, final Update update) {
		Message message = update.getMessage();
		boolean isSuccess = false;

		if (message.hasText()) {
			String text = message.getText();
			chats.setText(chatId, text);

			isSuccess = true;
		} else if (message.hasPhoto()) {
			String caption = message.getCaption();
			String photoId = selectPhoto(message.getPhoto());
			chats.setPhoto(chatId, caption, photoId);

			isSuccess = true;
		} else if (message.hasVoice()) {
			String voiceId = message.getVoice().getFileId();
			chats.setVoice(chatId, voiceId);

			isSuccess = true;
		}

		if (isSuccess) {
			sender.sendMessage(chatId, "Сообщение сохранено.", KeyboardFactory.sendViewCancel());
		} else {
			sender.sendMessage(
				chatId,
				"Ошибка, неподдерживаемый тип сообщения. Используйте только текст, фотографии или голос."
			);
		}
	}

	private void processCallback(final MessageContext context, final long chatId, final Update update) {
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

		case Constants.LIST_USERS:
			replyToListUsers(context);
			break;

		case Constants.LIST_WRITERS:
			replyToListWriters(context);
			break;

		case Constants.LIST_ADMINS:
			replyToListAdmins(context);
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
			sender.sendMessage(chatId, "Некорректные данные в обратном вызове: " + data);
		}
	}

	/**
	 * Select largest photo and return its internal identifier.
	 *
	 * @param photos
	 *            photo list
	 * @return photo identifier
	 */
	private String selectPhoto(final List<PhotoSize> photos) {
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

	public void replyToStart(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			users.ensureUser(user, chatId);

			ReplyKeyboard keyboard = KeyboardFactory.userKeyboard(users.isAdmin(userId), users.isWriter(userId));
			sender
				.sendMessage(chatId, "Добро пожаловать в бот уведомлений. Вам доступны следующие действия:", keyboard);
		});
	}

	public void replyToSubscribe(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			users.subscribeUser(userId);

			sender.sendMessage(chatId, "Вы подписаны. Теперь вы будете получать уведомления.");
		});
	}

	public void replyToUnsubscribe(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			users.unsubscribeUser(userId);

			sender.sendMessage(chatId, "Вы отписаны. Больше вы не будете получать уведомления.");
		});
	}

	public void replyToStatus(final MessageContext context) {
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

			ReplyKeyboard keyboard = KeyboardFactory.userKeyboard(users.isAdmin(userId), users.isWriter(userId));

			sender.sendMessage(chatId, builder.toString().stripTrailing(), keyboard);
		});
	}

	public void replyToPublish(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			if (!users.isWriter(userId)) {
				sender.sendMessage(
					chatId,
					"К сожалению, вы не можете писать публикации. Свяжитесь с администратором, чтобы получить такую возможность."
				);

				return;
			}

			chats.put(chatId, ChatState.publishMessage());

			sender.sendMessage(
				chatId,
				"Следующим сообщением напишите текст публикации, который будет отправлени всем подписанным пользователям. Поддерживаются следующие типы сообщений: текст, изображения и голос."
			);
		});
	}

	public void replyToWritersRemove(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			if (!users.isAdmin(userId)) {
				sender.sendMessage(chatId, "Нужно быть администратором, чтобы выполнить это действие.");

				return;
			}

			chats.put(chatId, ChatState.denyWrite());

			sender.sendMessage(
				chatId,
				"Следующим сообщением пришлите спилок логинов пользователей, разделенных любыми пробельными символами, запятыми или точками с запятой."
			);
		});
	}

	public void replyToWritersAdd(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			if (!users.isAdmin(userId)) {
				sender.sendMessage(chatId, "Нужно быть администратором, чтобы выполнить это действие.");

				return;
			}

			chats.put(chatId, ChatState.allowWrite());

			sender.sendMessage(
				chatId,
				"Следующим сообщением пришлите спилок логинов пользователей, разделенных любыми пробельными символами, запятыми или точками с запятой."
			);
		});
	}

	public void replyToAdminsRemove(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			if (!users.isAdmin(userId)) {
				sender.sendMessage(chatId, "Нужно быть администратором, чтобы выполнить это действие.");

				return;
			}

			chats.put(chatId, ChatState.denyAdmin());

			sender.sendMessage(
				chatId,
				"Следующим сообщением пришлите спилок логинов пользователей, разделенных любыми пробельными символами, запятыми или точками с запятой."
			);
		});
	}

	public void replyToAdminsAdd(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			if (!users.isAdmin(userId)) {
				sender.sendMessage(chatId, "Нужно быть администратором, чтобы выполнить это действие.");

				return;
			}

			chats.put(chatId, ChatState.allowAdmin());

			sender.sendMessage(
				chatId,
				"Следующим сообщением пришлите спилок логинов пользователей, разделенных любыми пробельными символами, запятыми или точками с запятой."
			);
		});
	}

	public void replyToListAdmins(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			if (!users.isAdmin(userId)) {
				sender.sendMessage(chatId, "Нужно быть администратором, чтобы выполнить это действие.");

				return;
			}

			Set<String> userNames = users.getAdmins();
			groupNames(userNames, batch -> sender.sendMessage(chatId, batch));
		});
	}

	public void replyToListWriters(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			if (!users.isAdmin(userId)) {
				sender.sendMessage(chatId, "Нужно быть администратором, чтобы выполнить это действие.");

				return;
			}

			Set<String> userNames = users.getWriters();
			groupNames(userNames, batch -> sender.sendMessage(chatId, batch));
		});
	}

	public void replyToListUsers(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			if (!users.isAdmin(userId)) {
				sender.sendMessage(chatId, "Нужно быть администратором, чтобы выполнить это действие.");

				return;
			}

			Set<String> userNames = users.getUserNames();
			groupNames(userNames, batch -> sender.sendMessage(chatId, batch));
		});
	}

	private void groupNames(final Set<String> userNames, final Consumer<String> callback) {
		StringBuilder builder = new StringBuilder();

		for (String userName : userNames) {
			if (builder.length() > 0) {
				builder.append(", ");
			}

			builder.append(userName);

			if (builder.length() > MESSAGE_LENGTH_THRESHOLD) {
				callback.accept(builder.toString());
				builder.setLength(0);
			}
		}

		if (builder.length() > 0) {
			callback.accept(builder.toString());
		}
	}

	public void replyToSend(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			ChatState state = chats.get(chatId);

			if (state != null) {
				List<Long> readers = users.allReaders();
				String text = state.getText();
				String caption = state.getCaption();
				String photoId = state.getPhotoId();
				String voiceId = state.getVoiceId();

				if (text != null) {
					readers.forEach(readerChatId -> sender.sendMessage(readerChatId, text));
				} else if (caption != null || photoId != null) {
					readers.forEach(readerChatId -> sender.sendPhoto(readerChatId, caption, photoId));
				} else if (voiceId != null) {
					readers.forEach(
						readerChatId -> sender.sendVoice(readerChatId, voiceId, KeyboardFactory.sendViewCancel())
					);
				}

				ReplyKeyboard keyboard = KeyboardFactory.userKeyboard(users.isAdmin(userId), users.isWriter(userId));
				sender.sendMessage(chatId, "Сообщение отправлено", keyboard);
			}

			chats.remove(chatId);
		});
	}

	public void replyToView(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			ChatState state = chats.get(chatId);

			if (state != null) {
				String text = state.getText();
				String caption = state.getCaption();
				String photoId = state.getPhotoId();
				String voiceId = state.getVoiceId();

				if (text != null) {
					sender.sendMessage(chatId, text, KeyboardFactory.sendViewCancel());
				} else if (photoId != null) {
					sender.sendPhoto(chatId, caption, photoId, KeyboardFactory.sendViewCancel());
				} else if (voiceId != null) {
					sender.sendVoice(chatId, voiceId, KeyboardFactory.sendViewCancel());
				} else {
					sender.sendMessage(chatId, "<Сообщение отсутствует>", KeyboardFactory.sendViewCancel());
				}
			}
		});
	}

	public void replyToDelete(final MessageContext context) {
		withUser(context, (user, userId, chatId) -> {
			chats.remove(chatId);

			ReplyKeyboard keyboard = KeyboardFactory.userKeyboard(users.isAdmin(userId), users.isWriter(userId));
			sender.sendMessage(chatId, "Сообщение удалено.", keyboard);
		});
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
		return "MessageHandler [sender=" + sender + ", db=" + db + ", users=" + users + ", chats=" + chats + "]";
	}

}
