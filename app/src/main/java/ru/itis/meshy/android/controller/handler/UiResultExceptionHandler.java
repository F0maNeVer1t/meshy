package ru.itis.meshy.android.controller.handler;

import androidx.annotation.UiThread;

import ru.itis.meshy.android.DestroyableContext;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public abstract class UiResultExceptionHandler<R, E extends Exception>
		extends UiExceptionHandler<E> implements ResultExceptionHandler<R, E> {

	protected UiResultExceptionHandler(DestroyableContext listener) {
		super(listener);
	}

	@Override
	public void onResult(R result) {
		listener.runOnUiThreadUnlessDestroyed(() -> onResultUi(result));
	}

	@UiThread
	public abstract void onResultUi(R result);

}
