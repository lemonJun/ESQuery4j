package lemon.elastic4j.test.chew;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class RawFileTest {

    @Test
    public void checksize() {
        try {
            List<String> list = Files.readLines(new File("D:/20150811"), Charsets.UTF_8);
            System.out.println("total--" + list.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void duplicatego() {
        try {
            List<String> list = Files.readLines(new File("D:/20150811"), Charsets.UTF_8);
            Set<Long> infoset = new HashSet<Long>();
            int dupcnt = 0;
            int flag = 0;
            for (String linestr : list) {
                if (linestr.indexOf("\t") > -1) {
                    String line = linestr.split("\t")[0];
                    String[] values = line.split(",");
                    Long infoID = Long.valueOf(values[0]);
                    if (infoset.contains(infoID)) {
                        System.out.println("id--" + infoID);
                        dupcnt++;
                    } else {
                        if (String.valueOf(infoID).endsWith("9")) {
                            flag++;
                        }
                        infoset.add(infoID);
                    }
                }
            }
            System.out.println("\n\n");
            System.out.println("raw total= " + list.size());
            System.out.println("duplicate= " + dupcnt);
            System.out.println("filter total-- " + infoset.size());
            System.out.println("flag total-- " + flag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
