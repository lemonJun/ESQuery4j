package lemon.elastic.query4j.util;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * 经纬度工具类
 *
 * @author WangYazhou
 * @date  2016年3月22日 上午11:40:26
 * @see
 */
public class LngLatUtil {

    private static final double EARTH_RADIUS = 6378137; //赤道半径  M

    public static final double POLAR_RADIUS = 6356725; //极半径 M

    public static final DecimalFormat df = new DecimalFormat("#.000000");

    public static final int MT_GOOGLE = 1;
    public static final int MT_BAIDU = 2;

    /**
     * 计算两个经纬度坐标点之间的距离     经百度地图测距比较
     * @param lng1
     * @param Lat1
     * @param lng2
     * @param lat2
     * @return unit: M
     */
    public static Long calculateDistance(Double lng1, Double lat1, Double lng2, Double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        return (long) s;
    }

    public static Double adjustUp(Double lng, Double lat, Double distance) {
        return adjust(lng, lat, distance, 0D);
    }

    public static Double adjustRight(Double lng, Double lat, Double distance) {
        return adjust(lng, lat, distance, 90D);
    }

    public static Double adjustDown(Double lng, Double lat, Double distance) {
        return adjust(lng, lat, distance, 180D);
    }

    public static Double adjustLeft(Double lng, Double lat, Double distance) {
        return adjust(lng, lat, distance, 270D);
    }

    /**
     * 已知A点经纬度，B到A的距离，求B经纬度，
     * 以3KM测试，误差为4M
     * @param lng
     * @param lat
     * @param distance
     * @param angle
     * @param xy 0:up  1:right  2:down  3:left
     * @return
     */
    public static Double adjust(Double lng, Double lat, Double distance, Double angle) {
        double dx = distance * 1000 * Math.sin(angle * Math.PI / 180);
        double dy = distance * 1000 * Math.cos(angle * Math.PI / 180);
        double lon1 = (dx / Ed(lat) + rad(lng)) * 180 / Math.PI;
        double lat1 = (dy / Ec(lat) + rad(lat)) * 180 / Math.PI;

        if (angle == 0 || angle == 180) {
            return Double.parseDouble(df.format(lat1));
        } else {
            return Double.parseDouble(df.format(lon1));
        }
    }

    /**
     * 转换为弧度
     * @param d
     * @return
     */
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    private static double Ec(Double lat) {
        return POLAR_RADIUS + (EARTH_RADIUS - POLAR_RADIUS) * (90 - lat) / 90;
    }

    private static double Ed(Double lat) {
        return Ec(lat) * Math.cos(rad(lat));
    }

    /**
     * 随机生成一区域内的经纬度   
     * 北京五环的坐标为:
     * 西 116.218571,39.930871
     * 东 116.557771,39.930429
     * 南 116.385297,39.797068
     * 北 116.395645,40.033924
     * 
     * @param lngstart  116.218571
     * @param lngend    116.557771
     * @param latstart  39.797068
     * @param latend    40.033924
     */
    public static void randomGenLngLat(Double lngstart, Double lngend, Double latstart, Double latend) {
        Random ran = new Random();
        Double lngdis = lngend - lngstart;
        double latdis = latend - latstart;

        int count = 0;
        while (count < 200) {
            count++;
            Double d1 = Double.parseDouble(df.format(ran.nextDouble() * lngdis + lngstart));
            Double d2 = Double.parseDouble(df.format(ran.nextDouble() * latdis + latstart));

            System.out.println(d1 + "," + d2);
        }
    }

    public static Double randomGenLng(Double lngstart, Double lngend) {
        Random ran = new Random();
        Double lngdis = lngend - lngstart;

        return Double.parseDouble(df.format(ran.nextDouble() * lngdis + lngstart));
    }

    public static Double randomGenLat(Double latstart, Double latend) {
        Random ran = new Random();
        double latdis = latend - latstart;

        return Double.parseDouble(df.format(ran.nextDouble() * latdis + latstart));
    }

    public static void main(String[] args) {
        //        randomGenLngLat(116.218571, 116.557771, 39.797068, 40.033924);
        //generateLngLat(116.392896, 39.939502, 39.797068, 40.033924);
        //百度地图上测距为213
        //        System.out.println(calculateDistance(116.432561, 39.96508, 116.435076, 39.965121));
        //        System.out.println(calculateDistance(116.392896, 39.939502, 116.357695, 39.912512));

        //        System.out.println(adjustLeft(116.392896, 39.939502, 3D));
    }
}
