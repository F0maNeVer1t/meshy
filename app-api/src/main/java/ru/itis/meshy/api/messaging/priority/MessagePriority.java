package ru.itis.meshy.api.messaging.priority;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public enum MessagePriority {

	STANDARD((byte) 0),
	EMERGENCY((byte) 1);

	private final byte value;

	MessagePriority(byte value) {
		this.value = value;
	}

	public byte getValue() {
		return value;
	}

	public static MessagePriority fromByte(byte b) {
		for (MessagePriority p : values()) {
			if (p.value == b) return p;
		}
		throw new IllegalArgumentException("Unknown priority: " + b);
	}
}
