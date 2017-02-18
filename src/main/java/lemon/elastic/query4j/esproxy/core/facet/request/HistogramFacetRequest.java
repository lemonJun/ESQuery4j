/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lemon.elastic.query4j.esproxy.core.facet.request;

import java.util.concurrent.TimeUnit;

import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.histogram.HistogramFacetBuilder;

import com.google.common.base.Preconditions;

import lemon.elastic.query4j.esproxy.core.facet.AbstractFacetRequest;

/**
 * @author Artur Konczak
 * @author Mohsin Husen
 */
public class HistogramFacetRequest extends AbstractFacetRequest {

	private String field;
	private long interval;
	private TimeUnit timeUnit;

	public HistogramFacetRequest(String name) {
		super(name);
	}

	public void setField(String field) {
		this.field = field;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public FacetBuilder getFacet() {
		Preconditions.checkNotNull(getName(), "Facet name can't be a null !!!");
		Preconditions.checkState(StringUtils.isNotBlank(field), "Please select field on which to build the facet !!!");
		Preconditions.checkState(interval > 0, "Please provide interval as positive value greater them zero !!!");

		HistogramFacetBuilder builder = FacetBuilders.histogramFacet(getName());
		builder.field(field);

		if (timeUnit != null) {
			builder.interval(interval, timeUnit);
		} else {
			builder.interval(interval);
		}

		return builder;
	}
}
