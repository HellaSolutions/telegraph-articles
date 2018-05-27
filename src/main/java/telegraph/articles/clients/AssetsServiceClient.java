package telegraph.articles.clients;

import telegraph.articles.models.Image;
import telegraph.articles.models.Video;

import java.util.concurrent.Future;

public interface AssetsServiceClient {
	Future<Image> getImageById(String id);

	Future<Video> getVideoById(String id);
}
