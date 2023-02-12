package ru.snake.telegram.voiceofrealist;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws ParseException {
		LOG.info("Read settings...");

		Settings settings = Settings.from(args);
		Properties properties = loadProperties(settings);
		DefaultBotOptions options = new DefaultBotOptions();
		long creator = Long.parseLong(properties.getProperty("creator"));
		String token = properties.getProperty("token");
		String userName = properties.getProperty("user_name");
		VoiceOfRealist bot = new VoiceOfRealist(creator, token, userName, options);

		LOG.info("Starting...");

		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(bot);
		} catch (TelegramApiException e) {
			LOG.error("Failed to start bot", e);
		}
	}

	private static Properties loadProperties(Settings settings) {
		Properties properties = new Properties();

		try (InputStream stream = new FileInputStream(settings.getConfig())) {
			properties.load(stream);
		} catch (IOException e) {
			LOG.error("Failed to read settings", e);

			throw new RuntimeException("Failed to read settings", e);
		}

		return properties;
	}

}
