package lemon.elastic4j.test.jobmv;

import java.io.File;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import lemon.elastic.query4j.util.StringUtil;

public class Checkfile {

    public static void main(String[] args) {
        int count = 0;
        try {
            File w = new File("D:\\mv2\\mvcheck");
            Files.write("", w, Charsets.UTF_8);
            List<String> lines = Files.readLines(new File("D:\\mv2\\mv"), Charsets.UTF_8);
            for (String line : lines) {
                if (StringUtil.isNotNullOrEmpty(line)) {

                    String[] arys = line.split(",");
                    if (arys.length < 4) {
                        System.out.println(line);
                        continue;
                    }
                    long time = Long.parseLong(arys[1]);
                    if (time < 1473004800) {
                        //                        System.out.println(time + "<1473004800");
                        continue;
                    }
                    Files.append(line + "\n", w, Charsets.UTF_8);
                    count++;
                }
            }
            System.out.println(count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
