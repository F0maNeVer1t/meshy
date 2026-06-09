package ru.itis.meshy.android.login;

import static ru.itis.messaging_engine.api.crypto.DecryptionResult.SUCCESS;

import androidx.lifecycle.ViewModel;

import ru.itis.messaging_engine.api.account.AccountManager;
import ru.itis.messaging_engine.api.crypto.DecryptionException;
import ru.itis.messaging_engine.api.crypto.DecryptionResult;
import ru.itis.messaging_engine.api.crypto.PasswordStrengthEstimator;
import ru.itis.messaging_engine.api.lifecycle.IoExecutor;
import ru.itis.meshy.android.viewmodel.LiveEvent;
import ru.itis.meshy.android.viewmodel.MutableLiveEvent;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.inject.Inject;

@NotNullByDefault
public class ChangePasswordViewModel extends ViewModel {

	private final AccountManager accountManager;
	private final Executor ioExecutor;
	private final PasswordStrengthEstimator strengthEstimator;

	@Inject
	ChangePasswordViewModel(AccountManager accountManager,
			@IoExecutor Executor ioExecutor,
			PasswordStrengthEstimator strengthEstimator) {
		this.accountManager = accountManager;
		this.ioExecutor = ioExecutor;
		this.strengthEstimator = strengthEstimator;
	}

	float estimatePasswordStrength(String password) {
		return strengthEstimator.estimateStrength(password);
	}

	LiveEvent<DecryptionResult> changePassword(String oldPassword,
			String newPassword) {
		MutableLiveEvent<DecryptionResult> result = new MutableLiveEvent<>();
		ioExecutor.execute(() -> {
			try {
				accountManager.changePassword(oldPassword, newPassword);
				result.postEvent(SUCCESS);
			} catch (DecryptionException e) {
				result.postEvent(e.getDecryptionResult());
			}
		});
		return result;
	}
}
