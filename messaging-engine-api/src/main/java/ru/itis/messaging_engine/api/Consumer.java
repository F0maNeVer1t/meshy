package ru.itis.messaging_engine.api;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Consumer<T> {

	void accept(T t);
}
