package com.crawler.law.thread;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawler.law.dao.LawDAO;
import com.crawler.law.model.Law;
import com.crawler.law.parser.ThuKyLuatParser;

public class ThreadCategory implements Runnable  {
    private static final Logger logger = LoggerFactory.getLogger(ThreadCategory.class);
    private LawDAO lawDAO = new LawDAO();
    private ThuKyLuatParser thuKyLuatParser = new ThuKyLuatParser();

    private int page = 10;

    public ThreadCategory(int page) {
        this.page = page;
        System.out.println("START_THREAD_DATA_CATEGORY" );
    }

    @Override
    public void run() {

            String url = "https://thukyluat.vn/tim-kiem/?page=%s";

            for (int i = 1; i <= page; i++) {
                try {
                    logger.info(String.format(url, i));
                    List<Law> laws = thuKyLuatParser.readQuery(String.format(url, i));

                    for (Law law : laws) {
                        lawDAO.insertCategory(law);
                    }
                } catch (Exception ex) {
                    logger.error("ERROR_PAGE [{}]", String.format(url, i), ex);
                }
            }

    }
}
