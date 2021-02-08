package com.crawler.law.thread;

import com.crawler.law.core.ShareQueue;
import com.crawler.law.dao.DataDAO;
import com.crawler.law.model.Data;
import com.crawler.law.util.ResourceUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ThreadShareItemQueue implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ThreadShareItemQueue.class);
    private DataDAO dataDAO = new DataDAO();

    public ThreadShareItemQueue(){
        System.out.println("START_THREAD_QUEUE_ITEM");
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (ShareQueue.shareQueueItem.size() == 0) {

                    int limit = NumberUtils.toInt(ResourceUtil.getValue("data.crawler.data.limit"));

                    List<Data> contentList = dataDAO.queueList(limit);

                    ShareQueue.shareQueueItem.addAll(contentList);
                }

                System.out.println("SHARE_QUEUE_ITEM=" + ShareQueue.shareQueueItem.size());

                int time = NumberUtils.toInt(ResourceUtil.getValue("data.crawler.queue.data.time"));

                Thread.sleep(time * 60 * 1000);
            }
        } catch (Exception ex) {
            logger.error("ERROR[ThreadShareQueue]", ex);
        }
    }
}
