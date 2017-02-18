package lemon.elastic4j.test.chew;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class UserInfoDuplicateTest {

    @Test
    public void userDuplicate() {
        Map<Long, Set<Long>> map = new HashMap<Long, Set<Long>>();
        try {
            List<String> lines = Files.readLines(new File("D:/20150810"), Charsets.UTF_8);
            System.out.println("lines = " + lines.size());

            for (String line : lines) {
                if (line != null && line.length() > 0) {
                    String value = line.split("\t")[0];
                    String[] values = value.split(",");
                    long infoid = Long.parseLong(values[0]);
                    long uid = Long.parseLong(values[3]);
                    Set<Long> infos = map.get(uid);
                    if (infos == null) {
                        infos = new HashSet<Long>();
                        map.put(uid, infos);
                    }
                    infos.add(infoid);
                }
            }

            //
            Map<Integer, Integer> cntMap = new HashMap<Integer, Integer>();
            for (Long key : map.keySet()) {
                int size = map.get(key).size();
                Integer cnt = cntMap.get(size);
                if (cnt == null) {
                    cntMap.put(size, 1);
                } else {
                    cntMap.put(size, cnt + 1);
                }
            }
            int cnt = 0;
            for (Integer key : cntMap.keySet()) {
                System.out.println(key + "," + cntMap.get(key));
                cnt += key * cntMap.get(key);
            }

            System.out.println("\n cnt=" + cnt);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
