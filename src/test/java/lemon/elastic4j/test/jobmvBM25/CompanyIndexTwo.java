package lemon.elastic4j.test.jobmvBM25;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.util.concurrent.RateLimiter;

import core.ESTemplateClient;
import lemon.elastic.query4j.BootStrap;
import lemon.elastic.query4j.esproxy.core.query.IndexQuery;
import lemon.elastic.query4j.esproxy.core.query.IndexQueryBuilder;
import lemon.elastic.query4j.util.StringUtil;

public class CompanyIndexTwo {

    private static final Logger logger = LoggerFactory.getLogger(CompanyIndexTwo.class);

    public static final ExecutorService executor = Executors.newFixedThreadPool(50);
    static CompletionService<IndexQuery> completion = new ExecutorCompletionService<IndexQuery>(executor);

    private static final int INDEX_CNT_EVERY_TIME = 1000;

    static RateLimiter limit = RateLimiter.create(1000.0d);

    static AtomicInteger total = new AtomicInteger(0);
    static AtomicInteger successCnt = new AtomicInteger(0);

    private static LinkedBlockingQueue<IndexQuery> queue = new LinkedBlockingQueue<IndexQuery>();

    public static void main(String[] args) {
        BootStrap.init();

        ScheduledExecutorService respScheduler = new ScheduledThreadPoolExecutor(1);

        respScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log();
            }
        }, 10, 5, TimeUnit.SECONDS);

        new CompanyIndexTwo().index();
    }

    public static void log() {
        logger.info(String.format("statics read:%d , index:%d", total.get(), successCnt.get()));
    }

    public void index() {
        try {
            Executors.newFixedThreadPool(1).submit(new ESThread());
            Files.readLines(new File("D:\\mv2\\mvcheck"), Charsets.UTF_8, new CVLine());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class CVLine implements LineProcessor<String> {
        @Override
        public boolean processLine(String line) throws IOException {
            try {
                if (StringUtil.isNotNullOrEmpty(line)) {
                    limit.acquire();
                    //                    bean.setExplocal(bean.getExplocal().replaceAll(",", " "));
                    String[] arys = line.split(",");
                    if (arys.length < 4) {
                        System.out.println(line);
                        return true;
                    }

                    CompanyEsInfoV2 bean = new CompanyEsInfoV2();
                    //                    bean.setId(arys[0]);
                    bean.setId(arys[0]);
                    long time = Long.parseLong(arys[1]);
                    if (time < 1473004800) {
                        //                        System.out.println(time + "<1473004800");
                        return true;
                    }
                    bean.setAddTime(time);
                    bean.setSource(arys[2]);
                    bean.setComName(arys[3]);
                    IndexQuery query = new IndexQueryBuilder().withId(bean.getId()).withObject(bean).build();
                    queue.offer(query);
                    total.getAndIncrement();
                    //                    if (total.get() > 100) {
                    //                        return false;
                    //                    }
                } else {
                    logger.info("error:" + line);
                }
            } catch (Exception e) {
                logger.info("error:" + line);
            }
            return true;
        }

        @Override
        public String getResult() {
            return null;
        }
    }

    class ESThread implements Runnable {
        @Override
        public void run() {
            logger.info("start queue thread");
            List<IndexQuery> indexQueries = new ArrayList<IndexQuery>();
            try {
                while (true) {
                    IndexQuery query = queue.poll(5, TimeUnit.SECONDS);
                    indexQueries.add(query);
                    successCnt.getAndIncrement();
                    if (successCnt.get() % INDEX_CNT_EVERY_TIME == 0) {
                        long startime = System.currentTimeMillis();
                        List<IndexQuery> inlist = new ArrayList<IndexQuery>(Arrays.asList(new IndexQuery[indexQueries.size()]));
                        Collections.copy(inlist, indexQueries);
                        ESTemplateClient.getInstance().getTemplate().bulkIndex(inlist);
                        ESTemplateClient.getInstance().getTemplate().refresh(CompanyEsInfoV2.class, true);
                        logger.info(String.format("index into elastic cnt:%d ,ms:%d", successCnt.get(), (System.currentTimeMillis() - startime)));
                        indexQueries.clear();
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            try {
                if (indexQueries.size() > 0) {
                    ESTemplateClient.getInstance().getTemplate().bulkIndex(indexQueries);
                    ESTemplateClient.getInstance().getTemplate().refresh(CompanyEsInfoV2.class, true);
                    indexQueries.clear();
                    logger.info(String.format("index resume into elastic ,cnt = %d ", successCnt.get()));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
