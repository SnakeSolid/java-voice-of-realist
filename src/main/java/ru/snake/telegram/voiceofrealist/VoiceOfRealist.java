package ru.snake.telegram.voiceofrealist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.bots.DefaultBotOptions;

public class VoiceOfRealist extends AbilityBot {

	private static final Logger LOG = LoggerFactory.getLogger(VoiceOfRealist.class);

	private final MessageHandler messageHandler;

	private final long creatorId;

	public VoiceOfRealist(
		final long creatorId,
		final String botToken,
		final String botUserName,
		final DefaultBotOptions options
	) {
		super(botToken, botUserName, options);

		this.messageHandler = new MessageHandler(sender, db, creatorId);
		this.creatorId = creatorId;
	}

	public Ability replyToDefault() {
		return Ability.builder()
			.name("default")
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToDefault)
			.build();
	}

	public Ability replyToStart() {
		return Ability.builder()
			.name("start")
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToStart)
			.build();
	}

	public Ability replyToSubscribe() {
		return Ability.builder()
			.name("subscribe")
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToSubscribe)
			.build();
	}

	public Ability replyToUnsubscribe() {
		return Ability.builder()
			.name("unsubscribe")
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToUnsubscribe)
			.build();
	}

	public Ability replyToState() {
		return Ability.builder()
			.name("state")
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToStatus)
			.build();
	}

	public Ability replyToPublish() {
		return Ability.builder()
			.name("publish")
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToPublish)
			.build();
	}

	@Override
	public long creatorId() {
		return creatorId;
	}

}
