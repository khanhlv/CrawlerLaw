package com.crawler.law.proxy;

import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class HideMySource {
    private static final Logger logger = LoggerFactory.getLogger(HideMySource.class);

    public static List<InetSocketAddress> proxy() {
        final List<InetSocketAddress> proxies = new ArrayList<>();

        try {
            Document doc = Jsoup.connect("https://hidemy.name/en/proxy-list/?type=s&anon=4#list")
                    .userAgent(RandomStringUtils.random(32, true, true))
                    .get();

            Element table = doc.select("div.table_block").first();

            if (table != null) {
                for (Element e : table.select("tbody tr")) {
                    final String hostname = e.select("td:eq(0)").text();
                    final String port = e.select("td:eq(1)").text();
                    proxies.add(new InetSocketAddress(hostname, Integer.parseInt(port)));
                }
            }

        } catch (Exception ex) {
            logger.error("HideMySource", ex);
        }

        return proxies;
    }

    public static void main(String[] args) throws Exception {
        new HideMySource().proxy().forEach(v -> System.out.println(v.toString()));
    }
}
