package ru.itis.meshy.api.sharing;

import ru.itis.messaging_engine.api.Nameable;
import ru.itis.messaging_engine.api.sync.GroupId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Shareable extends Nameable {

	GroupId getId();

}
