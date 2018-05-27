## TMG Test

Requirements deduced from the test description:

- Video and Image classes are simplified representations of objects that will contain large streams of bytes.
- The hero image and videos associated to an article should be acquired in full concurrency to minimize the latency of the _ArticleEnricher_ service.
- The _enrichArticleWithId_ method should not indefinitely stale. If one of the clients methods involved does not return a response in a given window of time the  _enrichArticleWithId_ should end with failure. Timeout thresholds should be defined and configurable independently for image and video acquiring. The strategy could be improved requiring that, to consider the article fully enriched, only a majority of the required data (e.g. 80% of videos available) should be available, in a given window of time (not implemented in the current solution).
- The information delivered is an article. Overall, is very probable than a few number of articles will get a big number of requests, while the majority of them will stay at the average. Given the presence of large volumes of unstructured data required to enrich an article, a caching strategy seems a right choice to minimize the response time and the overall processing effort required to serve the data. Implemented using Echace.
- It is not clear from the provided interfaces the relation between an image or video "id", required by the assets client, and videos and image urls returned by the article reference service. I assumed that urls can be used as ids to invoke the assets service.
