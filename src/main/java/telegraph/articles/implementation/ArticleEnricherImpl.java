package telegraph.articles.implementation;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import telegraph.articles.ArticleEnricher;
import telegraph.articles.clients.ArticleRepositoryClient;
import telegraph.articles.implementation.RichArticleSteps.RichArticleStepsBuilder;
import telegraph.articles.models.ArticleReference;
import telegraph.articles.models.RichArticle;
import telegraph.articles.models.Video;

@Slf4j
@Component
public class ArticleEnricherImpl implements ArticleEnricher {

	@Value("${services.images.timeout}")
	private long imageServiceTimeout;

	@Value("${services.videos.timeout}")
	private long videoServiceTimeout;

	@Value("${services.reference.timeout}")
	private long referenceServiceTimeout;

	/** Article repository client service. */
	@Autowired
	ArticleRepositoryClient articleRepositoryClient;
	
	@Autowired
	CacheableAssetsServiceClient casheableAssetsServiceClient;

	/*
	 * (non-Javadoc)
	 * 
	 * @see telegraph.articles.ArticleEnricher#enrichArticleWithId(java.lang.String)
	 */
	@Override
	public Future<RichArticle> enrichArticleWithId(String articleId) {

		final RichArticleStepsBuilder builder = RichArticleSteps.builder();
		return getArticleReference(articleId).thenCompose(articleReference -> compose(articleReference, builder))
				.thenApply(richArticleStepsBuilder -> richArticleStepsBuilder.build());

	}

	/**
	 * Gets the article reference.
	 *
	 * @param articleId
	 *            the article id
	 * @return the article reference object
	 */
	private CompletableFuture<ArticleReference> getArticleReference(String articleId) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return articleRepositoryClient.getArticleReferenceForId(articleId).get(referenceServiceTimeout,
						TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				throw new ServiceException(e);
			}
		}, Executors.newSingleThreadExecutor()).exceptionally(t -> {
			exceptionHandler(t);
			return null;
		});
	}

	/**
	 * Compose.
	 * 
	 * Retrieves concurrently the hero image and the video collection
	 *
	 * @param articleReference
	 *            the article reference
	 * @param builder
	 *            the builder
	 * @return the completable future
	 */
	private CompletableFuture<RichArticleStepsBuilder> compose(ArticleReference articleReference,
			RichArticleStepsBuilder builder) {

		return CompletableFuture.allOf(getHeroImage(articleReference, builder), getVideos(articleReference, builder))
				.thenApply(v -> builder.name(articleReference.getName()).id(articleReference.getId()));

	}

	/**
	 * Gets the hero image with an async call.
	 *
	 * @param articleReference
	 *            the article reference
	 * @param builder
	 *            the builder
	 * @return the hero image
	 */
	private CompletableFuture<RichArticleStepsBuilder> getHeroImage(ArticleReference articleReference,
			RichArticleStepsBuilder builder) {
		return CompletableFuture.supplyAsync(() -> 
			builder.heroImage(casheableAssetsServiceClient.getImageByIdCacheable(articleReference)), 
			Executors.newSingleThreadExecutor()).exceptionally(t -> {
			exceptionHandler(t);
			return null;
		});
	}

	/**
	 * Gets the videos.
	 * 
	 * Retrieves concurrently all the videos in the video collection
	 *
	 * @param articleReference
	 *            the article reference
	 * @param builder
	 *            the builder
	 * @return the videos
	 */
	private CompletableFuture<RichArticleStepsBuilder> getVideos(ArticleReference articleReference,
			RichArticleStepsBuilder builder) {

		List<CompletableFuture<Video>> videoCompletableFutures = articleReference.getVideoUrls().stream().
				map(url -> getVideo(url)).collect(Collectors.toList());
		return supplyAllOf(videoCompletableFutures).thenApply(videos -> builder.videos(videos));

	}

	/**
	 * Gets a single video with an async call.
	 *
	 * @param videoId
	 *            the video id
	 * @return the video
	 */
	private CompletableFuture<Video> getVideo(String videoId) {
		return CompletableFuture.supplyAsync(() -> {
			return casheableAssetsServiceClient.getVideoByIdCacheable(videoId);
		}, Executors.newSingleThreadExecutor()).exceptionally(t -> {
			exceptionHandler(t);
			return null;
		});
	}

	/**
	 * Retrieves the results of a concurrent execution on a list of
	 * CompletableFuture typed T.
	 *
	 * @param <T>
	 *            the return type of the CompletableFuture
	 * @param futures
	 *            the list of futures
	 * @return a completable future containing the return values of the input
	 *         futures
	 */
	private static <T> CompletableFuture<List<T>> supplyAllOf(List<CompletableFuture<T>> futures) {
		CompletableFuture<Void> allDoneFuture = CompletableFuture
				.allOf(futures.toArray(new CompletableFuture[futures.size()]));
		return allDoneFuture
				.thenApply(v -> futures.stream().map(future -> future.join()).collect(Collectors.<T>toList()));
	}

	private void exceptionHandler(Throwable t) {

		log.error(t.getMessage(), t);
		ServiceException serviceException = (ServiceException) t.getCause();
		if (serviceException.getCause() instanceof TimeoutException) {
			throw new ServiceTimeoutException((TimeoutException) serviceException.getCause());
		}
		throw serviceException;

	}

}
