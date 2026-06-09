package ru.itis.meshy.android.account;

import androidx.lifecycle.ViewModel;

import ru.itis.meshy.android.viewmodel.ViewModelKey;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class SetupModule {

	@Binds
	@IntoMap
	@ViewModelKey(SetupViewModel.class)
	abstract ViewModel bindSetupViewModel(
			SetupViewModel setupViewModel);
}
