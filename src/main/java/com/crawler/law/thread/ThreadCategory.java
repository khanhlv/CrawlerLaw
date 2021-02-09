package com.crawler.law.thread;

import java.sql.SQLException;
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

    @Override
    public void run() {
        try {
            List<Law> laws = thuKyLuatParser.readQuery("https://thukyluat.vn/tim-kiem/?page=19054");

            for (Law law : laws) {
                lawDAO.insertCategory(law);
            }
        } catch (Exception ex) {
            logger.error("ThreadCategory" , ex);
        }
    }
}
