package ru.itis.messaging_engine.qrcode;

import ru.itis.messaging_engine.api.qrcode.QrCodeClassifier;

import dagger.Module;
import dagger.Provides;

@Module
public class QrCodeModule {

	@Provides
	QrCodeClassifier provideQrCodeClassifier(
			QrCodeClassifierImpl qrCodeClassifier) {
		return qrCodeClassifier;
	}
}
