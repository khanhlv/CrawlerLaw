package com.crawler.law.dao;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawler.law.core.ConnectionPool;
import com.crawler.law.model.Law;
import com.crawler.law.util.StringUtil;

public class LawDAO {

    private static final Logger logger = LoggerFactory.getLogger(LawDAO.class);

    public void insertCategory(Law law) throws SQLException {

        if (checkExists(law.getCrawlerSource())) {
            return;
        }

        String sqlStory = "INSERT INTO LAW(LAW_NAME,LAW_DATE_ISSUED,LAW_UPDATED_DATE,META_URL,META_TITLE,META_DESCRIPTION,META_KEYWORD,CRAWLER_CATEGORY,CRAWLER_SOURCE,STATUS) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory, Statement.RETURN_GENERATED_KEYS)) {

            pStmt.setString(1, law.getName());
            pStmt.setDate(2, new Date(law.getDateIssued().getTime()));
            pStmt.setDate(3, new Date(law.getUpdatedDate().getTime()));
            pStmt.setString(4, StringUtil.stripAccents(law.getName(), "-"));
            pStmt.setString(5, law.getName());
            pStmt.setString(6, law.getName());
            pStmt.setString(7, law.getName());
            pStmt.setString(8, law.getCrawlerCategoryName());
            pStmt.setString(9, law.getCrawlerSource());
            pStmt.setInt(10, 0);

            pStmt.executeUpdate();

            ResultSet genKeysRs = pStmt.getGeneratedKeys();

            if (genKeysRs.next()) {
                int lawId = genKeysRs.getInt(1);

                for(String data : law.getCategory()) {
                    insertLawCategory(lawId, NumberUtils.toInt(data));
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void insertLawCategory(int id, int category) throws SQLException {
        String sqlStory = "INSERT INTO LAW_CATEGORY VALUES (?,?)";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setInt(1, id);
            pStmt.setInt(2, category);

            pStmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public boolean checkExists(String crawlerSouce) throws SQLException {
        String sqlStory = "SELECT * FROM LAW WHERE CRAWLER_SOURCE = ?";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setString(1, crawlerSouce);

            ResultSet rs = pStmt.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (Exception ex) {
            throw ex;
        }

        return false;
    }
}
