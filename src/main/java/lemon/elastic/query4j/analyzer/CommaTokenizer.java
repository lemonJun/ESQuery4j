package lemon.elastic.query4j.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.util.CharTokenizer;

public class CommaTokenizer extends CharTokenizer {

    public CommaTokenizer(Reader in) {
        super(in);
    }

    @Override
    protected boolean isTokenChar(int c) {
        return !(c == 44);
    }
}
