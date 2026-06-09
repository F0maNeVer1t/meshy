package ru.itis.meshy.android.conversation;

import android.view.View;

import androidx.annotation.UiThread;

import ru.itis.meshy.android.attachment.AttachmentItem;
import org.briarproject.nullsafety.NotNullByDefault;

@UiThread
@NotNullByDefault
interface ConversationListener {

	void respondToRequest(ConversationRequestItem item, boolean accept);

	void openRequestedShareable(ConversationRequestItem item);

	void onAttachmentClicked(View view, ConversationMessageItem messageItem,
			AttachmentItem attachmentItem);

	void onAutoDeleteTimerNoticeClicked();

	void onLinkClick(String url);

}
