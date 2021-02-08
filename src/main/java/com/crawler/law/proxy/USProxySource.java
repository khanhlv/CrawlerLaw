package com.crawler.law.proxy;

import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class USProxySource {

    private static final Logger logger = LoggerFactory.getLogger(USProxySource.class);

    public static List<InetSocketAddress> proxy() {

        final List<InetSocketAddress> proxies = new ArrayList<>();

        try {
            Document html = Jsoup.connect("https://www.us-proxy.org")
                    .userAgent(RandomStringUtils.random(32, true, true))
                    .get();

            Elements elements = html.select("table#proxylisttable tbody tr");

            elements.forEach(els -> {
                String host = els.select("td:eq(0)").text().trim();
                String port = els.select("td:eq(1)").text().trim();
                String https = els.select("td:eq(6)").text().trim();

                if (https.equals("yes")) {
                    proxies.add(new InetSocketAddress(host, Integer.parseInt(port)));
                }
            });

        } catch (Exception ex) {
            logger.error("USProxySource", ex);
        }

        return proxies;
    }

    public static void main(String[] args) throws Exception {
        new USProxySource().proxy().forEach(v -> System.out.println(v.toString()));
    }
}
