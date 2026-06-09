package ru.itis.meshy.android.contactselection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.itis.meshy.R;
import ru.itis.meshy.android.contact.OnContactClickListener;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
class ContactSelectorAdapter extends
		BaseContactSelectorAdapter<SelectableContactItem, SelectableContactHolder> {

	ContactSelectorAdapter(Context context,
			OnContactClickListener<SelectableContactItem> listener) {
		super(context, SelectableContactItem.class, listener);
	}

	@Override
	public SelectableContactHolder onCreateViewHolder(ViewGroup viewGroup,
			int i) {
		View v = LayoutInflater.from(ctx).inflate(
				R.layout.list_item_selectable_contact, viewGroup, false);
		return new SelectableContactHolder(v);
	}

}
