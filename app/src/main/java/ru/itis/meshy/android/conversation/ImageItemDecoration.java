package ru.itis.meshy.android.conversation;

import static ru.itis.meshy.android.conversation.ImageAdapter.isBottomRow;
import static ru.itis.meshy.android.conversation.ImageAdapter.isLeft;
import static ru.itis.meshy.android.conversation.ImageAdapter.isTopRow;
import static ru.itis.meshy.android.conversation.ImageAdapter.singleInRow;
import static ru.itis.meshy.android.util.UiUtils.isRtl;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.State;

import ru.itis.meshy.R;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
class ImageItemDecoration extends ItemDecoration {

	private final int border;
	private final boolean isRtl;

	ImageItemDecoration(Context ctx) {
		Resources res = ctx.getResources();

		// for pixel perfection, add a pixel to the border if it has an odd size
		int b = res.getDimensionPixelSize(R.dimen.message_bubble_border);
		int realBorderSize = b % 2 == 0 ? b : b + 1;

		// we are applying half the border around the insides of each image
		// to prevent differently sized images looking slightly broken
		border = realBorderSize / 2;

		// find out if we are showing a RTL language
		isRtl = isRtl(ctx);
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
			State state) {
		if (state.getItemCount() == 1) return;
		int pos = parent.getChildAdapterPosition(view);
		int num = state.getItemCount();
		boolean start = isLeft(pos) ^ isRtl;
		outRect.top = isTopRow(pos) ? 0 : border;
		outRect.left = start ? 0 : border;
		outRect.right = start && !singleInRow(pos, num) ? border : 0;
		outRect.bottom = isBottomRow(pos, num) ? 0 : border;
	}

}
