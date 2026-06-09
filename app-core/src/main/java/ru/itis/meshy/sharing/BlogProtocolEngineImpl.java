package ru.itis.meshy.sharing;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.api.versioning.ClientVersioningManager;
import ru.itis.meshy.api.autodelete.AutoDeleteManager;
import ru.itis.meshy.api.blog.Blog;
import ru.itis.meshy.api.blog.BlogInvitationResponse;
import ru.itis.meshy.api.blog.BlogManager;
import ru.itis.meshy.api.blog.BlogSharingManager;
import ru.itis.meshy.api.blog.event.BlogInvitationRequestReceivedEvent;
import ru.itis.meshy.api.blog.event.BlogInvitationResponseReceivedEvent;
import ru.itis.meshy.api.conversation.ConversationManager;
import ru.itis.meshy.api.conversation.ConversationRequest;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class BlogProtocolEngineImpl extends ProtocolEngineImpl<Blog> {

	private final BlogManager blogManager;
	private final InvitationFactory<Blog, BlogInvitationResponse>
			invitationFactory;

	@Inject
	BlogProtocolEngineImpl(
			DatabaseComponent db,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MessageEncoder messageEncoder,
			MessageParser<Blog> messageParser,
			AutoDeleteManager autoDeleteManager,
			ConversationManager conversationManager,
			Clock clock,
			BlogManager blogManager,
			InvitationFactory<Blog, BlogInvitationResponse> invitationFactory) {
		super(db, clientHelper, clientVersioningManager, messageEncoder,
				messageParser, autoDeleteManager,
				conversationManager, clock, BlogSharingManager.CLIENT_ID,
				BlogSharingManager.MAJOR_VERSION, BlogManager.CLIENT_ID,
				BlogManager.MAJOR_VERSION);
		this.blogManager = blogManager;
		this.invitationFactory = invitationFactory;
	}

	@Override
	Event getInvitationRequestReceivedEvent(InviteMessage<Blog> m,
			ContactId contactId, boolean available, boolean canBeOpened) {
		ConversationRequest<Blog> request = invitationFactory
				.createInvitationRequest(false, false, true, false, m,
						contactId, available, canBeOpened,
						m.getAutoDeleteTimer());
		return new BlogInvitationRequestReceivedEvent(request, contactId);
	}

	@Override
	Event getInvitationResponseReceivedEvent(AcceptMessage m,
			ContactId contactId) {
		BlogInvitationResponse response = invitationFactory
				.createInvitationResponse(m.getId(), m.getContactGroupId(),
						m.getTimestamp(), false, false, false, false,
						true, m.getShareableId(), m.getAutoDeleteTimer(),
						false);
		return new BlogInvitationResponseReceivedEvent(response, contactId);
	}

	@Override
	Event getInvitationResponseReceivedEvent(DeclineMessage m,
			ContactId contactId) {
		BlogInvitationResponse response = invitationFactory
				.createInvitationResponse(m.getId(), m.getContactGroupId(),
						m.getTimestamp(), false, false, false, false,
						false, m.getShareableId(), m.getAutoDeleteTimer(),
						false);
		return new BlogInvitationResponseReceivedEvent(response, contactId);
	}

	@Override
	Event getAutoDeclineInvitationResponseReceivedEvent(Session s, Message m,
			ContactId contactId, long timer) {
		BlogInvitationResponse response = invitationFactory
				.createInvitationResponse(m.getId(), s.getContactGroupId(),
						m.getTimestamp(), true, false, false, true,
						false, s.getShareableId(), timer, true);
		return new BlogInvitationResponseReceivedEvent(response, contactId);
	}

	@Override
	protected void addShareable(Transaction txn, MessageId inviteId)
			throws DbException, FormatException {
		InviteMessage<Blog> invite =
				messageParser.getInviteMessage(txn, inviteId);
		blogManager.addBlog(txn, invite.getShareable());
	}

}
