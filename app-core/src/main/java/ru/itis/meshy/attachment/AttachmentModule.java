package ru.itis.meshy.attachment;

import ru.itis.meshy.api.attachment.AttachmentReader;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AttachmentModule {

	@Provides
	@Singleton
	AttachmentReader provideAttachmentReader(
			AttachmentReaderImpl attachmentReader) {
		return attachmentReader;
	}

}
