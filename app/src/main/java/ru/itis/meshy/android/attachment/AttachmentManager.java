package ru.itis.meshy.android.attachment;

import android.net.Uri;

import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;

import ru.itis.meshy.api.attachment.AttachmentHeader;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.List;

@UiThread
@NotNullByDefault
public interface AttachmentManager {

	LiveData<AttachmentResult> storeAttachments(Collection<Uri> uri,
			boolean restart);

	List<AttachmentHeader> getAttachmentHeadersForSending();

	void cancel();

}
