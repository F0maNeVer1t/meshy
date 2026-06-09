package ru.itis.messaging_engine.keyagreement;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.Pair;
import ru.itis.messaging_engine.api.UnsupportedVersionException;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.data.BdfReader;
import ru.itis.messaging_engine.api.data.BdfReaderFactory;
import ru.itis.messaging_engine.api.keyagreement.Payload;
import ru.itis.messaging_engine.api.keyagreement.PayloadParser;
import ru.itis.messaging_engine.api.keyagreement.TransportDescriptor;
import ru.itis.messaging_engine.api.plugin.BluetoothConstants;
import ru.itis.messaging_engine.api.plugin.LanTcpConstants;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.qrcode.QrCodeClassifier;
import ru.itis.messaging_engine.api.qrcode.QrCodeClassifier.QrCodeType;
import ru.itis.messaging_engine.api.qrcode.WrongQrCodeTypeException;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.api.keyagreement.KeyAgreementConstants.COMMIT_LENGTH;
import static ru.itis.messaging_engine.api.keyagreement.KeyAgreementConstants.QR_FORMAT_VERSION;
import static ru.itis.messaging_engine.api.keyagreement.KeyAgreementConstants.TRANSPORT_ID_BLUETOOTH;
import static ru.itis.messaging_engine.api.keyagreement.KeyAgreementConstants.TRANSPORT_ID_LAN;
import static ru.itis.messaging_engine.api.qrcode.QrCodeClassifier.QrCodeType.BQP;
import static ru.itis.messaging_engine.util.StringUtils.ISO_8859_1;

@Immutable
@NotNullByDefault
class PayloadParserImpl implements PayloadParser {

	private final BdfReaderFactory bdfReaderFactory;
	private final QrCodeClassifier qrCodeClassifier;

	@Inject
	PayloadParserImpl(BdfReaderFactory bdfReaderFactory,
			QrCodeClassifier qrCodeClassifier) {
		this.bdfReaderFactory = bdfReaderFactory;
		this.qrCodeClassifier = qrCodeClassifier;
	}

	@Override
	public Payload parse(String payloadString) throws IOException {
		Pair<QrCodeType, Integer> typeAndVersion =
				qrCodeClassifier.classifyQrCode(payloadString);
		QrCodeType qrCodeType = typeAndVersion.getFirst();
		if (qrCodeType != BQP) throw new WrongQrCodeTypeException(qrCodeType);
		int formatVersion = typeAndVersion.getSecond();
		if (formatVersion != QR_FORMAT_VERSION) {
			boolean tooOld = formatVersion < QR_FORMAT_VERSION;
			throw new UnsupportedVersionException(tooOld);
		}
		byte[] raw = payloadString.getBytes(ISO_8859_1);
		ByteArrayInputStream in = new ByteArrayInputStream(raw);
		// First byte: the format identifier and version (already parsed)
		if (in.read() == -1) throw new AssertionError();
		// The rest of the payload is a BDF list with one or more elements
		BdfReader r = bdfReaderFactory.createReader(in);
		BdfList payload = r.readList();
		if (payload.isEmpty()) throw new FormatException();
		if (!r.eof()) throw new FormatException();
		// First element: the public key commitment
		byte[] commitment = payload.getRaw(0);
		if (commitment.length != COMMIT_LENGTH) throw new FormatException();
		// Remaining elements: transport descriptors
		List<TransportDescriptor> recognised = new ArrayList<>();
		for (int i = 1; i < payload.size(); i++) {
			BdfList descriptor = payload.getList(i);
			int transportId = descriptor.getInt(0);
			if (transportId == TRANSPORT_ID_BLUETOOTH) {
				TransportId id = BluetoothConstants.ID;
				recognised.add(new TransportDescriptor(id, descriptor));
			} else if (transportId == TRANSPORT_ID_LAN) {
				TransportId id = LanTcpConstants.ID;
				recognised.add(new TransportDescriptor(id, descriptor));
			}
		}
		return new Payload(commitment, recognised);
	}
}
