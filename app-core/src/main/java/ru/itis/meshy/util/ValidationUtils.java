package ru.itis.meshy.util;

import ru.itis.messaging_engine.api.FormatException;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static ru.itis.messaging_engine.util.ValidationUtils.checkRange;
import static ru.itis.meshy.api.autodelete.AutoDeleteConstants.MAX_AUTO_DELETE_TIMER_MS;
import static ru.itis.meshy.api.autodelete.AutoDeleteConstants.MIN_AUTO_DELETE_TIMER_MS;
import static ru.itis.meshy.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;

@Immutable
@NotNullByDefault
public class ValidationUtils {

	public static long validateAutoDeleteTimer(@Nullable Long timer)
			throws FormatException {
		if (timer == null) return NO_AUTO_DELETE_TIMER;
		checkRange(timer, MIN_AUTO_DELETE_TIMER_MS, MAX_AUTO_DELETE_TIMER_MS);
		return timer;
	}
}
