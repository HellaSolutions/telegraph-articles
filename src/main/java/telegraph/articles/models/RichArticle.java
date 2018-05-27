package telegraph.articles.models;

import java.util.Collection;

public class RichArticle {

	private final String id;
	private final String name;
	private final Image heroImage;
	private final Collection<Video> videos;

	public RichArticle(String id, String name, Image heroImage, Collection<Video> videos) {
		this.id = id;
		this.name = name;
		this.heroImage = heroImage;
		this.videos = videos;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Image getHeroImage() {
		return heroImage;
	}

	public Collection<Video> getVideos() {
		return videos;
	}
}
