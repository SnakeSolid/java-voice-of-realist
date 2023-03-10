package ru.snake.telegram.voiceofrealist.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.telegram.telegrambots.meta.api.objects.User;

public class UsersDAO {

	private final long creatorId;

	private final Map<Long, UserData> inner;

	public UsersDAO(final long creatorId, final Map<Long, UserData> inner) {
		this.creatorId = creatorId;
		this.inner = inner;
	}

	public synchronized void ensureUser(final User user, final long chatId) {
		long userId = user.getId();
		UserData userData = UserData.create(userId, chatId);
		userData.setUserName(user.getUserName());
		userData.setFirstName(user.getFirstName());
		userData.setLastName(user.getLastName());
		inner.put(userId, userData);
	}

	public synchronized void subscribeUser(long userId) {
		UserData userData = inner.get(userId);

		if (userData != null) {
			userData.setReader(true);
			inner.put(userId, userData);
		}
	}

	public synchronized void unsubscribeUser(long userId) {
		UserData userData = inner.get(userId);

		if (userData != null) {
			userData.setReader(false);
			inner.put(userId, userData);
		}
	}

	public synchronized boolean isValidUser(long userId) {
		return inner.containsKey(userId);
	}

	public synchronized boolean isReader(long userId) {
		UserData userData = inner.get(userId);

		return userData != null && userData.isReader();
	}

	public synchronized boolean isWriter(long userId) {
		UserData userData = inner.get(userId);

		return userData != null && (userId == creatorId || userData.isWriter());
	}

	public synchronized boolean isAdmin(long userId) {
		UserData userData = inner.get(userId);

		return userData != null && (userId == creatorId || userData.isAdmin());
	}

	public synchronized List<Long> allReaders() {
		List<Long> result = new ArrayList<>();

		for (UserData userData : inner.values()) {
			if (userData.isReader()) {
				result.add(userData.getChatId());
			}
		}

		return result;
	}

	public synchronized void addAdmins(Set<String> userNames) {
		for (UserData userData : inner.values()) {
			String userName = userData.getUserName();

			if (userName != null && userNames.contains(userName.toLowerCase())) {
				long userId = userData.getUserId();

				userData.setAdmin(true);
				inner.put(userId, userData);
			}
		}
	}

	public synchronized void removeAdmins(Set<String> userNames) {
		for (UserData userData : inner.values()) {
			String userName = userData.getUserName();

			if (userName != null && userNames.contains(userName.toLowerCase())) {
				long userId = userData.getUserId();

				userData.setAdmin(false);
				inner.put(userId, userData);
			}
		}
	}

	public synchronized void addWriters(Set<String> userNames) {
		for (UserData userData : inner.values()) {
			String userName = userData.getUserName();

			if (userName != null && userNames.contains(userName.toLowerCase())) {
				long userId = userData.getUserId();

				userData.setWriter(true);
				inner.put(userId, userData);
			}
		}
	}

	public synchronized void removeWriters(Set<String> userNames) {
		for (UserData userData : inner.values()) {
			String userName = userData.getUserName();

			if (userName != null && userNames.contains(userName.toLowerCase())) {
				long userId = userData.getUserId();

				userData.setWriter(false);
				inner.put(userId, userData);
			}
		}
	}

	public synchronized Set<String> getUserNames() {
		Set<String> result = new TreeSet<>();

		for (UserData userData : inner.values()) {
			String userName = userData.getUserName();

			if (userName != null) {
				result.add(userName);
			}
		}

		return result;
	}

	public synchronized Set<String> getWriters() {
		Set<String> result = new TreeSet<>();

		for (UserData userData : inner.values()) {
			String userName = userData.getUserName();

			if (userName != null && userData.isWriter()) {
				result.add(userName);
			}
		}

		return result;
	}

	public synchronized Set<String> getAdmins() {
		Set<String> result = new TreeSet<>();

		for (UserData userData : inner.values()) {
			String userName = userData.getUserName();

			if (userName != null && userData.isAdmin()) {
				result.add(userName);
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return "UsersDAO [inner=" + inner + "]";
	}

	public static UsersDAO from(final long creatorId, final Map<Long, UserData> inner) {
		return new UsersDAO(creatorId, inner);
	}

}
