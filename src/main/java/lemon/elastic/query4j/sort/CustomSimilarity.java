package lemon.elastic.query4j.sort;

import org.apache.lucene.search.similarities.DefaultSimilarity;

public class CustomSimilarity extends DefaultSimilarity {

    @Override
    public float idf(long docFreq, long numDocs) {
        return 1.0f;
    }
}
