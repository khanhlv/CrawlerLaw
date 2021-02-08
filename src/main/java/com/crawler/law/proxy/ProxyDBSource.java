package com.crawler.law.proxy;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ProxyDBSource {
    private static final Logger logger = LoggerFactory.getLogger(ProxyDBSource.class);

    public static List<InetSocketAddress> proxy() {
        final List<InetSocketAddress> proxies = new ArrayList<>();

        try {
            Document html = Jsoup.connect("http://proxydb.net/?protocol=https&anonlvl=4&country=")
                    .userAgent(RandomStringUtils.random(32, true, true))
                    .get();

            Elements elements = html.select("table tbody tr");

            elements.forEach(els -> {
                Element tdElement = els.select("td:eq(0) a").first();
                String[] address = StringUtils.split(tdElement.text().trim(), ":");

                proxies.add(new InetSocketAddress(address[0], Integer.parseInt(address[1])));

            });

        } catch (Exception ex) {
            logger.error("ProxyDBSource", ex);
        }

        return proxies;
    }

    public static void main(String[] args) {
        new ProxyDBSource().proxy().forEach(v -> System.out.println(v.toString()));
    }
}
