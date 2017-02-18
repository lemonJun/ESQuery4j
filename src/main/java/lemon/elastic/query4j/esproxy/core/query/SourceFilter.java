package lemon.elastic.query4j.esproxy.core.query;

/**
 * SourceFilter for providing includes and excludes.
 *
 * @Author Jon Tsiros
 */
public interface SourceFilter {

    String[] getIncludes();

    String[] getExcludes();
}
