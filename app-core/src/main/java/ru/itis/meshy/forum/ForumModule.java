package ru.itis.meshy.forum;

import ru.itis.messaging_engine.api.FeatureFlags;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.sync.validation.ValidationManager;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.meshy.api.forum.ForumFactory;
import ru.itis.meshy.api.forum.ForumManager;
import ru.itis.meshy.api.forum.ForumPostFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static ru.itis.meshy.api.forum.ForumManager.CLIENT_ID;
import static ru.itis.meshy.api.forum.ForumManager.MAJOR_VERSION;

@Module
public class ForumModule {

	public static class EagerSingletons {
		@Inject
		ForumManager forumManager;
		@Inject
		ForumPostValidator forumPostValidator;
	}

	@Provides
	@Singleton
	ForumManager provideForumManager(ForumManagerImpl forumManager,
			ValidationManager validationManager,
			FeatureFlags featureFlags) {
		if (!featureFlags.shouldEnableForumsInCore()) {
			return forumManager;
		}
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				forumManager);
		return forumManager;
	}

	@Provides
	ForumPostFactory provideForumPostFactory(
			ForumPostFactoryImpl forumPostFactory) {
		return forumPostFactory;
	}

	@Provides
	ForumFactory provideForumFactory(ForumFactoryImpl forumFactory) {
		return forumFactory;
	}

	@Provides
	@Singleton
	ForumPostValidator provideForumPostValidator(
			ValidationManager validationManager, ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock,
			FeatureFlags featureFlags) {
		ForumPostValidator validator = new ForumPostValidator(clientHelper,
				metadataEncoder, clock);
		if (featureFlags.shouldEnableForumsInCore()) {
			validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
					validator);
		}
		return validator;
	}

}
