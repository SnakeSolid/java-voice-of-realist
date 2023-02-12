package ru.snake.telegram.voiceofrealist;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Settings {

	private final String config;

	public Settings(String config) {
		this.config = config;
	}

	/**
	 * @return the config
	 */
	public String getConfig() {
		return config;
	}

	public static Settings from(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption(Option.builder("c").longOpt("config").hasArg().argName("PATH").required().build());
		CommandLineParser parser = new DefaultParser();
		CommandLine result = parser.parse(options, args);
		String config = result.getOptionValue('c');

		return new Settings(config);
	}

}
