package ru.itis.meshy.api.conversation;

import ru.itis.meshy.api.blog.BlogInvitationRequest;
import ru.itis.meshy.api.blog.BlogInvitationResponse;
import ru.itis.meshy.api.forum.ForumInvitationRequest;
import ru.itis.meshy.api.forum.ForumInvitationResponse;
import ru.itis.meshy.api.introduction.IntroductionRequest;
import ru.itis.meshy.api.introduction.IntroductionResponse;
import ru.itis.meshy.api.messaging.PrivateMessageHeader;
import ru.itis.meshy.api.privategroup.invitation.GroupInvitationRequest;
import ru.itis.meshy.api.privategroup.invitation.GroupInvitationResponse;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface ConversationMessageVisitor<T> {

	T visitPrivateMessageHeader(PrivateMessageHeader h);

	T visitBlogInvitationRequest(BlogInvitationRequest r);

	T visitBlogInvitationResponse(BlogInvitationResponse r);

	T visitForumInvitationRequest(ForumInvitationRequest r);

	T visitForumInvitationResponse(ForumInvitationResponse r);

	T visitGroupInvitationRequest(GroupInvitationRequest r);

	T visitGroupInvitationResponse(GroupInvitationResponse r);

	T visitIntroductionRequest(IntroductionRequest r);

	T visitIntroductionResponse(IntroductionResponse r);
}
