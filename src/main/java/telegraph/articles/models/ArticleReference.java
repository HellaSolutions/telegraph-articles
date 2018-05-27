package telegraph.articles.models;

import java.util.Collection;

public class ArticleReference {
	private final String id;
	private final String name;
	private final String heroImageUrl;
	private final Collection<String> videoUrls;

	public ArticleReference(String id, String name, String heroImageUrl, Collection<String> videoUrls) {
		this.id = id;
		this.name = name;
		this.heroImageUrl = heroImageUrl;
		this.videoUrls = videoUrls;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getHeroImageUrl() {
		return heroImageUrl;
	}

	public Collection<String> getVideoUrls() {
		return videoUrls;
	}
}
