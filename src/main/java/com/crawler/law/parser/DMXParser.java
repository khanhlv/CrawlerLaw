package com.crawler.law.parser;

import com.crawler.law.core.ConnectionPool;
import com.crawler.law.core.Consts;
import com.crawler.law.core.UserAgent;
import com.crawler.law.model.Law;
import com.crawler.law.util.GZipUtil;
import com.crawler.law.util.StringUtil;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DMXParser {
    private static final Logger logger = LoggerFactory.getLogger(DMXParser.class);

    public void readDetail(String url, String id) throws Exception {
        //String url = "https://www.dienmayxanh.com/vao-bep/cach-lam-banh-pho-mai-hinh-trai-cam-cuc-ngon-cuc-hot-ma-de-14279";
        System.out.println(url);

        Connection connection = Jsoup.connect(url)
                .userAgent(UserAgent.getUserAgent())
                .timeout(Consts.TIMEOUT)
                .maxBodySize(0);

        Document body = connection.get();
        Elements elements = body.select(".detail-content");

        elements.select(".rate-view").remove();
        elements.select(".list-recipe").remove();
        elements.select(".infobox").remove();
        elements.select(".tipsnote").remove();
        elements.select("#dmxoverlay").remove();
        elements.select(".customerSurvey").remove();
        elements.select(".order-review").remove();
        elements.select(".food-similar").remove();
        elements.select("#boxRating").remove();
        elements.select(".box-commentCook").remove();
        elements.select(".staple small").remove();

        elements.select(".tipsrecipe p").forEach(v -> {
            if (v.text().contains("Xem chi tiết")) {
                v.remove();
            }
        });

        elements.select("a").forEach(v -> {
            v.attr("href", "#");
        });

        elements.select("*").removeAttr("onclick").removeAttr("data-picid");

        String data = elements.toString()
                .replaceAll("data-src", "src")
                .replaceAll("Điện máy XANH", "HAY ĂN")
                .replaceAll("bachhoaxanh.com", "hayan.vn");

        String metaKeywork = body.select("meta[name=keywords]").attr("content");
        String metaDescritpion = body.select("meta[name=description]").attr("content");
        String category = body.select(".detail-danhmuc ul li.active").text().trim();

        GZipUtil.compressGZIP(data, new File("D:\\hayan.vn\\" + id.concat(".html.gz")));

        updatePostContent(id, id.concat(".html.gz"), category, metaDescritpion, metaKeywork);

        updatePostStatus(id, 1);

        System.out.println("Category: " + category);
        System.out.println("Keywork: " + metaKeywork);
        System.out.println("Data: " + data.length());
    }
    public static List<String> listId = new ArrayList<>();

    public void readPage(int page) throws Exception {
        String url = "https://www.dienmayxanh.com/vao-bep/aj/Home/ViewMoreLastestBox";
        System.out.println(url + " - " + page);
        Connection connection = Jsoup.connect(url)
                .userAgent(UserAgent.getUserAgent())
                .header("X-Requested-With", "XMLHttpRequest")
                .timeout(Consts.TIMEOUT)
                .maxBodySize(0);

        Map<String, String> dataMap = new LinkedHashMap<>();
        dataMap.put("pageIndex", String.valueOf(page));
        connection.data(dataMap);


        Document body = connection.post();

        Elements elements = body.select("li a[href]");

        elements.stream().forEach(v -> {
            String title = v.select("strong").text();
            String nameFood = v.select("img").attr("alt");
            String image = v.select("img").attr("data-src");
            String link = "https://www.dienmayxanh.com" + v.attr("href");

            System.out.println("-----------------");
            String id = link.substring(link.lastIndexOf("-") + 1);
            System.out.println("Title: " + title);
            System.out.println("Name: " + nameFood);
            System.out.println("Image: " + image);
            System.out.println("Link: " + link);
            System.out.println("Id: " + id);

            if (!listId.contains(id)) {
                try {
                    insertPostPage(title, nameFood, image, link, id);
                } catch (SQLException e) {
                    logger.error("SQLException + " + page, e);
                }

                listId.add(id);
            }

        });

        System.out.println(elements.size() + "-" + listId.size());
    }

    public List<String> queueList() throws SQLException {
        List<String> dataList = new ArrayList<>();
        String sqlStory = "SELECT TOP 1 * FROM POST WHERE STATUS = 0";
        try (java.sql.Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            ResultSet resultSet = pStmt.executeQuery();
            while(resultSet.next()) {
                dataList.add(resultSet.getString("POST_ID") + "|" + resultSet.getString("SOURCE_URL"));
            }
            resultSet.close();
        } catch (Exception ex) {
            throw ex;
        }

        return dataList;
    }

    public void insertPostPage(String title, String name, String image, String link, String id) throws SQLException {
        String sqlStory = "INSERT INTO POST (POST_TITLE, POST_IMAGE, POST_DATE, " +
                "META_URL, META_TITLE, META_KEYWORDS, " +
                "SOURCE_URL, SOURCE_ID, STATUS) " +
                " VALUES (?,?,getdate(),?,?,?,?,?,0)";
        try (java.sql.Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setString(1, title);
            pStmt.setString(2, image);
            pStmt.setString(3, StringUtil.stripAccents(title, "-"));
            pStmt.setString(4, title);
            pStmt.setString(5, name);
            pStmt.setString(6, link);
            pStmt.setString(7, id);

            pStmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void updatePostStatus(String id, int status) throws SQLException {
        String sqlStory = "UPDATE POST SET STATUS = ? WHERE POST_ID = ?";
        try (java.sql.Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setInt(1, status);
            pStmt.setString(2, id);

            pStmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void updatePostContent(String id, String content, String category, String description, String keyword) throws SQLException {
        String sqlStory = "UPDATE POST SET POST_CONTENT = ?, CATEGORY = ?, CATEGORY_URL = ?, META_DESCRIPTION = ?, META_KEYWORDS = META_KEYWORDS + ? WHERE POST_ID = ?";
        try (java.sql.Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setString(1, content);
            pStmt.setString(2, category);
            pStmt.setString(3, StringUtil.stripAccents(category));
            pStmt.setString(4, description);
            pStmt.setString(5, "," + keyword);
            pStmt.setString(6, id);

            pStmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static void main(String[] args) throws Exception {

        DMXParser dmxParser = new DMXParser();
//        int page = 875;
//        while (page > 0) {
//            dmxParser.readPage(page);
//            page--;
//            Thread.sleep(500);
//        }

        dmxParser.queueList().forEach(v -> {
            String id = v.split("\\|")[0];
            String url = v.split("\\|")[1];

            System.out.println(id + "-" + url);

            try {
                dmxParser.readDetail(url, id);
            } catch (Exception ex) {
                logger.error("readDetail" + ex);
                try {
                    dmxParser.updatePostStatus(id, -1);
                } catch (Exception ex1) {
                    logger.error("updatePostStatus" + ex);
                }
            }
        });

    }

}
