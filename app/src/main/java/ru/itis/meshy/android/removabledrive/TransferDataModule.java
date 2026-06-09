package ru.itis.meshy.android.removabledrive;

import androidx.lifecycle.ViewModel;

import ru.itis.meshy.android.viewmodel.ViewModelKey;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public interface TransferDataModule {

	@Binds
	@IntoMap
	@ViewModelKey(RemovableDriveViewModel.class)
	ViewModel bindRemovableDriveViewModel(
			RemovableDriveViewModel removableDriveViewModel);

}
