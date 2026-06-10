package ru.itis.meshy.api.messaging.priority;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MessagePriorityClassifier {

	MessagePriority classify(String text);
}
