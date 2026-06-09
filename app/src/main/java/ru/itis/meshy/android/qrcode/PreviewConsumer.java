package ru.itis.meshy.android.qrcode;

import android.hardware.Camera;

import androidx.annotation.UiThread;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface PreviewConsumer {

	@UiThread
	void start(Camera camera, int cameraIndex);

	@UiThread
	void stop();
}
