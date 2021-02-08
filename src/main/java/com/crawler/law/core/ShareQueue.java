package com.crawler.law.core;

import com.crawler.law.model.Data;
import com.crawler.law.model.Queue;
import com.crawler.law.util.ResourceUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class ShareQueue {
    public static ConcurrentLinkedDeque<String> shareQueue = new ConcurrentLinkedDeque<>();
    public static ConcurrentLinkedDeque<Data> shareQueueItem = new ConcurrentLinkedDeque<>();
    public static List<InetSocketAddress> socketAddressList = new LinkedList<>();
    public final static int QUEUE_SIZE = NumberUtils.toInt(ResourceUtil.getValue("data.crawler.queue.size"));
    public final static int QUEUE_SIZE_LIMIT = NumberUtils.toInt(ResourceUtil.getValue("data.crawler.queue.limit"));

    public static void addItem(List<Queue> queueList) {
        if (shareQueue.size() < QUEUE_SIZE_LIMIT) {
            for (Queue queue : queueList) {
                String link = queue.getId() + "|" + queue.getLink() + "|" + queue.getName();
                if (!shareQueue.contains(link)) {
                    shareQueue.add(link);
                }
            }
        }
    }

    public static List<String> getItem() throws IOException {
        List<String> listItem = new ArrayList<>();

        int size = shareQueue.size() > QUEUE_SIZE ? QUEUE_SIZE : shareQueue.size();

        for (int i = 0; i < size; i++) {
            listItem.add(shareQueue.poll());
        }

        FileUtils.writeLines(new File("data/queue.txt"), "UTF-8", ShareQueue.shareQueue);

        return listItem;
    }
}
