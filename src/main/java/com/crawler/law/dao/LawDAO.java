package com.crawler.law.dao;

import com.crawler.law.core.ConnectionPool;
import com.crawler.law.model.Law;
import com.crawler.law.util.StringUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LawDAO {

    private static final Logger logger = LoggerFactory.getLogger(LawDAO.class);

    public List<Law> queueList(int limit) throws SQLException {

        List<Law> dataList = new ArrayList<>();
        String sqlStory = "SELECT TOP "+ limit+" * FROM LAW WHERE STATUS = 0 ORDER BY LAW_UPDATED_DATE DESC";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            ResultSet resultSet = pStmt.executeQuery();
            while(resultSet.next()) {

                Law data = new Law();
                data.setId(resultSet.getLong("LAW_ID"));
                data.setCrawlerSource(resultSet.getString("CRAWLER_SOURCE"));

                data.setStatus(resultSet.getInt("STATUS"));

                dataList.add(data);
            }

            resultSet.close();
        } catch (Exception ex) {
            throw ex;
        }

        return dataList;
    }

    public List<Law> queueListStatusExpired(int limit) throws SQLException {

        List<Law> dataList = new ArrayList<>();
        String sqlStory = "SELECT TOP "+ limit+" * FROM LAW WHERE STATUS = 1 AND STATUS_DATE_EXPIRED = 0";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            ResultSet resultSet = pStmt.executeQuery();
            while(resultSet.next()) {

                Law data = new Law();
                data.setId(resultSet.getLong("LAW_ID"));
                data.setNumber(resultSet.getString("LAW_NUMBER"));
                data.setDateIssued(resultSet.getDate("LAW_DATE_ISSUED"));
                data.setCrawlerTypeName(resultSet.getString("CRAWLER_TYPE_NAME"));
                data.setCrawlerAgencyName(resultSet.getString("CRAWLER_AGENCY_NAME"));
                dataList.add(data);
            }

            resultSet.close();
        } catch (Exception ex) {
            throw ex;
        }

        return dataList;
    }

    public List<Law> queueList() throws SQLException {

        List<Law> dataList = new ArrayList<>();
        String sqlStory = "SELECT  * FROM LAW WHERE status = 1 and law_content != ''";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            ResultSet resultSet = pStmt.executeQuery();
            while(resultSet.next()) {

                Law data = new Law();
                data.setId(resultSet.getLong("LAW_ID"));
                data.setContent(resultSet.getString("LAW_CONTENT"));

                dataList.add(data);
            }

            resultSet.close();
        } catch (Exception ex) {
            throw ex;
        }

        return dataList;
    }

    public void insertCategory(Law law) throws SQLException {

        if (checkExists(law.getCrawlerSource())) {
            return;
        }

        System.out.println("INSERT[" + law.getCrawlerSource() + "]");

        String sqlStory = "INSERT INTO LAW(LAW_NAME,LAW_DATE_ISSUED,LAW_UPDATED_DATE,META_URL,META_TITLE,META_DESCRIPTION,META_KEYWORD,CRAWLER_CATEGORY,CRAWLER_SOURCE,STATUS," +
                "CRAWLER_SOURCE_ID) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
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
            pStmt.setString(11, crawlerSouceId(law.getCrawlerSource()));

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

    public void update(Law law) throws SQLException {

        String sqlStory = "UPDATE LAW SET LAW_NUMBER = ?, " +
                "LAW_NUMBER_PUBLICATION = ?, LAW_DATE_PUBLICATION = ?, LAW_AGENCY_ID = ?, " +
                "LAW_TYPE_ID = ?, LAW_SIGNED = ?, LAW_STATUS = ?, " +
                "LAW_CONTENT = ?, STATUS = ?, META_URL = ?, " +
                "CRAWLER_AGENCY_NAME = ?, CRAWLER_TYPE_NAME = ?, CRAWLER_LAW_REFER = ? WHERE LAW_ID = ?";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setString(1, law.getNumber());
            pStmt.setString(2, law.getNumberPublic());
            pStmt.setDate(3, new Date(law.getDatePublic().getTime()));
            pStmt.setLong(4, law.getAgencyId());
            pStmt.setLong(5, law.getTypeId());
            pStmt.setString(6, law.getSigned());
            pStmt.setLong(7, law.getLawStatus());
            pStmt.setString(8, law.getGoogleDriveId());
            pStmt.setLong(9, 1);
            pStmt.setString(10, law.getMetaUrl());
            pStmt.setString(11, law.getCrawlerAgencyName());
            pStmt.setString(12, law.getCrawlerTypeName());
            pStmt.setString(13, law.getCrawlerLawRefer());
            pStmt.setLong(14, law.getId());

            pStmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void updateStatus(long lawId, long status) throws SQLException {

        String sqlStory = "UPDATE LAW SET STATUS = ? WHERE LAW_ID = ?";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setLong(1, status);
            pStmt.setLong(2, lawId);

            pStmt.executeUpdate();
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

    public boolean checkExists(String crawlerSource) throws SQLException {
        String sqlStory = "SELECT * FROM LAW WHERE CRAWLER_SOURCE_ID = ?";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setString(1, crawlerSouceId(crawlerSource));

            ResultSet rs = pStmt.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (Exception ex) {
            throw ex;
        }

        return false;
    }

    public Map<String, String> selectTypeAll() throws SQLException {
        String sqlStory = "SELECT * FROM LAW_TYPE";
        Map<String, String> mapData = new HashMap<>();
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            ResultSet resultSet = pStmt.executeQuery();

            while(resultSet.next()) {
                mapData.put(resultSet.getString("LAW_TYPE_NAME"), resultSet.getString("LAW_TYPE_ID"));
            }
        } catch (Exception ex) {
            throw ex;
        }

        return mapData;
    }

    public Map<String, String> selectCategoryAll() throws SQLException {
        String sqlStory = "SELECT * FROM CATEGORY";
        Map<String, String> mapData = new HashMap<>();
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            ResultSet resultSet = pStmt.executeQuery();

            while(resultSet.next()) {
                mapData.put(resultSet.getString("CATEGORY_NAME"), resultSet.getString("CATEGORY_ID"));
            }
        } catch (Exception ex) {
            throw ex;
        }

        return mapData;
    }

    public Map<String, String> selectAgencyAll() throws SQLException {
        String sqlStory = "SELECT * FROM LAW_AGENCY";
        Map<String, String> mapData = new HashMap<>();
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            ResultSet resultSet = pStmt.executeQuery();

            while(resultSet.next()) {
                mapData.put(resultSet.getString("LAW_AGENCY_NAME"), resultSet.getString("LAW_AGENCY_ID"));
            }
        } catch (Exception ex) {
            throw ex;
        }

        return mapData;
    }

    public void updateStatusExpired(long lawId, long status) throws SQLException {

        String sqlStory = "UPDATE LAW SET STATUS_DATE_EXPIRED = ? WHERE LAW_ID = ?";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setLong(1, status);
            pStmt.setLong(2, lawId);

            pStmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void updateContentExpired(Law content) throws SQLException {

        String sqlStory = "UPDATE LAW SET LAW_DATE_EFFECTIVE = ?, LAW_DATE_EXPIRED = ?, LAW_STATUS = ?, STATUS_DATE_EXPIRED = 1  WHERE LAW_ID = ?";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setDate(1, content.getDateEffective() != null ? new Date(content.getDateEffective().getTime()) : null);
            pStmt.setDate(2, content.getDateExpired() != null ? new Date(content.getDateExpired().getTime()): null);
            pStmt.setLong(3, content.getLawStatus());
            pStmt.setLong(4, content.getId());

            pStmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }

    private String crawlerSouceId(String data) {
        String crawlerSourceId = data.substring(data.lastIndexOf("-") + 1, data.lastIndexOf("."));

        return crawlerSourceId;
    }
}
