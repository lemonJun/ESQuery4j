package lemon.elastic4j.test.cv;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class CvCheck {

    public static void main(String[] args) {
        try {
            List<String> lines = Files.readLines(new File("D:\\es\\data\\1.txt"), Charsets.UTF_8);
            System.out.println(lines.size());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
