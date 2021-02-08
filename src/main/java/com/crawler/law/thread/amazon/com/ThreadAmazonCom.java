package com.crawler.law.thread.amazon.com;


import com.crawler.law.core.ShareQueue;
import com.crawler.law.dao.CrawlerDAO;
import com.crawler.law.dao.DataDAO;
import com.crawler.law.enums.Crawler;
import com.crawler.law.model.Data;
import com.crawler.law.parser.AmazonComParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ThreadAmazonCom implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ThreadAmazonCom.class);
    private AmazonComParser amazonParser = new AmazonComParser();
    private DataDAO dataDAO = new DataDAO();
    private CrawlerDAO crawlerDAO = new CrawlerDAO();

    private String threadName = "THREAD_";

    public ThreadAmazonCom(int threadCount) {
        this.threadName = this.threadName + threadCount;
        System.out.println("START_THREAD_" + threadCount);
    }

    @Override
    public void run() {
        try {
            while (true) {
                List<String> listContent = ShareQueue.getItem();

                System.out.println(this.threadName + " ## LINK_SIZE [" + listContent.size() + "]");

                for(String data : listContent){
                    String[] str = StringUtils.split(data,"\\|");
                    int id = NumberUtils.toInt(str[0]);
                    String link = str[1];
                    String category = str[2];
                    try {

                        logger.debug(this.threadName + " ## GET_START [URL=" + link + "]");

                        link = link.replaceAll(Crawler.AMAZON_COM.getSite() + "/s", "https://www.amazon.com/s/query");

                        List<Data> listData = amazonParser.readQuery(link);

                        if (listData.size() > 0) {
                            for (Data result : listData) {
                                result.setCategory(category);
                                dataDAO.insert(result);
                            }
                        } else {
                            crawlerDAO.updateQueueStatus(id, 2);
                        }

                        logger.debug(this.threadName + " ## GET_END [URL={}] [SIZE={}]", link, listData.size());
                    } catch (Exception ex) {
                        logger.error(this.threadName + " ## ERROR[" + link + "]", ex);
                        crawlerDAO.updateQueueStatus(id, -1);
                    }
                    Thread.sleep( 500);
                }

                Thread.sleep( 1000);
            }
        } catch (Exception ex) {
            logger.error(this.threadName + " ## ERROR[ThreadAmazonCom]", ex);
        }
    }
}
