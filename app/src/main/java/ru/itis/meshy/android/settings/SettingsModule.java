package ru.itis.meshy.android.settings;

import androidx.lifecycle.ViewModel;

import ru.itis.meshy.android.viewmodel.ViewModelKey;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class SettingsModule {

	@Binds
	@IntoMap
	@ViewModelKey(SettingsViewModel.class)
	abstract ViewModel bindSettingsViewModel(
			SettingsViewModel settingsViewModel);

}
