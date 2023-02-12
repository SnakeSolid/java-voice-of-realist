package ru.snake.telegram.voiceofrealist;

import org.telegram.telegrambots.meta.api.objects.User;

@FunctionalInterface
public interface UserCallback {

	/**
	 * Send messages to given chat.
	 *
	 * @param user
	 *            user data
	 * @param userId
	 *            user identifier
	 * @param chatId
	 *            char identifier
	 */
	public void apply(final User user, final long userId, final long chatId);

}
