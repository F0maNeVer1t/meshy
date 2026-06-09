package ru.itis.meshy.sharing;

import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.client.ContactGroupFactory;
import ru.itis.messaging_engine.api.data.MetadataParser;
import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.sync.ClientId;
import ru.itis.messaging_engine.api.versioning.ClientVersioningManager;
import ru.itis.meshy.api.client.MessageTracker;
import ru.itis.meshy.api.forum.Forum;
import ru.itis.meshy.api.forum.ForumInvitationResponse;
import ru.itis.meshy.api.forum.ForumManager;
import ru.itis.meshy.api.forum.ForumManager.RemoveForumHook;
import ru.itis.meshy.api.forum.ForumSharingManager;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.inject.Inject;

@NotNullByDefault
class ForumSharingManagerImpl extends SharingManagerImpl<Forum>
		implements ForumSharingManager, RemoveForumHook {

	@Inject
	ForumSharingManagerImpl(DatabaseComponent db, ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MetadataParser metadataParser, MessageParser<Forum> messageParser,
			SessionEncoder sessionEncoder, SessionParser sessionParser,
			MessageTracker messageTracker,
			ContactGroupFactory contactGroupFactory,
			ProtocolEngine<Forum> engine,
			InvitationFactory<Forum, ForumInvitationResponse> invitationFactory) {
		super(db, clientHelper, clientVersioningManager, metadataParser,
				messageParser, sessionEncoder, sessionParser, messageTracker,
				contactGroupFactory, engine, invitationFactory);
	}

	@Override
	protected ClientId getClientId() {
		return CLIENT_ID;
	}

	@Override
	protected int getMajorVersion() {
		return MAJOR_VERSION;
	}

	@Override
	protected ClientId getShareableClientId() {
		return ForumManager.CLIENT_ID;
	}

	@Override
	protected int getShareableMajorVersion() {
		return ForumManager.MAJOR_VERSION;
	}

	@Override
	public void removingForum(Transaction txn, Forum f) throws DbException {
		removingShareable(txn, f);
	}

}
