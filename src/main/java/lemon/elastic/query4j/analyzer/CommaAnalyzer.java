package lemon.elastic.query4j.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;

/**
 * 自定义解析器
 * 
 * 
 * @author lemon
 * @version 1.0
 * @date  2016年4月8日 上午12:15:32
 * @see 
 * @since
 */
public class CommaAnalyzer extends Analyzer {

    public CommaAnalyzer() {
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        return new TokenStreamComponents(new CommaTokenizer(reader));
    }

}
