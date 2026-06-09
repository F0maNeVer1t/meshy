package ru.itis.meshy;

import ru.itis.meshy.autodelete.AutoDeleteModule;
import ru.itis.meshy.avatar.AvatarModule;
import ru.itis.meshy.blog.BlogModule;
import ru.itis.meshy.conversation.ConversationModule;
import ru.itis.meshy.feed.FeedModule;
import ru.itis.meshy.forum.ForumModule;
import ru.itis.meshy.identity.IdentityModule;
import ru.itis.meshy.introduction.IntroductionModule;
import ru.itis.meshy.messaging.MessagingModule;
import ru.itis.meshy.privategroup.PrivateGroupModule;
import ru.itis.meshy.privategroup.invitation.GroupInvitationModule;
import ru.itis.meshy.sharing.SharingModule;

public interface BriarCoreEagerSingletons {

	void inject(AutoDeleteModule.EagerSingletons init);

	void inject(AvatarModule.EagerSingletons init);

	void inject(BlogModule.EagerSingletons init);

	void inject(ConversationModule.EagerSingletons init);

	void inject(FeedModule.EagerSingletons init);

	void inject(ForumModule.EagerSingletons init);

	void inject(GroupInvitationModule.EagerSingletons init);

	void inject(IdentityModule.EagerSingletons init);

	void inject(IntroductionModule.EagerSingletons init);

	void inject(MessagingModule.EagerSingletons init);

	void inject(PrivateGroupModule.EagerSingletons init);

	void inject(SharingModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(BriarCoreEagerSingletons c) {
			c.inject(new AutoDeleteModule.EagerSingletons());
			c.inject(new AvatarModule.EagerSingletons());
			c.inject(new BlogModule.EagerSingletons());
			c.inject(new ConversationModule.EagerSingletons());
			c.inject(new FeedModule.EagerSingletons());
			c.inject(new ForumModule.EagerSingletons());
			c.inject(new GroupInvitationModule.EagerSingletons());
			c.inject(new MessagingModule.EagerSingletons());
			c.inject(new PrivateGroupModule.EagerSingletons());
			c.inject(new SharingModule.EagerSingletons());
			c.inject(new IdentityModule.EagerSingletons());
			c.inject(new IntroductionModule.EagerSingletons());
		}
	}
}
