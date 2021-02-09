package com.crawler.law.parser;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawler.law.core.Consts;
import com.crawler.law.core.ShareApplication;
import com.crawler.law.core.UserAgent;
import com.crawler.law.dao.LawDAO;
import com.crawler.law.model.Law;
import com.crawler.law.util.ResourceUtil;

public class ThuKyLuatParser {
    private static final Logger logger = LoggerFactory.getLogger(ThuKyLuatParser.class);
    private static String page = "https://thukyluat.vn/tim-kiem/?page=2";
    private static String detail = "https://thukyluat.vn/vb/thong-tu-17-2020-tt-btnmt-lap-ban-ve-mat-cat-hien-trang-khu-vuc-duoc-phep-khai-thac-khoang-san-70601.html";
    private static String fileDownload = "https://thukyluat.vn/downloadpdf/24b1a/0?googledoc=true";

    public Law readDetail(String url, String code, int id, InetSocketAddress socketAddress) throws Exception {
        Law dataMap = new Law();

        Connection connection = Jsoup.connect(url)
                .userAgent(UserAgent.getUserAgent())
                .timeout(Consts.TIMEOUT)
                .maxBodySize(0);

        if (socketAddress != null) {
            connection.proxy(new Proxy(Proxy.Type.HTTP, socketAddress));
        }

        Document doc = connection.get();

        if (ResourceUtil.getValue("debug").equals("true")) {
            FileUtils.writeStringToFile(new File("data/html/" + code + ".html"), doc.html());
        }

        String htmlContent = doc.select("#NDDayDu").select(".MainContentAll").html();

        Elements tip = doc.select("#NDDayDu").select("#bmContent");

//        tip.stream().forEach(v -> {
//            System.out.println(v.parent());
//            System.out.println("-----------------");
//        });
//        System.out.println(tip.size());

        Elements elsNDTomTat = doc.select("#NDTomTat").select("table tbody tr");

        // Số hiệu
        System.out.println(elsNDTomTat.get(0).select("td").get(1).text());

        // Loại văn bản
        System.out.println(elsNDTomTat.get(0).select("td").get(4).text());

        // Nơi ban hành
        System.out.println(elsNDTomTat.get(1).select("td").get(1).text());

        // Người ký
        System.out.println(elsNDTomTat.get(1).select("td").get(4).text());

        // Ngày ban hành
        System.out.println(elsNDTomTat.get(2).select("td").get(1).text());

        // Ngày hiệu lực
        System.out.println(elsNDTomTat.get(2).select("td").get(4).text());

        // Ngày công báo
        System.out.println(elsNDTomTat.get(3).select("td").get(1).text());

        // Số công báo
        System.out.println(elsNDTomTat.get(3).select("td").get(4).text());

        // Lĩnh vực
        System.out.println(elsNDTomTat.get(4).select("td").get(1).text());

        // Tình trạng
        System.out.println(elsNDTomTat.get(4).select("td").get(4).text());

        return dataMap;
    }

    public List<Law> readQuery(String url) throws Exception {
        List<Law> lisData = new ArrayList<>();

        Connection.Response resp = Jsoup.connect(url)
                .userAgent(UserAgent.getUserAgent())
                .timeout(Consts.TIMEOUT_CATEGORY)
                .ignoreContentType(true)
                .method(Connection.Method.GET)
                .maxBodySize(0)
                .execute();

        String body = resp.body();

        if (ResourceUtil.getValue("debug").equals("true")) {
            FileUtils.writeStringToFile(new File("data/category/" + url.substring(url.lastIndexOf("?") + 1) + ".html"), body);
        }

        Document document = Jsoup.parse(body);

        Elements elements = document.select(".vaban-nd .item-vb");

        elements.stream().forEach(els -> {
            Elements elementsUl = els.select("ul");

            Elements href = elementsUl.get(0).select(".vb-Tit > a");
            href.select("span").remove();

            Elements category = elementsUl.get(2).select("a");

            String title = href.text().trim();
            String crawlerSource = href.attr("href").trim();

            ArrayList<String> categoryList = new ArrayList<>();
            ArrayList<String> categoryNameList = new ArrayList<>();
            category.stream().forEach(v -> {
                String categoryN = v.select("a").text().trim();

                categoryNameList.add(categoryN);
                categoryList.add(ShareApplication.CATEGORY_MAP.get(categoryN));
            });

            Elements elsDate = els.select("ul").get(0).select(".date-vb li");
            String dateIssued = elsDate.get(0).select("span").text().trim();
            String dateUpdated = elsDate.select(".cap-nhat").select("span").text().trim();

            lisData.add(toData(title, crawlerSource, StringUtils.join(categoryNameList, "|"), categoryList, dateIssued, dateUpdated));
        });

        return lisData;
    }
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Law toData(String title, String url, String categoryName, ArrayList<String> categoryId,
                       String dateIssued, String dateUpdate) {
        Law law = new Law();
        law.setName(title);
        law.setCrawlerSource(url);
        law.setCrawlerCategoryName(categoryName);

        law.setCategory(categoryId);

        try {
            law.setDateIssued(dateFormat.parse(dateIssued));
        } catch (ParseException e) {

        }

        try {
            law.setUpdatedDate(dateFormat.parse(dateUpdate));
        } catch (ParseException e) {

        }

        System.out.println(law.getName());
        System.out.println(law.getCrawlerSource());
        System.out.println(law.getCrawlerCategoryName() + "#" + StringUtils.join(law.getCategory(), ","));
        System.out.println(dateIssued);
        System.out.println(dateUpdate);
        System.out.println("-------------------------------------");
        return law;
    }

    public static void main(String[] args) {
        try {
            ThuKyLuatParser thuKyLuatParser = new ThuKyLuatParser();
            List<Law> laws = thuKyLuatParser.readQuery("https://thukyluat.vn/tim-kiem/?page=19054");

            LawDAO lawDAO = new LawDAO();
            laws.forEach(v -> {
                try {
                    lawDAO.insertCategory(v);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
//            thuKyLuatParser.readDetail(detail, "123", 1, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
