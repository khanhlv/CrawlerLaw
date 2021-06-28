package com.crawler.law.thread;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawler.law.core.ShareQueue;
import com.crawler.law.dao.LawDAO;
import com.crawler.law.model.Law;
import com.crawler.law.parser.ThuKyLuatParser;
import com.crawler.law.parser.VanBanPhapLuatCoParser;

public class ThreadLawStatusExpired implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ThreadLawStatusExpired.class);
    private VanBanPhapLuatCoParser vanBanPhapLuatCoParser = new VanBanPhapLuatCoParser();
    private LawDAO lawDAO = new LawDAO();
    private String threadName = "THREAD_DATA_STATUS_";

    public ThreadLawStatusExpired(int threadCount) {
        this.threadName = this.threadName + threadCount;
        System.out.println("START_THREAD_DATA_STATUS" + threadCount);
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("SHARE_QUEUE_STATUS_ITEM_SIZE[" + ShareQueue.shareQueueStatusItem.size() + "]");

                if (ShareQueue.shareQueueStatusItem.size() > 0) {
                    Law data = ShareQueue.shareQueueStatusItem.poll();

                    try {
                        long start = System.currentTimeMillis();

                        Law content = vanBanPhapLuatCoParser.readDetail(data);

                        String status = "1";
                        if (content == null) {
                            lawDAO.updateStatusExpired(data.getId(), 2);
                            status = "2";
                        } else {
                            lawDAO.updateContentExpired(content);
                        }

                        long end = System.currentTimeMillis() - start;

                        logger.debug(this.threadName + "## GET ID["+data.getId()+"][TIME=" + end  + "][STATUS="+status+"]");

                    } catch (Exception ex) {
                        logger.error(this.threadName + " ## ERROR[" + data.getId() + "]", ex);

                        lawDAO.updateStatusExpired(data.getId(), -1);
                    }
                }

                Thread.sleep(500);
            }
        } catch (Exception ex) {
            logger.error(this.threadName + " ## ERROR[ThreadLawStatusExpired]", ex);
        }
    }
}
