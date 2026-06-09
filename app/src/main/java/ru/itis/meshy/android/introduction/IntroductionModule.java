package ru.itis.meshy.android.introduction;

import androidx.lifecycle.ViewModel;

import ru.itis.meshy.android.viewmodel.ViewModelKey;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class IntroductionModule {

	@Binds
	@IntoMap
	@ViewModelKey(IntroductionViewModel.class)
	abstract ViewModel bindIntroductionViewModel(
			IntroductionViewModel introductionViewModel);

}
