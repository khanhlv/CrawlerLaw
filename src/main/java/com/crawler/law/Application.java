package com.crawler.law;

import com.crawler.law.thread.ThreadCategory;

public class Application {
    public static void main(String[] args) throws Exception {
        new ThreadCategory().run();
    }
}
