package ru.itis.meshy.android.sharing;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.recyclerview.widget.RecyclerView;

import ru.itis.meshy.R;
import ru.itis.meshy.android.sharing.InvitationAdapter.InvitationClickListener;
import ru.itis.meshy.android.view.TextAvatarView;
import ru.itis.meshy.api.sharing.InvitationItem;

import javax.annotation.Nullable;

public class InvitationViewHolder<I extends InvitationItem>
		extends RecyclerView.ViewHolder {

	private final TextAvatarView avatar;
	private final TextView name;
	protected final TextView sharedBy;
	private final TextView subscribed;
	private final Button accept;
	private final Button decline;

	public InvitationViewHolder(View v) {
		super(v);

		avatar = v.findViewById(R.id.avatarView);
		name = v.findViewById(R.id.forumNameView);
		sharedBy = v.findViewById(R.id.sharedByView);
		subscribed = v.findViewById(R.id.forumSubscribedView);
		accept = v.findViewById(R.id.acceptButton);
		decline = v.findViewById(R.id.declineButton);
	}

	@CallSuper
	public void onBind(@Nullable I item, InvitationClickListener<I> listener) {
		if (item == null) return;

		avatar.setText(item.getShareable().getName().substring(0, 1));
		avatar.setBackgroundBytes(item.getShareable().getId().getBytes());

		name.setText(item.getShareable().getName());

		if (item.isSubscribed()) {
			subscribed.setVisibility(VISIBLE);
		} else {
			subscribed.setVisibility(GONE);
		}

		accept.setOnClickListener(v -> listener.onItemClick(item, true));
		decline.setOnClickListener(v -> listener.onItemClick(item, false));
	}

}