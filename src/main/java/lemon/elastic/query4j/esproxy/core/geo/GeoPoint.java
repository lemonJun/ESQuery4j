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

import lemon.elastic.query4j.esproxy.geo.Point;

/**
 * geo-location used for #{@link lemon.elastic.query4j.esproxy.core.query.Criteria}.
 *
 * @author Franck Marchand
 */
public class GeoPoint {

    private double lat;
    private double lon;

    private GeoPoint() {
        //required by mapper to instantiate object
    }

    public GeoPoint(double latitude, double longitude) {
        this.lat = latitude;
        this.lon = longitude;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    /**
     * build a GeoPoint from a {@link lemon.elastic.query4j.esproxy.geo.Point}
     *
     * @param point {@link lemon.elastic.query4j.esproxy.geo.Point}
     * @return a {@link lemon.elastic.query4j.esproxy.core.geo.GeoPoint}
     */
    public static GeoPoint fromPoint(Point point) {
        return new GeoPoint(point.getY(), point.getX());
    }

    public static Point toPoint(GeoPoint point) {
        return new Point(point.getLat(), point.getLon());
    }
}
