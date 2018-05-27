package telegraph;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static telegraph.TestDataBox.with;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import telegraph.articles.ArticleEnricher;
import telegraph.articles.clients.ArticleRepositoryClient;
import telegraph.articles.clients.AssetsServiceClient;
import telegraph.articles.implementation.CacheableAssetsServiceClient;
import telegraph.articles.implementation.ServiceTimeoutException;
import telegraph.articles.models.ArticleReference;
import telegraph.articles.models.Image;
import telegraph.articles.models.RichArticle;
import telegraph.articles.models.Video;

//TODO: Auto-generated Javadoc
/**
 * The Class TelegraphApplicationTests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableCaching
public class TelegraphApplicationTests {

	/**
	 * The Constant CONCURRENCY_LEVEL_THRESHOLD.
	 * 
	 * The videos and image retrievals are executed in full concurrency. If the
	 * number of the slower requests is limited, the total processing time should 
	 * not differ too much from the maximum of the delays assigned to each service 
	 * mock. CONCURRENCY_LEVEL_THRESHOLD bounds the total process time in terms of 
	 * the relative error
	 * 
	 * |total_processing_time - max_delay|/max_delay <= CONCURRENCY_LEVEL_THRESHOLD
	 * 
	 * Should be adjusted in dependence of the system CPU (current: i7 quad core)
	 * 
	 */
	private static final double CONCURRENCY_LEVEL_THRESHOLD = 10E-2;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Value("${services.images.timeout}")
	private long imageServiceTimeout;

	@Value("${services.videos.timeout}")
	private long videoServiceTimeout;

	@Value("${services.reference.timeout}")
	private long referenceServiceTimeout;

	/** The article repository client mock. */
	@MockBean
	ArticleRepositoryClient articleRepositoryClientMock;

	/** The assets service client mock. */
	@MockBean
	AssetsServiceClient assetsServiceClientMock;

	/** The article enricher implememtation under test. */
	@Autowired
	ArticleEnricher articleEnricher;
	
	/** The cacheable asset service client mock. */
	@Autowired
	CacheableAssetsServiceClient cacheableAssetsServiceClient;

	/**
	 * Tests data match and concurrency threshold.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testDataMatchAndConcurrencyThreshold() throws Exception {

		final StopWatch stopWatch = new StopWatch();
		TestDataBox testData = standardTestConfig(referenceServiceTimeout, imageServiceTimeout, videoServiceTimeout);
		stopWatch.start();
		RichArticle richArticle = articleEnricher.enrichArticleWithId(testData.getReference().getId()).get();
		stopWatch.stop();
		testData.assertMatch(richArticle);
		assertEquals("Wrong concurrency level", videoServiceTimeout, new Double(stopWatch.getTotalTimeMillis()),
				videoServiceTimeout * CONCURRENCY_LEVEL_THRESHOLD);

	}

	/**
	 * Tests reference service timeout.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testReferenceServiceTimeout() throws Exception {

		thrown.expectCause(isA(ServiceTimeoutException.class));
		TestDataBox testData = standardTestConfig(2 * referenceServiceTimeout, imageServiceTimeout,
				videoServiceTimeout);
		articleEnricher.enrichArticleWithId(testData.getReference().getId()).get();

	}

	/**
	 * Tests video service timeout.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testVideoServiceTimeout() throws Exception {

		thrown.expectCause(isA(ServiceTimeoutException.class));
		TestDataBox testData = standardTestConfig(referenceServiceTimeout, 2 * imageServiceTimeout,
				videoServiceTimeout);
		articleEnricher.enrichArticleWithId(testData.getReference().getId()).get();

	}

	/**
	 * Tests image service timeout.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testImageServiceTimeout() throws Exception {

		thrown.expectCause(isA(ServiceTimeoutException.class));
		TestDataBox testData = standardTestConfig(referenceServiceTimeout, imageServiceTimeout,
				2 * videoServiceTimeout);
		articleEnricher.enrichArticleWithId(testData.getReference().getId()).get();

	}

	/**
	 * Tests caching is working properly.
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCachingAvoidsTimeout() throws Exception {

		String id = UUID.randomUUID().toString();
		TestDataBox testData = standardTestConfig(id, referenceServiceTimeout, imageServiceTimeout, videoServiceTimeout);
		articleEnricher.enrichArticleWithId(testData.getReference().getId()).get();
		testData = standardTestConfig(id, referenceServiceTimeout, 2 * imageServiceTimeout, 2 * videoServiceTimeout);
		articleEnricher.enrichArticleWithId(testData.getReference().getId()).get();

	}
	
	/**
	 * Tests caching is working properly.
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCacheClearCausesTimeout() throws Exception {

		thrown.expectCause(isA(ServiceTimeoutException.class));
		
		String id = UUID.randomUUID().toString();
		TestDataBox testData = standardTestConfig(id, referenceServiceTimeout, imageServiceTimeout, videoServiceTimeout);
		articleEnricher.enrichArticleWithId(testData.getReference().getId()).get();
		cacheableAssetsServiceClient.cacheClear();
		testData = standardTestConfig(id, referenceServiceTimeout, 2 * imageServiceTimeout, 2 * videoServiceTimeout);
		articleEnricher.enrichArticleWithId(testData.getReference().getId()).get();

	}
	
	/**
	 * Standard test config.
	 *
	 * @param referenceDelay
	 *            the reference service delay
	 * @param imageDelay
	 *            the image service delay
	 * @param videoDelay
	 *            the video service delay
	 * @return the test data box
	 */
	public TestDataBox standardTestConfig(long referenceDelay, long imageDelay, long videoDelay) {
		return standardTestConfig(UUID.randomUUID().toString(), referenceDelay, imageDelay, videoDelay);
	}
	
	public TestDataBox standardTestConfig(String id, long referenceDelay, long imageDelay, long videoDelay) {
		
		TestDataBox testData = with(
				new ArticleReference(id, id + "_name", id + "_heroImageUrl", Arrays.asList(id + "_video_a", id + "_video_b", id + "_video_c")));
		givenArticleReferenceServiceDelay(testData.getReference(), referenceDelay, TimeUnit.MILLISECONDS);
		givenAssetsServiceClientHeroImageDelay(testData.getImage(), imageDelay, TimeUnit.MILLISECONDS);
		givenAssetsServiceClientVideoDelay(testData.getVideos(), new long[] { videoDelay, videoDelay, videoDelay },
				TimeUnit.MILLISECONDS);
		return testData;
		
	}

	/**
	 * Mocks reference service with a response delay.
	 *
	 * @param reference
	 *            the reference to be returned
	 * @param delay
	 *            the reference service delay
	 * @param unit
	 *            the time unit
	 */
	public void givenArticleReferenceServiceDelay(ArticleReference reference, long delay, TimeUnit unit) {

		given(this.articleRepositoryClientMock.getArticleReferenceForId(reference.getId()))
				.willReturn(withDelay(reference, delay, unit));

	}

	/**
	 * Mocks image service with a response delay.
	 *
	 * @param reference
	 *            the reference to be returned
	 * @param delay
	 *            the image service delay
	 * @param unit
	 *            the time unit
	 */
	public void givenAssetsServiceClientHeroImageDelay(Image reference, long delay, TimeUnit unit) {

		given(this.assetsServiceClientMock.getImageById(reference.getId()))
				.willReturn(withDelay(reference, delay, unit));

	}

	/**
	 * Mocks video service with a response delay.
	 *
	 * @param reference
	 *            the reference to be returned
	 * @param delay
	 *            the video service delay
	 * @param unit
	 *            the time unit
	 */
	public void givenAssetsServiceClientVideoDelay(Video reference, long delay, TimeUnit unit) {

		given(this.assetsServiceClientMock.getVideoById(reference.getId()))
				.willReturn(withDelay(reference, delay, unit));

	}

	/**
	 * Mocks video service with a response delay.
	 *
	 * @param references
	 *            the references to be returned
	 * @param delay
	 *            the video service delays for each reference instance
	 * @param unit
	 *            the time unit
	 */
	public void givenAssetsServiceClientVideoDelay(List<Video> references, long[] delay, TimeUnit unit) {

		if (delay.length < references.size()) {
			throw new IllegalArgumentException("Provide a delay value for each reference");
		}
		int d = 0;
		for (Video v : references) {
			givenAssetsServiceClientVideoDelay(v, delay[d++], unit);
		}

	}

	/**
	 * Return a Future of type T that returns reference with the given delay
	 *
	 * @param <T>
	 *            the generic type
	 * @param reference
	 *            the reference
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the time unit
	 * @return the future
	 */
	public static <T> Future<T> withDelay(T reference, long delay, TimeUnit unit) {
		return Executors.newScheduledThreadPool(1).schedule(() -> {
			return reference;
		}, delay, unit);
	}

}
