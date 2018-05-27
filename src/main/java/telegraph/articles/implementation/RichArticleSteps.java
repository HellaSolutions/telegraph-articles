package telegraph.articles.implementation;

import java.util.Collection;

import lombok.Builder;
import telegraph.articles.models.Image;
import telegraph.articles.models.RichArticle;
import telegraph.articles.models.Video;

/**
 * The Class RichArticleSteps.
 * 
 * RichArticle class enriched with a Builder interface
 * 
 */
public class RichArticleSteps extends RichArticle {

	/*
	 * Can access only through builder interface
	 * 
	 */
	@Builder
	private RichArticleSteps(String id, String name, Image heroImage, Collection<Video> videos) {
		super(id, name, heroImage, videos);
	}

}
