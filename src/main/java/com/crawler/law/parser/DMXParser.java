package com.crawler.law.parser;

import com.crawler.law.core.ConnectionPool;
import com.crawler.law.core.Consts;
import com.crawler.law.core.UserAgent;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;

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
        //elements.select(".tipsnote").remove();
        elements.select(".generate-promotion-products").remove();
        elements.select(".txtAuthor").remove();
        elements.select(".sourceNoneClick").remove();

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
                .replaceAll("điện máy XANH", "HAY ĂN")
                .replaceAll("bachhoaxanh.com", "hayan.vn");

        String metaKeywork = body.select("meta[name=keywords]").attr("content");
        String metaDescritpion = body.select("meta[name=description]").attr("content");
        metaDescritpion = metaDescritpion.replaceAll("Điện máy XANH", "HAY ĂN")
                .replaceAll("điện máy XANH", "HAY ĂN")
                .replaceAll("bachhoaxanh.com", "hayan.vn");
        String category = body.select(".detail-danhmuc ul li.active").text().trim();

        GZipUtil.compressGZIP(data, new File("C:\\vhots\\hayan.vn\\data\\" + id.concat(".html.gz")));

        updatePostContent(id, id.concat(".html.gz"), category, metaDescritpion, metaKeywork);

        updatePostStatus(id, 1);

        System.out.println("Category: " + category);
        System.out.println("Keywork: " + metaKeywork);
        System.out.println("Data: " + data.length());
    }

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
            String id = link.substring(link.lastIndexOf("-") + 1);

            try {
                if (!checkExists(id)) {
                    System.out.println("-----------------");
                    System.out.println("Title: " + title);
                    System.out.println("Name: " + nameFood);
                    System.out.println("Image: " + image);
                    System.out.println("Link: " + link);
                    System.out.println("Id: " + id);
                    insertPostPage(title, nameFood, image, link, id);
                }
            } catch (SQLException e) {
                logger.error("SQLException + " + page, e);
            }
        });
    }

    public ConcurrentLinkedDeque<String> queueList() throws SQLException {
        ConcurrentLinkedDeque<String> dataList = new ConcurrentLinkedDeque<>();
        String sqlStory = "SELECT * FROM POST WHERE STATUS = 0";
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

    public boolean checkExists(String id) throws SQLException {
        String sqlStory = "SELECT * FROM POST WHERE SOURCE_ID = ?";
        try (java.sql.Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setString(1, id);

            ResultSet rs = pStmt.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (Exception ex) {
            throw ex;
        }

        return false;
    }

    public void insertPostPage(String title, String name, String image, String link, String id) throws SQLException {
        String sqlStory = "INSERT INTO POST (POST_TITLE, POST_IMAGE, POST_DATE, " +
                "META_URL, META_TITLE, META_KEYWORDS, META_IMAGE, " +
                "SOURCE_URL, SOURCE_ID, STATUS) " +
                " VALUES (?,?,getdate(),?,?,?,?,?,?,0)";
        try (java.sql.Connection con = ConnectionPool.getTransactional();
             PreparedStatement pStmt = con.prepareStatement(sqlStory)) {

            pStmt.setString(1, title);
            pStmt.setString(2, image);
            pStmt.setString(3, StringUtil.stripAccents(title, "-"));
            pStmt.setString(4, title);
            pStmt.setString(5, name);
            pStmt.setString(6, image);
            pStmt.setString(7, link);
            pStmt.setString(8, id);

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
            pStmt.setString(3, StringUtil.stripAccents(category, "-"));
            pStmt.setString(4, description);
            pStmt.setString(5, "," + keyword);
            pStmt.setString(6, id);

            pStmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void startDetail() {
        DMXParser dmxParser = new DMXParser();

        try {
            ConcurrentLinkedDeque<String> concurrentLinkedDeque = dmxParser.queueList();

            while (concurrentLinkedDeque.size() > 0) {
                for (int i = 1; i <= 5; i++) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (concurrentLinkedDeque.size() > 0) {
                                String data = concurrentLinkedDeque.poll();

                                String id = data.split("\\|")[0];
                                String url = data.split("\\|")[1];

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
                            }
                        }
                    }).start();
                    Thread.sleep(1000);
                }
            }
            System.out.println("SIZE " + concurrentLinkedDeque.size());
        } catch (Exception ex) {
            logger.error("startDetail" + ex);
        }
    }

    public void startCategory()  {
        DMXParser dmxParser = new DMXParser();
        int page = 100;
        while (page > 0) {
            try {
                dmxParser.readPage(page);

                Thread.sleep(500);
            } catch (Exception ex) {
                logger.error("startCategory", ex);
            }

            page--;
        }
    }

    public static void main(String[] args) {

        DMXParser dmxParser = new DMXParser();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    dmxParser.startCategory();
                    dmxParser.startDetail();
                } catch (Exception e) {
                    logger.error("TimerTask", e);
                }
            }
        };

        int time = 240; //Phut

        Timer timer = new Timer();
        timer.schedule(timerTask, 0, time * 60 * 1000);
    }
}
