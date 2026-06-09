package ru.itis.meshy.android.mailbox;

import androidx.lifecycle.ViewModel;

import ru.itis.meshy.android.viewmodel.ViewModelKey;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public interface MailboxModule {

	@Binds
	@IntoMap
	@ViewModelKey(MailboxViewModel.class)
	ViewModel bindMailboxViewModel(MailboxViewModel mailboxViewModel);

}
