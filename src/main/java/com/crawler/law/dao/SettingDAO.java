package com.crawler.law.dao;

import com.crawler.law.core.ConnectionPool;
import com.crawler.law.core.ShareApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingDAO {

    public String getValue(String key) throws SQLException {
        String result = null;

        String sqlStory = "SELECT * FROM `setting` where `setting_key` = ? and `status` = 1";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setString(1, key);

            ResultSet resultSet = pStmt.executeQuery();

            if (resultSet.next()) {
                result = resultSet.getString("setting_value");
            }

            resultSet.close();
        } catch (Exception ex) {
            throw ex;
        }

        return result;
    }

    public void updateStatus(String key, int status) throws SQLException {
        String sqlStory = "UPDATE `setting` SET `status` = ?, `update_agent` = ?, `updated_date` = now() WHERE `setting_key` = ?";
        try (Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setInt(1, status);
            pStmt.setString(2, ShareApplication.crawlerAgent);
            pStmt.setString(3, key);

            pStmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }
}
