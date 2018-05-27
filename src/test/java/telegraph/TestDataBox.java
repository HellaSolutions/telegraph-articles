package telegraph;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import telegraph.articles.models.ArticleReference;
import telegraph.articles.models.Image;
import telegraph.articles.models.RichArticle;
import telegraph.articles.models.Video;

@Getter
@Setter
public class TestDataBox {

	/** The reference. */
	private ArticleReference reference = null;
	
	/** The image. */
	private Image image = null;
	
	/** The videos. */
	private List<Video> videos = new ArrayList<>();

	/**
	 * Instantiates a new test data box.
	 */
	private TestDataBox() {
	}

	/**
	 * Prepares the expected results with the given ArticleReference 
	 *
	 * @param reference the reference
	 * @return the test data box
	 */
	public static TestDataBox with(ArticleReference reference) {

		TestDataBox test = new TestDataBox();
		test.reference = reference;
		if (reference != null) {
			test.image = new Image(reference.getHeroImageUrl(), "altText");
			test.videos = reference.getVideoUrls().stream().map(vId -> new Video(vId, "c_" + vId))
					.collect(Collectors.toList());
		}
		return test;

	}

	/**
	 * Checks if the RichArticle elements match the ArticleReference attributes  
	 *
	 * @param richArticle the rich article
	 */
	public void assertMatch(RichArticle richArticle) {

		assertEquals("Hero image does not math", hash(image), hash(richArticle.getHeroImage()));
		assertEquals("name does not math", reference.getName(), richArticle.getName());
		assertEquals("id does not math", reference.getId(), richArticle.getId());
		assertEquals("videos do not math", hash(videos), hash(richArticle.getVideos()));

	}

	/**
	 * Hash.
	 *
	 * @param videos the videos
	 * @return the list
	 */
	private static final List<VideoMatcher> hash(Collection<Video> videos) {

		return videos.stream().map(v -> new VideoMatcher(v)).collect(Collectors.toList());

	}

	/**
	 * Hash.
	 *
	 * @param image the image
	 * @return the image matcher
	 */
	private static final ImageMatcher hash(Image image) {

		return new ImageMatcher(image);

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@EqualsAndHashCode
	private static class VideoMatcher {

		/** The id. */
		private String id;
		
		/** The caption. */
		private String caption;

		/**
		 * Instantiates a new video matcher.
		 *
		 * @param video the video
		 */
		public VideoMatcher(Video video) {
			this.id = video.getId();
			this.caption = video.getCaption();
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@EqualsAndHashCode
	private static class ImageMatcher {

		/** The id. */
		private String id;
		
		/** The alt text. */
		private String altText;

		/**
		 * Instantiates a new image matcher.
		 *
		 * @param image the image
		 */
		public ImageMatcher(Image image) {
			this.id = image.getId();
			this.altText = image.getAltText();
		}

	}

}
