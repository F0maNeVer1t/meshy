package ru.itis.messaging_engine.reliability;

import ru.itis.messaging_engine.api.lifecycle.IoExecutor;
import ru.itis.messaging_engine.api.reliability.ReliabilityLayerFactory;

import java.util.concurrent.Executor;

import dagger.Module;
import dagger.Provides;

@Module
public class ReliabilityModule {

	@Provides
	ReliabilityLayerFactory provideReliabilityFactoryByExector(
			@IoExecutor Executor ioExecutor) {
		return new ReliabilityLayerFactoryImpl(ioExecutor);
	}

	@Provides
	ReliabilityLayerFactory provideReliabilityFactory(
			ReliabilityLayerFactoryImpl reliabilityLayerFactory) {
		return reliabilityLayerFactory;
	}

}
