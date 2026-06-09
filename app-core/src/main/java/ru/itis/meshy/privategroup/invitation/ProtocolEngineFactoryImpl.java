package ru.itis.meshy.privategroup.invitation;

import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.identity.IdentityManager;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.api.versioning.ClientVersioningManager;
import ru.itis.meshy.api.autodelete.AutoDeleteManager;
import ru.itis.meshy.api.conversation.ConversationManager;
import ru.itis.meshy.api.privategroup.GroupMessageFactory;
import ru.itis.meshy.api.privategroup.PrivateGroupFactory;
import ru.itis.meshy.api.privategroup.PrivateGroupManager;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class ProtocolEngineFactoryImpl implements ProtocolEngineFactory {

	private final DatabaseComponent db;
	private final ClientHelper clientHelper;
	private final ClientVersioningManager clientVersioningManager;
	private final PrivateGroupManager privateGroupManager;
	private final PrivateGroupFactory privateGroupFactory;
	private final GroupMessageFactory groupMessageFactory;
	private final IdentityManager identityManager;
	private final MessageParser messageParser;
	private final MessageEncoder messageEncoder;
	private final AutoDeleteManager autoDeleteManager;
	private final ConversationManager conversationManager;
	private final Clock clock;

	@Inject
	ProtocolEngineFactoryImpl(
			DatabaseComponent db,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			PrivateGroupManager privateGroupManager,
			PrivateGroupFactory privateGroupFactory,
			GroupMessageFactory groupMessageFactory,
			IdentityManager identityManager,
			MessageParser messageParser,
			MessageEncoder messageEncoder,
			AutoDeleteManager autoDeleteManager,
			ConversationManager conversationManager,
			Clock clock) {
		this.db = db;
		this.clientHelper = clientHelper;
		this.clientVersioningManager = clientVersioningManager;
		this.privateGroupManager = privateGroupManager;
		this.privateGroupFactory = privateGroupFactory;
		this.groupMessageFactory = groupMessageFactory;
		this.identityManager = identityManager;
		this.messageParser = messageParser;
		this.messageEncoder = messageEncoder;
		this.autoDeleteManager = autoDeleteManager;
		this.conversationManager = conversationManager;
		this.clock = clock;
	}

	@Override
	public ProtocolEngine<CreatorSession> createCreatorEngine() {
		return new CreatorProtocolEngine(db, clientHelper,
				clientVersioningManager, privateGroupManager,
				privateGroupFactory, groupMessageFactory, identityManager,
				messageParser, messageEncoder,
				autoDeleteManager, conversationManager, clock);
	}

	@Override
	public ProtocolEngine<InviteeSession> createInviteeEngine() {
		return new InviteeProtocolEngine(db, clientHelper,
				clientVersioningManager, privateGroupManager,
				privateGroupFactory, groupMessageFactory, identityManager,
				messageParser, messageEncoder,
				autoDeleteManager, conversationManager, clock);
	}

	@Override
	public ProtocolEngine<PeerSession> createPeerEngine() {
		return new PeerProtocolEngine(db, clientHelper,
				clientVersioningManager, privateGroupManager,
				privateGroupFactory, groupMessageFactory, identityManager,
				messageParser, messageEncoder,
				autoDeleteManager, conversationManager, clock);
	}
}
