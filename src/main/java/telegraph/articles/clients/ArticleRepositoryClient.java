package telegraph.articles.clients;

import telegraph.articles.models.ArticleReference;

import java.util.concurrent.Future;

public interface ArticleRepositoryClient {
	Future<ArticleReference> getArticleReferenceForId(String articleId);
}
