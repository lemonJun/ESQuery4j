package lemon.elastic4j.test.cv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.bj58.chr.scf.cvsearch.entity.CVIndexBean;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import core.ESTemplateClient;
import lemon.elastic.query4j.BootStrap;
import lemon.elastic.query4j.esproxy.core.query.IndexQuery;
import lemon.elastic.query4j.util.StringUtil;
import lemon.elastic4j.test.chew.ElasticResume;

public class CvIndex {
    private static final Logger logger = LoggerFactory.getLogger(CvIndex.class);

    public static final ExecutorService executor = Executors.newFixedThreadPool(50);
    static CompletionService<IndexQuery> completion = new ExecutorCompletionService<IndexQuery>(executor);

    private static final int INDEX_CNT_EVERY_TIME = 100;

    AtomicInteger total = new AtomicInteger(0);
    AtomicInteger successCnt = new AtomicInteger(0);
    AtomicInteger failCnt = new AtomicInteger(0);
    AtomicInteger lonlatCnt = new AtomicInteger(0);
    AtomicInteger zerosal = new AtomicInteger(0);

    public static void main(String[] args) {
        BootStrap.init();
        new CvIndex().index();
    }

    public void index() {
        try {
            Executors.newSingleThreadExecutor().submit(new CompletionThread(completion));
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
                    CVIndexBean bean = JSON.parseObject(line, CVIndexBean.class);
                    completion.submit(new ResumeCallabel(bean));
                }
            } catch (Exception e) {
                System.out.println(line);
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public String getResult() {
            return null;
        }

    }

    class ResumeCallabel implements Callable<IndexQuery> {

        private CVIndexBean bean;

        public ResumeCallabel(CVIndexBean bean) {
            this.bean = bean;
        }

        @Override
        public IndexQuery call() throws Exception {
            try {
                IndexQuery query = new IndexQuery();
                query.setId(String.valueOf(bean.getCvid()));
                query.setObject(bean);
                return query;
            } catch (Exception e) {
                logger.error("", e);
                return null;
            }
        }

    }

    class CompletionThread implements Runnable {

        private CompletionService<IndexQuery> completion;

        public CompletionThread(CompletionService<IndexQuery> completion) {
            this.completion = completion;
        }

        @Override
        public void run() {
            List<IndexQuery> indexQueries = new ArrayList<IndexQuery>();
            int cnt = 0;
            while (true) {
                cnt++;
                try {
                    Future<IndexQuery> future = completion.poll(60 * 1000, TimeUnit.MILLISECONDS);
                    if (future == null) {
                        break;
                    }
                    IndexQuery query = future.get();
                    if (query == null) {
                        continue;
                    }
                    successCnt.getAndIncrement();
                    indexQueries.add(query);
                    if (cnt % INDEX_CNT_EVERY_TIME == 0) {
                        long startime = System.currentTimeMillis();
                        //如果里面的值太多 说明elasticsearch可能已经挂掉了

                        List<IndexQuery> inlist = new ArrayList<IndexQuery>(Arrays.asList(new IndexQuery[indexQueries.size()]));
                        Collections.copy(inlist, indexQueries);
                        ESTemplateClient.getInstance().getTemplate().bulkIndex(inlist);
                        ESTemplateClient.getInstance().getTemplate().refresh(ElasticResume.class, true);
                        logger.info(String.format("index resume into elastic ,cnt = %d ,usetime= %d", cnt, (System.currentTimeMillis() - startime)));
                        indexQueries.clear();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    break;
                }
            }
            try {
                if (indexQueries.size() > 0 && indexQueries.size() < 10 * INDEX_CNT_EVERY_TIME) {
                    ESTemplateClient.getInstance().getTemplate().bulkIndex(indexQueries);
                    ESTemplateClient.getInstance().getTemplate().refresh(ElasticResume.class, true);
                    indexQueries.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            logger.info("\n\n");
            logger.info(String.format("tatol=%d ,successCnt=%d ,failCnt=%d ,lonlattatol=%d ,mianyitotal=%d ", total.get(), successCnt.get(), failCnt.get(), lonlatCnt.get(), zerosal.get()));
            logger.info(String.format("successRate=%f , lonlatRate=%f , mianyirate=%f ", (float) successCnt.get() / total.get(), (float) lonlatCnt.get() / total.get(), (float) zerosal.get() / total.get()));
        }

    }

}
