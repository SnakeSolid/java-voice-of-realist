package ru.snake.telegram.voiceofrealist.chat;

public class ChatState {

	private final ChatStateType stateType;

	private String text;

	private String caption;

	private String photoId;

	private String voiceId;

	private ChatState(final ChatStateType stateType) {
		this.stateType = stateType;
		this.text = null;
		this.caption = null;
		this.photoId = null;
		this.voiceId = null;
	}

	public boolean isAllowAdmins() {
		return stateType == ChatStateType.ALLOW_ADMIN;
	}

	public boolean isDenyAdmins() {
		return stateType == ChatStateType.DENY_ADMIN;
	}

	public boolean isAllowWrite() {
		return stateType == ChatStateType.ALLOW_WRITE;
	}

	public boolean isDenyWrite() {
		return stateType == ChatStateType.DENY_WRITE;
	}

	public boolean isPublishMessage() {
		return stateType == ChatStateType.PUBLISH_MESSAGE;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the caption
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * @param caption
	 *            the caption to set
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * @return the photoId
	 */
	public String getPhotoId() {
		return photoId;
	}

	/**
	 * @param photoId
	 *            the photoId to set
	 */
	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

	/**
	 * @return the voiceId
	 */
	public String getVoiceId() {
		return voiceId;
	}

	/**
	 * @param voiceId
	 *            the voiceId to set
	 */
	public void setVoiceId(String voiceId) {
		this.voiceId = voiceId;
	}

	@Override
	public String toString() {
		return "ChatState [stateType=" + stateType + ", text=" + text + ", caption=" + caption + ", photoId=" + photoId
				+ ", audioId=" + voiceId + "]";
	}

	public static ChatState publishMessage() {
		return new ChatState(ChatStateType.PUBLISH_MESSAGE);
	}

	public static ChatState allowWrite() {
		return new ChatState(ChatStateType.ALLOW_WRITE);
	}

	public static ChatState denyWrite() {
		return new ChatState(ChatStateType.DENY_WRITE);
	}

	public static ChatState allowAdmin() {
		return new ChatState(ChatStateType.ALLOW_ADMIN);
	}

	public static ChatState denyAdmin() {
		return new ChatState(ChatStateType.DENY_ADMIN);
	}

}
