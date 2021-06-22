package com.crawler.law.thread;

import com.crawler.law.core.ShareQueue;
import com.crawler.law.dao.LawDAO;
import com.crawler.law.enums.ThreadMod;
import com.crawler.law.model.Law;
import com.crawler.law.util.GoogleDriverUtil;
import com.crawler.law.util.ResourceUtil;
import com.google.api.services.drive.Drive;
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
                Drive.Files driveFiles = GoogleDriverUtil.driveFiles();

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
                    new Thread(new ThreadLawDetail(i, driveFiles)).start();
                    Thread.sleep(5000);
                }
                break;
            case SINGLE_CATEGORY:

                int page = NumberUtils.toInt(ResourceUtil.getValue("data.limit.category.page"), 10); //Phut

                TimerTask timerTaskCategory = new TimerTask() {
                    @Override
                    public void run() {
                        new Thread(new ThreadCategory(page)).start();
                        System.out.println("...............................................................");
                    }
                };

                int timeCategory = NumberUtils.toInt(ResourceUtil.getValue("data.time.get.category"), 1); //Phut

                Timer timerCategory = new Timer();
                timerCategory.schedule(timerTaskCategory, 0, timeCategory * 60 * 1000);

                break;

            case STATUS_EXPIRED:
                TimerTask timerTaskStatus = new TimerTask() {
                    @Override
                    public void run() {
                        if (ShareQueue.shareQueueStatusItem.size() == 0) {
                            try {
                                int limit = limitCount;

                                List<Law> contentList = lawDAO.queueListStatusExpired(limit);

                                ShareQueue.shareQueueStatusItem.addAll(contentList);
                            } catch (SQLException e) {
                                logger.error("StartThread", e);
                            }
                        }
                    }
                };

                int timeStatus = NumberUtils.toInt(ResourceUtil.getValue(" data.time.get.record"), 1); //Phut

                Timer timerStatus = new Timer();
                timerStatus.schedule(timerTaskStatus, 0, timeStatus * 60 * 1000);

                for (int i = 1; i <= threadDataCount; i++) {
                    new Thread(new ThreadLawStatusExpired(i)).start();
                    Thread.sleep(5000);
                }

                break;
        }

    }
}
