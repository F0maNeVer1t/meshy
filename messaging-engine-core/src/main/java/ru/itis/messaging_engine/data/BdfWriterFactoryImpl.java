package ru.itis.messaging_engine.data;

import ru.itis.messaging_engine.api.data.BdfWriter;
import ru.itis.messaging_engine.api.data.BdfWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.OutputStream;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class BdfWriterFactoryImpl implements BdfWriterFactory {

	@Override
	public BdfWriter createWriter(OutputStream out) {
		return new BdfWriterImpl(out);
	}
}
