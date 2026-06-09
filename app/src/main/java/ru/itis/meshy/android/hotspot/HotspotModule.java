package ru.itis.meshy.android.hotspot;

import androidx.lifecycle.ViewModel;

import ru.itis.meshy.android.viewmodel.ViewModelKey;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public interface HotspotModule {

	@Binds
	@IntoMap
	@ViewModelKey(HotspotViewModel.class)
	ViewModel bindHotspotViewModel(HotspotViewModel hotspotViewModel);

}
