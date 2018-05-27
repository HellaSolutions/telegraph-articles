package telegraph.articles.implementation;

import telegraph.articles.models.ArticleReference;
import telegraph.articles.models.Image;
import telegraph.articles.models.Video;

public interface CacheableAssetsServiceClient {
	
	Image getImageByIdCacheable(ArticleReference articleReference);

	Video getVideoByIdCacheable(String videoId);

	void cacheClear();


}