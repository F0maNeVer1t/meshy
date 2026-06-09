package ru.itis.messaging_engine.api.data;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.db.Metadata;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MetadataEncoder {

	Metadata encode(BdfDictionary d) throws FormatException;
}
