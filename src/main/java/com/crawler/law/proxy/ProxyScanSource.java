package com.crawler.law.proxy;

import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ProxyScanSource {
    private static final Logger logger = LoggerFactory.getLogger(ProxyScanSource.class);

    public static List<InetSocketAddress> proxy() {
        final List<InetSocketAddress> proxies = new ArrayList<>();

        try {
            Connection.Response html = Jsoup.connect("https://www.proxyscan.io/Home/FilterResult")
                    .userAgent(RandomStringUtils.random(32, true, true))
                    .maxBodySize(0)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .data("status", "1")
                    .data("ping", "")
                    .data("selectedType", "HTTPS")
                    .data("SelectedAnonymity", "Transparent")
                    .data("SelectedAnonymity", "Anonymous")
                    .data("SelectedAnonymity", "Elite")
                    .data("sortPing", "false")
                    .data("sortTime", "true")
                    .data("sortUptime", "false")
                    .method(Connection.Method.POST)
                    .execute();

            Document document = Jsoup.parse("<table>" + html.body() + "</table>");
            Elements elements = document.select("tr");

            elements.forEach(els -> {
                String host = els.select("th").text().trim();
                String port = els.select("td:eq(1)").text().trim();
                proxies.add(new InetSocketAddress(host, Integer.parseInt(port)));

            });

        } catch (Exception ex) {
            logger.error("ProxyScanSource", ex);
        }

        return proxies;
    }

    public static void main(String[] args) {
        new ProxyScanSource().proxy().forEach(v -> System.out.println(v.toString()));
    }
}
