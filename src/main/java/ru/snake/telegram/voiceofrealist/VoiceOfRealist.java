package ru.snake.telegram.voiceofrealist;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.bots.DefaultBotOptions;

public class VoiceOfRealist extends AbilityBot {

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
			.name(Constants.SUBSCRIBE)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToSubscribe)
			.build();
	}

	public Ability replyToUnsubscribe() {
		return Ability.builder()
			.name(Constants.UNSUBSCRIBE)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToUnsubscribe)
			.build();
	}

	public Ability replyToStatus() {
		return Ability.builder()
			.name(Constants.STATE)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToStatus)
			.build();
	}

	public Ability replyToListUsers() {
		return Ability.builder()
			.name(Constants.LIST_USERS)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToListUsers)
			.build();
	}

	public Ability replyToListWriters() {
		return Ability.builder()
			.name(Constants.LIST_WRITERS)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToListWriters)
			.build();
	}

	public Ability replyToListAdmins() {
		return Ability.builder()
			.name(Constants.LIST_ADMINS)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToListAdmins)
			.build();
	}

	public Ability replyToAdminsAdd() {
		return Ability.builder()
			.name(Constants.ADMINS_ADD)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToAdminsAdd)
			.build();
	}

	public Ability replyToAdminsRemove() {
		return Ability.builder()
			.name(Constants.ADMINS_REMOVE)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToAdminsRemove)
			.build();
	}

	public Ability replyToWritersAdd() {
		return Ability.builder()
			.name(Constants.WRITERS_ADD)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToWritersAdd)
			.build();
	}

	public Ability replyToWritersRemove() {
		return Ability.builder()
			.name(Constants.WRITERS_REMOVE)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToWritersRemove)
			.build();
	}

	public Ability replyToPublish() {
		return Ability.builder()
			.name(Constants.PUBLISH)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToPublish)
			.build();
	}

	public Ability replyToSend() {
		return Ability.builder()
			.name(Constants.SEND)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToSend)
			.build();
	}

	public Ability replyToView() {
		return Ability.builder()
			.name(Constants.VIEW)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToView)
			.build();
	}

	public Ability replyToDelete() {
		return Ability.builder()
			.name(Constants.DELETE)
			.locality(Locality.USER)
			.privacy(Privacy.PUBLIC)
			.action(messageHandler::replyToDelete)
			.build();
	}

	@Override
	public long creatorId() {
		return creatorId;
	}

}
