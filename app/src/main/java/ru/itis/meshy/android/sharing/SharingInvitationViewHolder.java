package ru.itis.meshy.android.sharing;

import static ru.itis.meshy.android.util.UiUtils.getContactDisplayName;

import android.view.View;

import ru.itis.messaging_engine.api.contact.Contact;
import ru.itis.messaging_engine.util.StringUtils;
import ru.itis.meshy.R;
import ru.itis.meshy.android.sharing.InvitationAdapter.InvitationClickListener;
import ru.itis.meshy.api.sharing.SharingInvitationItem;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

class SharingInvitationViewHolder
		extends InvitationViewHolder<SharingInvitationItem> {

	SharingInvitationViewHolder(View v) {
		super(v);
	}

	@Override
	public void onBind(@Nullable SharingInvitationItem item,
			InvitationClickListener<SharingInvitationItem> listener) {
		super.onBind(item, listener);
		if (item == null) return;

		Collection<String> names = new ArrayList<>();
		for (Contact c : item.getNewSharers())
			names.add(getContactDisplayName(c));
		sharedBy.setText(
				sharedBy.getContext().getString(R.string.shared_by_format,
						StringUtils.join(names, ", ")));
	}

}
