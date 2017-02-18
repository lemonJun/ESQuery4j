package lemon.elastic4j.test.cv;

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

import com.alibaba.fastjson.JSON;
import com.bj58.chr.scf.cvsearch.entity.CVIndexBean;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.util.concurrent.RateLimiter;

import core.ESTemplateClient;
import lemon.elastic.query4j.BootStrap;
import lemon.elastic.query4j.esproxy.core.query.IndexQuery;
import lemon.elastic.query4j.util.LngLatUtil;
import lemon.elastic.query4j.util.StringUtil;

public class CvIndexTwo {

    private static final Logger logger = LoggerFactory.getLogger(CvIndexTwo.class);

    public static final ExecutorService executor = Executors.newFixedThreadPool(50);
    static CompletionService<IndexQuery> completion = new ExecutorCompletionService<IndexQuery>(executor);

    private static final int INDEX_CNT_EVERY_TIME = 100;

    static RateLimiter limit = RateLimiter.create(100.0d);

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

        new CvIndexTwo().index();
    }

    public static void log() {
        logger.info(String.format("statics read:%d , index:%d", total.get(), successCnt.get()));
    }

    public void index() {
        try {
            Executors.newFixedThreadPool(1).submit(new ESThread());
            Files.readLines(new File("D:\\es\\data\\1.txt"), Charsets.UTF_8, new CVLine());

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
                    CVIndexBean bean = JSON.parseObject(line, CVIndexBean.class);
                    //                    bean.setExplocal(bean.getExplocal().replaceAll(",", " "));
                    String location = String.format("%s,%s", LngLatUtil.randomGenLat(39.797068, 40.033924), LngLatUtil.randomGenLng(116.218571, 116.557771));

                    bean.setLatlng(location);
                    IndexQuery query = new IndexQuery();
                    query.setId(String.valueOf(bean.getCvid()));
                    query.setObject(bean);
                    queue.offer(query);
                    total.getAndIncrement();
                    if (total.get() > 500) {
                        return false;
                    }
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
                        ESTemplateClient.getInstance().getTemplate().refresh(CVIndexBean.class, true);
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
                    ESTemplateClient.getInstance().getTemplate().refresh(CVIndexBean.class, true);
                    indexQueries.clear();
                    logger.info(String.format("index resume into elastic ,cnt = %d ", successCnt.get()));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
