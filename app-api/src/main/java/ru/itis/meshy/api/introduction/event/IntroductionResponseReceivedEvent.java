package ru.itis.meshy.api.introduction.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.meshy.api.conversation.event.ConversationMessageReceivedEvent;
import ru.itis.meshy.api.introduction.IntroductionResponse;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class IntroductionResponseReceivedEvent extends
		ConversationMessageReceivedEvent<IntroductionResponse> {

	public IntroductionResponseReceivedEvent(
			IntroductionResponse introductionResponse, ContactId contactId) {
		super(introductionResponse, contactId);
	}

}
