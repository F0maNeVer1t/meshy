package ru.itis.messaging_engine.api;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Predicate<T> {

	boolean test(T t);
}
