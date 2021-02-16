package com.crawler.law.core;

import com.crawler.law.model.Law;

import java.util.concurrent.ConcurrentLinkedDeque;

public final class ShareQueue {
    public static ConcurrentLinkedDeque<Law> shareQueueItem = new ConcurrentLinkedDeque<>();
}
