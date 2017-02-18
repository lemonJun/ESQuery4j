package lemon.elastic.query4j.esproxy.core;

import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.action.mlt.MoreLikeThisRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.lang3.StringUtils;

import lemon.elastic.query4j.esproxy.core.query.MoreLikeThisQuery;
import lemon.elastic.query4j.util.CollectionUtil;

public class MoreLikeThisProcessor implements Processor {

    @SuppressWarnings("deprecation")
    public MoreLikeThisRequestBuilder creatMoreLikeThis(Client client, String indexName, String type, int startRecord, MoreLikeThisQuery query, Class clazz) {
        MoreLikeThisRequestBuilder requestBuilder = client.prepareMoreLikeThis(indexName, type, query.getId());

        if (query.getPageable() != null) {
            startRecord = query.getPageable().getPageNumber() * query.getPageable().getPageSize();
            requestBuilder.setSearchSize(query.getPageable().getPageSize());
        }
        requestBuilder.setSearchFrom(startRecord);

        if (CollectionUtils.isNotEmpty(query.getSearchIndices())) {
            requestBuilder.setSearchIndices(CollectionUtil.toArray(query.getSearchIndices()));
        }
        if (CollectionUtils.isNotEmpty(query.getSearchTypes())) {
            requestBuilder.setSearchTypes(CollectionUtil.toArray(query.getSearchTypes()));
        }
        if (CollectionUtils.isNotEmpty(query.getFields())) {
            requestBuilder.setField(CollectionUtil.toArray(query.getFields()));
        }
        if (StringUtils.isNotBlank(query.getRouting())) {
            requestBuilder.setRouting(query.getRouting());
        }
        if (query.getPercentTermsToMatch() != null) {
            requestBuilder.setPercentTermsToMatch(query.getPercentTermsToMatch());
        }
        if (query.getMinTermFreq() != null) {
            requestBuilder.setMinTermFreq(query.getMinTermFreq());
        }
        if (query.getMaxQueryTerms() != null) {
            requestBuilder.maxQueryTerms(query.getMaxQueryTerms());
        }
        if (CollectionUtils.isNotEmpty(query.getStopWords())) {
            requestBuilder.setStopWords(CollectionUtil.toArray(query.getStopWords()));
        }
        if (query.getMinDocFreq() != null) {
            requestBuilder.setMinDocFreq(query.getMinDocFreq());
        }
        if (query.getMaxDocFreq() != null) {
            requestBuilder.setMaxDocFreq(query.getMaxDocFreq());
        }
        if (query.getMinWordLen() != null) {
            requestBuilder.setMinWordLen(query.getMinWordLen());
        }
        if (query.getMaxWordLen() != null) {
            requestBuilder.setMaxWordLen(query.getMaxWordLen());
        }
        if (query.getBoostTerms() != null) {
            requestBuilder.setBoostTerms(query.getBoostTerms());
        }

        return requestBuilder;
    }
}
