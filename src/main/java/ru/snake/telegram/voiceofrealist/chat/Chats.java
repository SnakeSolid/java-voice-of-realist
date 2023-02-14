package ru.snake.telegram.voiceofrealist.chat;

import java.util.HashMap;
import java.util.Map;

public class Chats {

	private final Map<Long, ChatState> chatStates;

	public Chats() {
		this.chatStates = new HashMap<>();
	}

	/**
	 * Check that chat in waiting publication state.
	 *
	 * @param chatId
	 *            chat identifier
	 * @return true if chat exists and in waiting message state
	 */
	public boolean isPublishMessage(long chatId) {
		ChatState state = chatStates.get(chatId);

		return state != null && state.isPublishMessage();
	}

	public boolean isDenyWrite(long chatId) {
		ChatState state = chatStates.get(chatId);

		return state != null && state.isDenyWrite();
	}

	public boolean isAllowWrite(long chatId) {
		ChatState state = chatStates.get(chatId);

		return state != null && state.isAllowWrite();
	}

	public boolean isDenyAdmins(long chatId) {
		ChatState state = chatStates.get(chatId);

		return state != null && state.isDenyAdmins();
	}

	public boolean isAllowAdmins(long chatId) {
		ChatState state = chatStates.get(chatId);

		return state != null && state.isAllowAdmins();
	}

	public void setText(long chatId, String text) {
		ChatState state = chatStates.get(chatId);

		if (state != null) {
			state.setText(text);
		}
	}

	public void setPhoto(long chatId, String caption, String photoId) {
		ChatState state = chatStates.get(chatId);

		if (state != null) {
			state.setCaption(caption);
			state.setPhotoId(photoId);
		}
	}

	public void setVoice(long chatId, String voiceId) {
		ChatState state = chatStates.get(chatId);

		if (state != null) {
			state.setVoiceId(voiceId);
		}
	}

	public ChatState get(long chatId) {
		return chatStates.get(chatId);
	}

	public void put(long chatId, ChatState chatState) {
		chatStates.put(chatId, chatState);
	}

	public void remove(long chatId) {
		chatStates.remove(chatId);
	}

	@Override
	public String toString() {
		return "Chats [chatStates=" + chatStates + "]";
	}

}
