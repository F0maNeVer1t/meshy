package ru.itis.meshy.android.settings;

import ru.itis.messaging_engine.api.identity.LocalAuthor;
import ru.itis.meshy.api.identity.AuthorInfo;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class OwnIdentityInfo {

	private final LocalAuthor localAuthor;
	private final AuthorInfo authorInfo;

	OwnIdentityInfo(LocalAuthor localAuthor, AuthorInfo authorInfo) {
		this.localAuthor = localAuthor;
		this.authorInfo = authorInfo;
	}

	LocalAuthor getLocalAuthor() {
		return localAuthor;
	}

	AuthorInfo getAuthorInfo() {
		return authorInfo;
	}

}