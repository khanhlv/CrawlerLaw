package com.crawler.law;

import com.crawler.law.enums.ThreadMod;
import com.crawler.law.thread.StartThread;

public class Application {
    public static void main(String[] args) throws Exception {
        String threadMod = args[0];

        new StartThread().execute(ThreadMod.valueOf(threadMod));
    }
}
