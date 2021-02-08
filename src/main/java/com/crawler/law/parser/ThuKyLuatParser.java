package com.crawler.law.parser;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawler.law.core.Consts;
import com.crawler.law.core.UserAgent;
import com.crawler.law.enums.Crawler;
import com.crawler.law.model.Data;
import com.crawler.law.proxy.ProxyProvider;
import com.crawler.law.util.AmazonUtil;
import com.crawler.law.util.ResourceUtil;
import com.google.gson.Gson;

public class ThuKyLuatParser {
    private static final Logger logger = LoggerFactory.getLogger(ThuKyLuatParser.class);
    private static String page = "https://thukyluat.vn/tim-kiem/?page=2";
    private static String detail = "https://thukyluat.vn/vb/thong-tu-17-2020-tt-btnmt-lap-ban-ve-mat-cat-hien-trang-khu-vuc-duoc-phep-khai-thac-khoang-san-70601.html";

    public Data readDetail(String url, String code, int id, InetSocketAddress socketAddress) throws Exception {
        Data dataMap = new Data();

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

    public List<Data> readQuery(String url) throws Exception {
        List<Data> lisData = new ArrayList<>();

        Map<String, String> mapHeader = new LinkedHashMap<>();
//        mapHeader.put("origin", Crawler.AMAZON_COM.getSite());
//        mapHeader.put("referer", url);
//        mapHeader.put("sec-fetch-dest", "empty");
//        mapHeader.put("sec-fetch-mode", "cors");
//        mapHeader.put("x-amazon-s-fallback-url", "");
//        mapHeader.put("x-amazon-s-mismatch-behavior", "ALLOW");
//        mapHeader.put("x-requested-with", "XMLHttpRequest");

        Connection.Response resp = Jsoup.connect(url)
                .userAgent(UserAgent.getUserAgent())
                .timeout(Consts.TIMEOUT_CATEGORY)
                .headers(mapHeader)
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
            Elements href = els.select("ul").get(0).select("li.vb-Tit > a");
            href.select("span").remove();

            Elements category = els.select("ul").get(1).select("a");


            System.out.println(href.text());
            System.out.println(href.attr("href"));

            category.stream().forEach(v -> {
                System.out.println(v.select("a").text());
            });
            System.out.println("---------------------------------------");
        });
        System.out.println(elements.size());

        return lisData;
    }

    public static void main(String[] args) {
        try {
            ThuKyLuatParser thuKyLuatParser = new ThuKyLuatParser();
//            thuKyLuatParser.readQuery("https://thukyluat.vn/tim-kiem/?page=19054");
            thuKyLuatParser.readDetail(detail, "123", 1, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
