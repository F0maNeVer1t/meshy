package ru.itis.meshy.android.sharing;

import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.meshy.android.controller.ActivityLifecycleController;
import ru.itis.meshy.android.controller.handler.ExceptionHandler;
import ru.itis.meshy.android.controller.handler.ResultExceptionHandler;
import ru.itis.meshy.api.sharing.InvitationItem;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;

@NotNullByDefault
public interface InvitationController<I extends InvitationItem>
		extends ActivityLifecycleController {

	void loadInvitations(boolean clear,
			ResultExceptionHandler<Collection<I>, DbException> handler);

	void respondToInvitation(I item, boolean accept,
			ExceptionHandler<DbException> handler);

	interface InvitationListener {

		void loadInvitations(boolean clear);

	}

}
