package ru.itis.meshy.feed;

import ru.itis.meshy.api.feed.Feed;
import ru.itis.meshy.api.feed.RssProperties;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@NotNullByDefault
interface FeedMatcher {

	/**
	 * Returns the best match for the given candidate from the given list of
	 * feeds, or null if there are no matches.
	 */
	@Nullable
	Feed findMatchingFeed(RssProperties candidate, List<Feed> feeds);
}
