package ru.snake.telegram.voiceofrealist;

public enum ChatStateType {

	/**
	 * Next message from this chat will be send to all subscribed readers.
	 */
	PUBLISH_MESSAGE,

	/**
	 * Next message in this chat will contain list of users to allow write.
	 */
	ALLOW_WRITE,

	/**
	 * Next message in this chat will contain list of users to deny write.
	 */
	DENY_WRITE,

	/**
	 * Next message in this chat will contain list of users to allow manage
	 * groups.
	 */
	ALLOW_ADMIN,

	/**
	 * Next message in this chat will contain list of users to deny manage
	 * groups.
	 */
	DENY_ADMIN,

}
