package ru.itis.meshy.android.conversation.glide

import android.graphics.Bitmap
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop

/**
 * Композиция двух трансформаций для изображений в conversation:
 * сначала [CenterCrop], затем [CustomCornersTransformation] с заданными
 * радиусами. Бывший `BriarImageTransformation`.
 */
class MeshyImageTransformation(r: Radii) :
    MultiTransformation<Bitmap>(CenterCrop(), CustomCornersTransformation(r))