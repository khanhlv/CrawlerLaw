package com.crawler.law.thread;


import com.crawler.law.core.ShareQueue;
import com.crawler.law.dao.LawDAO;
import com.crawler.law.model.Law;
import com.crawler.law.parser.ThuKyLuatParser;
import com.crawler.law.util.GZipUtil;
import com.crawler.law.util.GoogleDriverUtil;
import com.crawler.law.util.ResourceUtil;
import com.google.api.services.drive.Drive;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class ThreadLawDetail implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ThreadLawDetail.class);
    private ThuKyLuatParser thuKyLuatParser = new ThuKyLuatParser();
    private LawDAO lawDAO = new LawDAO();
    private Drive.Files driveFiles;
    private String threadName = "THREAD_DATA_";

    public ThreadLawDetail(int threadCount, Drive.Files driveFiles) {
        this.threadName = this.threadName + threadCount;
        this.driveFiles = driveFiles;
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
                    String fileId = "";
                    try {
                        long start = System.currentTimeMillis();

                        Law content = thuKyLuatParser.readDetail(link, data.getId());

                        if (content == null) {
                            lawDAO.updateStatus(data.getId(), -1);
                        } else {
                            if (StringUtils.isNotBlank(content.getContent())) {
                                InputStream inputStream = GZipUtil.compress(content.getContent());

                                fileId = GoogleDriverUtil.uploadFile(driveFiles, inputStream,content.getId() + ".html.gz", ResourceUtil.getValue("google.driver.folder"));
                            }

                            content.setGoogleDriveId(fileId);

                            lawDAO.update(content);
                        }

                        long end = System.currentTimeMillis() - start;

                        logger.debug(this.threadName + "## GET ID["+data.getId()+"][URL=" +link + "][TIME=" + end  + "]");

                    } catch (Exception ex) {
                        logger.error(this.threadName + " ## ERROR[" + link + "]", ex);

                        if (StringUtils.isNotBlank(fileId)) {
                            GoogleDriverUtil.deleteFile(driveFiles, fileId);
                        }

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
