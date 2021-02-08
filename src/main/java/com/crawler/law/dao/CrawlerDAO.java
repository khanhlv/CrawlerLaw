package com.crawler.law.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawler.law.core.ConnectionPool;
import com.crawler.law.core.ShareApplication;
import com.crawler.law.model.Crawler;
import com.crawler.law.model.Queue;

public class CrawlerDAO {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerDAO.class);

    public long countQueueCrawler() throws SQLException {
        String sqlStory = "SELECT count(1) FROM " + ShareApplication.crawler.getTableQueue() + " WHERE `status` = 0";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory);
             ResultSet resultSet = pStmt.executeQuery()) {

            if(resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (Exception ex) {
            throw ex;
        }

        return 0;
    }

    public void updateAllQueueStatus(int status) throws SQLException {
        String sqlStory = "UPDATE " + ShareApplication.crawler.getTableQueue() + " SET `status` = ?";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setInt(1, status);

            pStmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public List<Crawler> crawlerList() throws SQLException {

        List<Crawler> crawlerList = new ArrayList<>();
        String sqlStory = "SELECT * FROM " + ShareApplication.crawler.getTableCrawler();
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory);
             ResultSet resultSet = pStmt.executeQuery()) {

            while(resultSet.next()) {
                Crawler crawler = new Crawler();
                crawler.setId(resultSet.getString("id"));
                crawler.setName(resultSet.getString("name"));
                crawler.setUrl(resultSet.getString("url"));
                crawler.setPage(resultSet.getInt("page"));
                crawlerList.add(crawler);
            }
        } catch (Exception ex) {
            throw ex;
        }

        return crawlerList;
    }


    public List<Queue> queueList(int limit) throws SQLException {

        List<Queue> queueList = new ArrayList<>();
        String sqlStory = "SELECT * FROM " + ShareApplication.crawler.getTableQueue() + " WHERE status = 0 LIMIT ?";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setInt(1, limit);

            ResultSet resultSet = pStmt.executeQuery();
            while(resultSet.next()) {

                Queue queue = new Queue();
                queue.setId(resultSet.getInt("id"));
                queue.setLink(resultSet.getString("link"));
                queue.setStatus(resultSet.getInt("status"));
                queue.setName(resultSet.getString("name"));

                updateQueueStatus(queue.getId(), 1);

                queueList.add(queue);
            }
            resultSet.close();
        } catch (Exception ex) {
            throw ex;
        }

        return queueList;
    }

    public void updateQueueStatus(int id, int status) throws SQLException {
        String sqlStory = "UPDATE " + ShareApplication.crawler.getTableQueue() + " SET status = ? WHERE id = ?";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setInt(1, status);
            pStmt.setInt(2, id);

            pStmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void insertQueueFile() throws Exception {
        try {
            StringBuilder data = new StringBuilder();
            crawlerList().stream().forEach(v -> {
                for (int i = 2; i <= v.getPage(); i++) {
                    String url = v.getUrl().replaceAll("#\\{page\\}", i + "");
                    System.out.println(url);

                    data.append(String.format("INSERT INTO %s (link, name, status) VALUES ('%s','%s',0);\n", ShareApplication.crawler.getTableQueue(), url, v.getName()));
                }
            });

            FileUtils.writeStringToFile(new File("data/queue.sql"), data.toString());
        } catch (Exception e) {
            throw e;
        }
    }

    public static void main(String[] args) {
        try {
            CrawlerDAO crawlerDAO = new CrawlerDAO();

            crawlerDAO.insertQueueFile();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
