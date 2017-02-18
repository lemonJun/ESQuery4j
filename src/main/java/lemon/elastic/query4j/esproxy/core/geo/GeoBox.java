/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lemon.elastic.query4j.esproxy.core.geo;

import lemon.elastic.query4j.esproxy.geo.Box;

/**
 * 区域查询时的盒模型,这个在ES指南里有大量篇幅介绍
 * 这时候应该再看一遍这本书的
 * 
 * @author WangYazhou
 * @date  2016年3月29日 下午2:24:29
 * @see
 */
public class GeoBox {

    private GeoPoint topLeft;
    private GeoPoint bottomRight;

    public GeoBox(GeoPoint topLeft, GeoPoint bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    public GeoPoint getTopLeft() {
        return topLeft;
    }

    public GeoPoint getBottomRight() {
        return bottomRight;
    }

    /**
     * return a {@link lemon.elastic.query4j.esproxy.core.geo.GeoBox}
     * from a {@link lemon.elastic.query4j.esproxy.geo.Box}.
     *
     * @param box {@link lemon.elastic.query4j.esproxy.geo.Box} to use
     * @return a {@link lemon.elastic.query4j.esproxy.core.geo.GeoBox}
     */
    public static GeoBox fromBox(Box box) {
        GeoPoint topLeft = GeoPoint.fromPoint(box.getFirst());
        GeoPoint bottomRight = GeoPoint.fromPoint(box.getSecond());

        return new GeoBox(topLeft, bottomRight);
    }
}
