package com.crawler.law.thread;

import com.crawler.law.core.ShareQueue;
import com.crawler.law.dao.LawDAO;
import com.crawler.law.enums.ThreadMod;
import com.crawler.law.model.Law;
import com.crawler.law.util.ResourceUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StartThread {
    private LawDAO lawDAO = new LawDAO();
    private static final Logger logger = LoggerFactory.getLogger(StartThread.class);

    public void execute(ThreadMod threadMod) throws Exception {

        int threadDataCount = NumberUtils.toInt(ResourceUtil.getValue("data.crawler.detail.thread"), 1);

        int limitCount = NumberUtils.toInt(ResourceUtil.getValue("data.limit.record"), 1);

        switch (threadMod) {
            case SINGLE_DETAIL:

                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (ShareQueue.shareQueueItem.size() == 0) {
                            try {
                                int limit = limitCount;

                                List<Law> contentList = lawDAO.queueList(limit);

                                ShareQueue.shareQueueItem.addAll(contentList);
                            } catch (SQLException e) {
                                logger.error("StartThread", e);
                            }
                        }
                    }
                };

                int time = NumberUtils.toInt(ResourceUtil.getValue(" data.time.get.record"), 1); //Phut

                Timer timer = new Timer();
                timer.schedule(timerTask, 0, time * 60 * 1000);

                for (int i = 1; i <= threadDataCount; i++) {
                    new Thread(new ThreadLawDetail(i)).start();
                    Thread.sleep(5000);
                }
                break;
            case SINGLE_CATEGORY:
                new Thread(new ThreadCategory(10)).start();
                break;
        }

    }
}
