package ru.snake.telegram.voiceofrealist;

import java.io.Serializable;

public class UserData implements Serializable {

	private static final long serialVersionUID = 1L;

	private final long userId;

	private final long chatId;

	private String userName;

	private String firstName;

	private String lastName;

	private boolean reader;

	private boolean writer;

	private boolean admin;

	private UserData(
		final long userId,
		final long chatId,
		final String userName,
		final String firstName,
		final String lastName,
		final boolean reader,
		final boolean writer,
		final boolean admin
	) {
		this.userId = userId;
		this.chatId = chatId;
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.reader = reader;
		this.writer = writer;
		this.admin = admin;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName
	 *            the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName
	 *            the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the reader
	 */
	public boolean isReader() {
		return reader;
	}

	/**
	 * @param reader
	 *            the reader to set
	 */
	public void setReader(boolean reader) {
		this.reader = reader;
	}

	/**
	 * @return the writer
	 */
	public boolean isWriter() {
		return writer;
	}

	/**
	 * @param writer
	 *            the writer to set
	 */
	public void setWriter(boolean writer) {
		this.writer = writer;
	}

	/**
	 * @return the admin
	 */
	public boolean isAdmin() {
		return admin;
	}

	/**
	 * @param admin
	 *            the admin to set
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 * @return the userId
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * @return the chatId
	 */
	public long getChatId() {
		return chatId;
	}

	@Override
	public String toString() {
		return "UserData [userId=" + userId + ", chatId=" + chatId + ", userName=" + userName + ", firstName="
				+ firstName + ", lastName=" + lastName + ", reader=" + reader + ", writer=" + writer + ", admin="
				+ admin + "]";
	}

	public static UserData create(final long userId, final long chatId) {
		return new UserData(userId, chatId, null, null, null, false, false, false);
	}

}
