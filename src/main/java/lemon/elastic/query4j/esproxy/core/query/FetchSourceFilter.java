package lemon.elastic.query4j.esproxy.core.query;

/**
 * SourceFilter implementation for providing includes and excludes.
 * 
 * 不要随意删除可能有用的东西
 *
 * @Author Jon Tsiros
 */
public class FetchSourceFilter implements SourceFilter {

    private final String[] includes;
    private final String[] excludes;

    public FetchSourceFilter(final String[] includes, final String[] excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    @Override
    public String[] getIncludes() {
        return includes;
    }

    @Override
    public String[] getExcludes() {
        return excludes;
    }
}
