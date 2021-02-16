package com.crawler.law.thread;


import com.crawler.law.core.ShareQueue;
import com.crawler.law.dao.LawDAO;
import com.crawler.law.model.Law;
import com.crawler.law.parser.ThuKyLuatParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ThreadLawDetail implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ThreadLawDetail.class);
    private ThuKyLuatParser thuKyLuatParser = new ThuKyLuatParser();
    private LawDAO lawDAO = new LawDAO();

    private String threadName = "THREAD_DATA_";

    public ThreadLawDetail(int threadCount) {
        this.threadName = this.threadName + threadCount;
        System.out.println("START_THREAD_DATA_" + threadCount);
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("SHARE_QUEUE_ITEM_SIZE[" + ShareQueue.shareQueueItem.size() + "]");

                if (ShareQueue.shareQueueItem.size() > 0) {
                    Law data = ShareQueue.shareQueueItem.poll();

                    String link = "http://thukyluat.vn" + data.getCrawlerSource();
                    try {

                        logger.debug(this.threadName + "## GET_START [URL=" + link + "]");

                        long start = System.currentTimeMillis();

                        Law content = thuKyLuatParser.readDetail(link, data.getId());

                        long end = System.currentTimeMillis() - start;

                        logger.debug(this.threadName + "## GET_END [URL=" +link + "][TIME=" + end  + "]");

                        if (content == null) {
                            lawDAO.updateStatus(data.getId(), -1);
                        } else {
                            lawDAO.update(content);
                        }

                    } catch (Exception ex) {
                        logger.error(this.threadName + " ## ERROR[" + link + "]", ex);
                        lawDAO.updateStatus(data.getId(), -1);
                    }
                }

                Thread.sleep(500);
            }
        } catch (Exception ex) {
            logger.error(this.threadName + " ## ERROR[ThreadLawDetail]", ex);
        }
    }
}
