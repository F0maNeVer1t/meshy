package ru.itis.meshy.android.conversation;

import ru.itis.meshy.android.activity.ActivityScope;
import ru.itis.meshy.android.conversation.glide.MeshyDataFetcherFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class ConversationModule {

	@ActivityScope
	@Provides
	MeshyDataFetcherFactory provideMeshyDataFetcherFactory(
			MeshyDataFetcherFactory dataFetcherFactory) {
		return dataFetcherFactory;
	}

}
