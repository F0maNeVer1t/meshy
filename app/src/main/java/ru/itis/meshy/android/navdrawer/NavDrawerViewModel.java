package ru.itis.meshy.android.navdrawer;

import static org.briarproject.android.dontkillmelib.DozeUtils.needsDozeWhitelisting;
import static ru.itis.messaging_engine.util.LogUtils.logException;
import static ru.itis.meshy.android.controller.MeshyControllerImpl.DOZE_ASK_AGAIN;
import static ru.itis.meshy.android.settings.SettingsFragment.SETTINGS_NAMESPACE;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import android.app.Application;

import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.itis.meshy.android.MeshyApplication;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.settings.Settings;
import ru.itis.messaging_engine.api.settings.SettingsManager;
import ru.itis.messaging_engine.api.system.AndroidExecutor;
import ru.itis.meshy.android.viewmodel.DbViewModel;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

@NotNullByDefault
public class NavDrawerViewModel extends DbViewModel {

	private static final Logger LOG =
			getLogger(NavDrawerViewModel.class.getName());

	private static final String SHOW_TRANSPORTS_ONBOARDING =
			"showTransportsOnboarding";

	private final SettingsManager settingsManager;

	private final MutableLiveData<Boolean> shouldAskForDozeWhitelisting =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> showTransportsOnboarding =
			new MutableLiveData<>();

	@Inject
	NavDrawerViewModel(Application app,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			SettingsManager settingsManager) {
		super(app, dbExecutor, lifecycleManager, db, androidExecutor);
		this.settingsManager = settingsManager;
	}

	LiveData<Boolean> shouldAskForDozeWhitelisting() {
		return shouldAskForDozeWhitelisting;
	}

	@UiThread
	void checkDozeWhitelisting() {
		// check this first, to hit the DbThread only when really necessary
		MeshyApplication app = getApplication();
		if (app.isInstrumentationTest() ||
				!needsDozeWhitelisting(getApplication())) {
			shouldAskForDozeWhitelisting.setValue(false);
			return;
		}
		runOnDbThread(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(SETTINGS_NAMESPACE);
				boolean ask = settings.getBoolean(DOZE_ASK_AGAIN, true);
				shouldAskForDozeWhitelisting.postValue(ask);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				shouldAskForDozeWhitelisting.postValue(true);
			}
		});
	}

	@UiThread
	LiveData<Boolean> showTransportsOnboarding() {
		return showTransportsOnboarding;
	}

	@UiThread
	void checkTransportsOnboarding() {
		if (showTransportsOnboarding.getValue() != null) return;
		runOnDbThread(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(SETTINGS_NAMESPACE);
				boolean show =
						settings.getBoolean(SHOW_TRANSPORTS_ONBOARDING, true);
				showTransportsOnboarding.postValue(show);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	@UiThread
	void transportsOnboardingShown() {
		showTransportsOnboarding.setValue(false);
		runOnDbThread(() -> {
			try {
				Settings settings = new Settings();
				settings.putBoolean(SHOW_TRANSPORTS_ONBOARDING, false);
				settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
}
