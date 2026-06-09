package ru.itis.messaging_engine.sync;

interface ThrowingRunnable<T extends Throwable> {

	void run() throws T;
}
