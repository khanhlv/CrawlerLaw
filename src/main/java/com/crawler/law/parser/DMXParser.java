package com.crawler.law.parser;

import com.crawler.law.core.Consts;
import com.crawler.law.core.UserAgent;
import com.crawler.law.model.Law;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class DMXParser {
    private static final Logger logger = LoggerFactory.getLogger(DMXParser.class);

    public void readDetail() throws Exception {
        String url = "https://www.dienmayxanh.com/vao-bep/cach-lam-thit-heo-hap-gung-thom-nong-am-nong-thom-ngon-kho-14043";
        System.out.println(url);

        Connection connection = Jsoup.connect(url)
                .userAgent(UserAgent.getUserAgent())
                .timeout(Consts.TIMEOUT)
                .maxBodySize(0);

        Document body = connection.get();

        System.out.println(body.select("#tongquan").toString().replaceAll("data-src", "src"));
        System.out.println(body.select("#chuanbi").toString().replaceAll("data-src", "src"));
        System.out.println(body.select("#step").toString().replaceAll("data-src", "src"));
    }

    public void readPage() throws Exception {
        String url = "https://www.dienmayxanh.com/vao-bep/aj/Home/ViewMoreLastestBox";
        System.out.println(url);
        Connection connection = Jsoup.connect(url)
                .userAgent(UserAgent.getUserAgent())
                .header("X-Requested-With", "XMLHttpRequest")
                .timeout(Consts.TIMEOUT)
                .maxBodySize(0);

        Map<String, String> dataMap = new LinkedHashMap<>();
        dataMap.put("pageIndex", "1");
        connection.data(dataMap);


        Document body = connection.post();

        Elements elements = body.select("li a[href]");

        elements.stream().forEach(v -> {
            String title = v.select("strong").text();
            String nameFood = v.select("img").attr("alt");
            String image = v.select("img").attr("data-src");
            String link = "https://www.dienmayxanh.com/" + v.attr("href");

            System.out.println("-----------------");
            System.out.println("Title: " + title);
            System.out.println("Name: " + nameFood);
            System.out.println("Image: " + image);
            System.out.println("Link: " + link);
        });

        System.out.println(elements.size());
    }

    public static void main(String[] args) throws Exception {

        new DMXParser().readDetail();
    }

}
