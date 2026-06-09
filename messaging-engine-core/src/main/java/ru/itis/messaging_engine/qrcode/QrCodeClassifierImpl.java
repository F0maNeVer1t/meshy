package ru.itis.messaging_engine.qrcode;

import ru.itis.messaging_engine.api.Pair;
import ru.itis.messaging_engine.api.keyagreement.KeyAgreementConstants;
import ru.itis.messaging_engine.api.mailbox.MailboxConstants;
import ru.itis.messaging_engine.api.qrcode.QrCodeClassifier;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.api.qrcode.QrCodeClassifier.QrCodeType.BQP;
import static ru.itis.messaging_engine.api.qrcode.QrCodeClassifier.QrCodeType.MAILBOX;
import static ru.itis.messaging_engine.api.qrcode.QrCodeClassifier.QrCodeType.UNKNOWN;
import static ru.itis.messaging_engine.util.StringUtils.ISO_8859_1;

@Immutable
@NotNullByDefault
class QrCodeClassifierImpl implements QrCodeClassifier {

	@Inject
	QrCodeClassifierImpl() {
	}

	@Override
	public Pair<QrCodeType, Integer> classifyQrCode(String payload) {
		byte[] bytes = payload.getBytes(ISO_8859_1);
		if (bytes.length == 0) return new Pair<>(UNKNOWN, 0);
		// If this is a Bramble QR code then the first byte encodes the
		// format ID (3 bits) and version (5 bits)
		int formatIdAndVersion = bytes[0] & 0xFF;
		int formatId = formatIdAndVersion >> 5;
		int formatVersion = formatIdAndVersion & 0x1F;
		if (formatId == KeyAgreementConstants.QR_FORMAT_ID) {
			return new Pair<>(BQP, formatVersion);
		}
		if (formatId == MailboxConstants.QR_FORMAT_ID) {
			return new Pair<>(MAILBOX, formatVersion);
		}
		return new Pair<>(UNKNOWN, 0);
	}
}
