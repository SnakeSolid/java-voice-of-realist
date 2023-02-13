package ru.snake.telegram.voiceofrealist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class KeyboardFactory {

	private KeyboardFactory() {
	}

	/**
	 * Creates in-line keyboard for given user according its rights.
	 *
	 * @param isAdmin
	 *            is user administrator
	 * @param isWriter
	 *            is user writer
	 * @return reply keyboard
	 */
	public static ReplyKeyboard userKeyboard(final boolean isAdmin, final boolean isWriter) {
		Collection<List<InlineKeyboardButton>> rows = new ArrayList<>();
		rows.add(
			Arrays.asList(
				InlineKeyboardButton.builder().text("Подписаться").callbackData(Constants.SUBSCRIBE).build(),
				InlineKeyboardButton.builder().text("Отписаться").callbackData(Constants.UNSUBSCRIBE).build(),
				InlineKeyboardButton.builder().text("Статус").callbackData(Constants.STATE).build()
			)
		);

		if (isAdmin) {
			rows.add(
				Arrays.asList(
					InlineKeyboardButton.builder().text("Все пользователи").callbackData(Constants.LIST_USERS).build(),
					InlineKeyboardButton.builder().text("Писатели").callbackData(Constants.LIST_USERS).build(),
					InlineKeyboardButton.builder().text("Администраторы").callbackData(Constants.LIST_USERS).build()
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

		if (isWriter) {
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
	public static ReplyKeyboard sendViewCancel() {
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

}
