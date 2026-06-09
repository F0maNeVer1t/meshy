package ru.itis.messaging_engine.plugin.file;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.plugin.file.RemovableDriveTask;
import ru.itis.messaging_engine.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface RemovableDriveTaskFactory {

	RemovableDriveTask createReader(RemovableDriveTaskRegistry registry,
			TransportProperties p);

	RemovableDriveTask createWriter(RemovableDriveTaskRegistry registry,
			ContactId c, TransportProperties p);
}
