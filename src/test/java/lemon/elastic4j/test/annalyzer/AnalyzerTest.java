package lemon.elastic4j.test.annalyzer;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.junit.Test;

import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;

public class AnalyzerTest {

    @Test
    public void comma() {
        Analyzer analyzer = null;
        try {
            analyzer = new WhitespaceAnalyzer();
            TokenStream tokenStream = analyzer.tokenStream("content", new StringReader("1 2 243"));
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                CharTermAttribute attribute = tokenStream.getAttribute(CharTermAttribute.class);
                OffsetAttribute ffsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
                System.out.println(attribute);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                analyzer.close();
            } catch (Exception e2) {
            }
        }
    }

    @Test
    public void mmseg() {
        Analyzer analyzer = null;
        try {
            analyzer = new MMSegAnalyzer(new File("E:\\opensource\\mmseg4j-master\\mmseg4j-master\\data"));

            TokenStream tokenStream = analyzer.tokenStream("content", new StringReader("宝洁 公司"));
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                CharTermAttribute attribute = tokenStream.getAttribute(CharTermAttribute.class);
                OffsetAttribute ffsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
                System.out.println(attribute);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                analyzer.close();
            } catch (Exception e2) {
            }
        }
    }

}
