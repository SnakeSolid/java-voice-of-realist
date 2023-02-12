package ru.snake.telegram.voiceofrealist;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

	private static final Pattern PATTERN = Pattern.compile("[a-z0-9]+");

	private TextUtils() {
	}

	public static Set<String> extractUserNames(String text) {
		Matcher matcher = PATTERN.matcher(text);
		Set<String> result = new HashSet<>();

		while (matcher.find()) {
			String login = matcher.group();

			result.add(login);
		}

		return result;
	}

}
