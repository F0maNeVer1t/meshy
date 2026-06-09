package ru.itis.meshy;

import ru.itis.meshy.attachment.AttachmentModule;
import ru.itis.meshy.autodelete.AutoDeleteModule;
import ru.itis.meshy.avatar.AvatarModule;
import ru.itis.meshy.blog.BlogModule;
import ru.itis.meshy.client.BriarClientModule;
import ru.itis.meshy.conversation.ConversationModule;
import ru.itis.meshy.feed.FeedModule;
import ru.itis.meshy.forum.ForumModule;
import ru.itis.meshy.identity.IdentityModule;
import ru.itis.meshy.introduction.IntroductionModule;
import ru.itis.meshy.messaging.MessagingModule;
import ru.itis.meshy.privategroup.PrivateGroupModule;
import ru.itis.meshy.privategroup.invitation.GroupInvitationModule;
import ru.itis.meshy.sharing.SharingModule;
import ru.itis.meshy.test.TestModule;

import dagger.Module;

@Module(includes = {
		AttachmentModule.class,
		AutoDeleteModule.class,
		AvatarModule.class,
		BlogModule.class,
		BriarClientModule.class,
		ConversationModule.class,
		FeedModule.class,
		ForumModule.class,
		GroupInvitationModule.class,
		IdentityModule.class,
		IntroductionModule.class,
		MessagingModule.class,
		PrivateGroupModule.class,
		SharingModule.class,
		TestModule.class
})
public class BriarCoreModule {
}
