package ru.itis.meshy.sharing;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.client.ContactGroupFactory;
import ru.itis.messaging_engine.api.contact.Contact;
import ru.itis.messaging_engine.api.data.MetadataParser;
import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.identity.IdentityManager;
import ru.itis.messaging_engine.api.identity.LocalAuthor;
import ru.itis.messaging_engine.api.sync.ClientId;
import ru.itis.messaging_engine.api.versioning.ClientVersioningManager;
import ru.itis.meshy.api.blog.Blog;
import ru.itis.meshy.api.blog.BlogInvitationResponse;
import ru.itis.meshy.api.blog.BlogManager;
import ru.itis.meshy.api.blog.BlogManager.RemoveBlogHook;
import ru.itis.meshy.api.blog.BlogSharingManager;
import ru.itis.meshy.api.client.MessageTracker;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class BlogSharingManagerImpl extends SharingManagerImpl<Blog>
		implements BlogSharingManager, RemoveBlogHook {

	private final IdentityManager identityManager;
	private final BlogManager blogManager;

	@Inject
	BlogSharingManagerImpl(DatabaseComponent db, ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MetadataParser metadataParser, MessageParser<Blog> messageParser,
			SessionEncoder sessionEncoder, SessionParser sessionParser,
			MessageTracker messageTracker,
			ContactGroupFactory contactGroupFactory,
			ProtocolEngine<Blog> engine,
			InvitationFactory<Blog, BlogInvitationResponse> invitationFactory,
			IdentityManager identityManager, BlogManager blogManager) {
		super(db, clientHelper, clientVersioningManager, metadataParser,
				messageParser, sessionEncoder, sessionParser, messageTracker,
				contactGroupFactory, engine, invitationFactory);
		this.identityManager = identityManager;
		this.blogManager = blogManager;
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
		return BlogManager.CLIENT_ID;
	}

	@Override
	protected int getShareableMajorVersion() {
		return BlogManager.MAJOR_VERSION;
	}

	@Override
	public void addingContact(Transaction txn, Contact c) throws DbException {
		// Create a group to share with the contact
		super.addingContact(txn, c);

		// Get our blog and that of the contact
		LocalAuthor localAuthor = identityManager.getLocalAuthor(txn);
		Blog ourBlog = blogManager.getPersonalBlog(localAuthor);
		Blog theirBlog = blogManager.getPersonalBlog(c.getAuthor());

		// Pre-share both blogs, if they have not been shared already
		try {
			preShareGroup(txn, c, ourBlog.getGroup());
			preShareGroup(txn, c, theirBlog.getGroup());
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public void removingBlog(Transaction txn, Blog b) throws DbException {
		removingShareable(txn, b);
	}

}
