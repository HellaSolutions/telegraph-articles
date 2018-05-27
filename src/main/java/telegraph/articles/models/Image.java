package telegraph.articles.models;

public class Image {
	private final String id;
	private final String altText;

	public Image(String id, String altText) {
		this.id = id;
		this.altText = altText;
	}

	public String getId() {
		return id;
	}

	public String getAltText() {
		return altText;
	}
}
