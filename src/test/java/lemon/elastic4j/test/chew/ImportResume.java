package lemon.elastic4j.test.chew;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import core.ESTemplateClient;
import lemon.elastic.query4j.esproxy.core.query.IndexQuery;

public class ImportResume {

    @Test
    public void imports() {
        try {
            List<IndexQuery> indexQueries = new ArrayList<IndexQuery>();
            List<String> lines = Files.readLines(new File("D:/resumes.txt"), Charsets.UTF_8);
            long resumeid = 0l;
            long uid = 0;
            String farea = "";
            String fcate = "";
            Date date = null;
            int exp = 1;
            int edu = 1;
            int sal = 1;
            String location = "";
            int age = 18;
            int gender = 0;
            int complete = 0;
            for (String line : lines) {
                if (line.indexOf("#") > -1) {
                    continue;
                }
                try {
                    String[] values = line.split(",");
                    resumeid = Long.parseLong(values[0].trim());
                    uid = Long.parseLong(values[1].trim());
                    farea = values[2];
                    fcate = values[3];
                    date = null;//DateUtil.fulldf.parse(values[4]);
                    exp = Integer.parseInt(values[5]);
                    edu = Integer.parseInt(values[6]);
                    sal = Integer.parseInt(values[7]);
                    location = values[8];
                    age = Integer.parseInt(values[9]);
                    gender = Integer.parseInt(values[10]);
                    complete = Integer.parseInt(values[11]);
                    IndexQuery query = new ElasticResumeBuilder().id(resumeid).uid(uid).farea(farea).fcate(fcate).age(age).exp(exp).edu(edu).date(date).sal(sal).gender(gender).location(location).complete(complete).buildIndex();
                    System.out.println(JSON.toJSONString(query));
                    indexQueries.add(query);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ESTemplateClient.getInstance().getTemplate().bulkIndex(indexQueries);
            ESTemplateClient.getInstance().getTemplate().refresh(ElasticResume.class, true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
