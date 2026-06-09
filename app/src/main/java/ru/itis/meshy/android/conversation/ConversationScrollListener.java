package ru.itis.meshy.android.conversation;

import ru.itis.meshy.android.view.MeshyRecyclerViewScrollListener;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
class ConversationScrollListener extends
        MeshyRecyclerViewScrollListener<ConversationAdapter, ConversationItem> {

	private final ConversationViewModel viewModel;

	protected ConversationScrollListener(ConversationAdapter adapter,
			ConversationViewModel viewModel) {
		super(adapter);
		this.viewModel = viewModel;
	}

	@Override
	protected void onItemVisible(ConversationItem item) {
		if (!item.isRead()) {
			viewModel.markMessageRead(item.getGroupId(), item.getId());
			item.markRead();
		}
	}

}
