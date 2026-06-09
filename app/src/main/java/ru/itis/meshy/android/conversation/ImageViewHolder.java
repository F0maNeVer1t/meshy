package ru.itis.meshy.android.conversation;

import static android.widget.ImageView.ScaleType.CENTER_CROP;
import static android.widget.ImageView.ScaleType.FIT_CENTER;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.NONE;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static ru.itis.meshy.android.attachment.AttachmentItem.State.AVAILABLE;
import static ru.itis.meshy.android.attachment.AttachmentItem.State.ERROR;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.StaggeredGridLayoutManager.LayoutParams;

import com.bumptech.glide.load.Transformation;

import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.R;
import ru.itis.meshy.android.attachment.AttachmentItem;
import ru.itis.meshy.android.conversation.glide.MeshyImageTransformation;
import ru.itis.meshy.android.conversation.glide.GlideApp;
import ru.itis.meshy.android.conversation.glide.Radii;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
class ImageViewHolder extends ViewHolder {

	@DrawableRes
	private static final int ERROR_RES = R.drawable.ic_image_broken;

	protected final ImageView imageView;
	private final int imageSize;
	private final MessageId conversationItemId;

	ImageViewHolder(View v, int imageSize, MessageId conversationItemId) {
		super(v);
		imageView = v.findViewById(R.id.imageView);
		this.imageSize = imageSize;
		this.conversationItemId = conversationItemId;
	}

	void bind(AttachmentItem attachment, Radii r, boolean single,
			boolean needsStretch) {
		setImageViewDimensions(attachment, single, needsStretch);
		if (attachment.getState() != AVAILABLE) {
			GlideApp.with(imageView).clear(imageView);
			if (attachment.getState() == ERROR) {
				imageView.setImageResource(ERROR_RES);
			} else {
				imageView.setImageResource(R.drawable.ic_image_missing);
			}
			imageView.setScaleType(FIT_CENTER);
		} else {
			loadImage(attachment, r);
			imageView.setScaleType(CENTER_CROP);
		}
		imageView.setTransitionName(
				attachment.getTransitionName(conversationItemId));
	}

	private void setImageViewDimensions(AttachmentItem a, boolean single,
			boolean needsStretch) {
		LayoutParams params = (LayoutParams) imageView.getLayoutParams();
		int width = needsStretch ? imageSize * 2 : imageSize;
		params.width = single ? a.getThumbnailWidth() : width;
		params.height = single ? a.getThumbnailHeight() : imageSize;
		params.setFullSpan(!single && needsStretch);
		imageView.setLayoutParams(params);
	}

	private void loadImage(AttachmentItem a, Radii r) {
		Transformation<Bitmap> transformation = new MeshyImageTransformation(r);
		GlideApp.with(imageView)
				.load(a.getHeader())
				.diskCacheStrategy(NONE)
				.error(ERROR_RES)
				.transform(transformation)
				.transition(withCrossFade())
				.into(imageView)
				.waitForLayout();
	}

}
