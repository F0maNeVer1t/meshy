package ru.itis.messaging_engine.api.properties;

import ru.itis.messaging_engine.api.StringMap;

import java.util.Map;

public class TransportProperties extends StringMap {

	public TransportProperties(Map<String, String> m) {
		super(m);
	}

	public TransportProperties() {
		super();
	}
}
