package com.crawler.law.thread.amazon.com;


import com.crawler.law.core.ShareQueue;
import com.crawler.law.dao.DataDAO;
import com.crawler.law.model.Data;
import com.crawler.law.parser.AmazonComParser;
import com.crawler.law.proxy.ProxyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;

public class ThreadAmazonComDetail implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ThreadAmazonComDetail.class);
    private AmazonComParser amazonParser = new AmazonComParser();
    private DataDAO dataDAO = new DataDAO();

    private String threadName = "THREAD_DATA_";

    public ThreadAmazonComDetail(int threadCount) {
        this.threadName = this.threadName + threadCount;
        System.out.println("START_THREAD_DATA_" + threadCount);
    }

    @Override
    public void run() {
        try {
            while (true) {


                if (ShareQueue.shareQueueItem.size() > 0) {
                    List<InetSocketAddress> inetSocketAddresses = ProxyProvider.proxyList();

                    if (inetSocketAddresses != null && inetSocketAddresses.size() > 0) {
                        Data data = ShareQueue.shareQueueItem.poll();

                        try {
                            logger.debug(this.threadName + " GET_START [{}]", data.getLink());

                            long start = System.currentTimeMillis();

                            boolean hasContent = false;

                            for (InetSocketAddress socketAddress : inetSocketAddresses) {
                                Data content = null;
                                try {
                                    content = amazonParser.readDetail(data.getLink(), data.getCode(), data.getId(), socketAddress);
                                } catch (Exception ex) {

                                }

                                if (content != null) {
                                    if (content.getPrice() > 0) {
                                        dataDAO.updateData(content, 1);
                                    } else {
                                        content.setPrice(data.getPrice());

                                        dataDAO.updateData(content, 1);
                                    }
                                    hasContent = true;
                                    break;
                                }
                            }

                            if (!hasContent) {
                                dataDAO.updateDataStatus(data.getId(), -1);
                            }

                            long end = System.currentTimeMillis() - start;

                            logger.debug(this.threadName + " GET_END [{}] TIME[{}] SIZE[{}]", data.getLink(), end, ShareQueue.shareQueueItem.size());

                        } catch (SQLException ex) {
                            logger.error(this.threadName + " ERROR[{}]", data.getLink(), ex);
                            dataDAO.updateDataStatus(data.getId(), -1);
                        }
                    }
                } else {
                    System.out.println(this.threadName + "_WAITING......");
                }
            }
        } catch (Exception ex) {
            logger.error(this.threadName + " ## ERROR[ThreadAmazonComDetail]", ex);
        }
    }
}
