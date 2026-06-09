package ru.itis.meshy.android.conversation;

import androidx.annotation.LayoutRes;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;

import ru.itis.meshy.android.attachment.AttachmentItem;
import ru.itis.meshy.api.messaging.PrivateMessageHeader;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@NotNullByDefault
class ConversationMessageItem extends ConversationItem {

	private final List<AttachmentItem> attachments;

	ConversationMessageItem(@LayoutRes int layoutRes, PrivateMessageHeader h,
			LiveData<String> contactName, List<AttachmentItem> attachments) {
		super(layoutRes, h, contactName);
		this.attachments = attachments;
	}

	List<AttachmentItem> getAttachments() {
		return attachments;
	}

	@UiThread
	boolean updateAttachments(AttachmentItem item) {
		int pos = attachments.indexOf(item);
		if (pos != -1 && attachments.get(pos).getState() != item.getState()) {
			attachments.set(pos, item);
			return true;
		}
		return false;
	}

}
