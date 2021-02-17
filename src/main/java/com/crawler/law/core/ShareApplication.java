package com.crawler.law.core;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawler.law.dao.LawDAO;

public class ShareApplication {
    private static final Logger logger = LoggerFactory.getLogger(ShareApplication.class);

    public static Map<String, String> CATEGORY_MAP = new HashMap<>();
    public static Map<String, String> AGENCY_MAP = new HashMap<>();
    public static Map<String, String> TYPE_MAP = new HashMap<>();
    public static LawDAO lawDAO = new LawDAO();

    static {
        try {
            CATEGORY_MAP = lawDAO.selectCategoryAll();
            AGENCY_MAP = lawDAO.selectAgencyAll();
            TYPE_MAP = lawDAO.selectTypeAll();
        } catch (SQLException e) {
            logger.error("ERROR[ShareApplication]", e);
        }
    }
}
