package telegraph.articles.implementation;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import telegraph.articles.clients.AssetsServiceClient;
import telegraph.articles.models.ArticleReference;
import telegraph.articles.models.Image;
import telegraph.articles.models.Video;

@Component
public class CacheableAssetsServiceClientImpl implements CacheableAssetsServiceClient{
	
	@Value("${services.images.timeout}")
	private long imageServiceTimeout;

	@Value("${services.videos.timeout}")
	private long videoServiceTimeout;

	@Value("${services.reference.timeout}")
	private long referenceServiceTimeout;
	
	/** Assets service client service. */
	@Autowired
	AssetsServiceClient assetsServiceClient;
	
	/* (non-Javadoc)
	 * @see telegraph.articles.implementation.CacheableAssetsServiceClientP#getHeroImageCacheable(telegraph.articles.models.ArticleReference)
	 */
	@Override
	@Cacheable(cacheNames = { "images" }, key = "#articleReference.id")
	public Image getImageByIdCacheable(ArticleReference articleReference){

		try {
			return assetsServiceClient.getImageById(articleReference.getHeroImageUrl()).get(imageServiceTimeout,
					TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see telegraph.articles.implementation.CacheableAssetsServiceClientP#getVideoCacheable(java.lang.String)
	 */
	@Override
	@Cacheable(cacheNames = { "videos" }, key = "#videoId")
	public Video getVideoByIdCacheable(String videoId) {

		try {
			return assetsServiceClient.getVideoById(videoId).get(videoServiceTimeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new ServiceException(e);
		}

	}
	
	/* (non-Javadoc)
	 * @see telegraph.articles.implementation.CacheableAssetsServiceClientP#cacheClear()
	 */
	@Override
	@CacheEvict(cacheNames = { "videos", "images" }, allEntries = true)
	public void cacheClear() {
		/* annotation driven */
	}

}
