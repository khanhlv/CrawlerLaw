package com.crawler.law.thread;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawler.law.core.ShareQueue;
import com.crawler.law.dao.CrawlerDAO;
import com.crawler.law.model.Queue;
import com.crawler.law.util.ResourceUtil;

public class ThreadShareQueue implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ThreadShareQueue.class);
    private CrawlerDAO crawlerDAO = new CrawlerDAO();
    private static final String PATH_QUEUE = "data/queue.txt";

    public ThreadShareQueue(){
        System.out.println("START_THREAD_QUEUE");
    }

    @Override
    public void run() {
        try {
            File fileQueue = new File(PATH_QUEUE);

            if (fileQueue.exists()) {
                List<String> stringList = FileUtils.readLines(fileQueue, "UTF-8");

                ShareQueue.shareQueue.addAll(stringList);
            }

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (ShareQueue.shareQueue.size() == 0) {
                        try {
                            if (crawlerDAO.countQueueCrawler() == 0) {
                                crawlerDAO.updateAllQueueStatus(0);
                            }
                        } catch (SQLException e) {

                        }
                    }
                }
            };

            int time = NumberUtils.toInt(ResourceUtil.getValue("data.crawler.queue.time"));

            Timer timer = new Timer();
            timer.schedule(timerTask, 0, time * 60 * 60 * 1000);

            while (true) {
                if (ShareQueue.shareQueue.size() < ShareQueue.QUEUE_SIZE_LIMIT) {
                    int limit = ShareQueue.QUEUE_SIZE_LIMIT - ShareQueue.shareQueue.size();

                    List<Queue> queueList = crawlerDAO.queueList(limit);

                    if (queueList != null && queueList.size() > 0) {
                        ShareQueue.addItem(queueList);
                    }

                    FileUtils.writeLines(fileQueue, "UTF-8", ShareQueue.shareQueue);
                }

                System.out.println("SHARE_QUEUE=" + ShareQueue.shareQueue.size());

                Thread.sleep(2 * 60 * 1000);
            }
        } catch (Exception ex) {
            logger.error("ERROR[ThreadShareQueue]", ex);
        }
    }
}
