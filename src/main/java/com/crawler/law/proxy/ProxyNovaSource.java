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

public class ProxyNovaSource {
    private static final Logger logger = LoggerFactory.getLogger(ProxyNovaSource.class);

    public static List<InetSocketAddress> proxy() {
        final List<InetSocketAddress> proxies = new ArrayList<>();

        try {
            Document html = Jsoup.connect("https://www.proxynova.com/proxy-server-list/elite-proxies/")
                    .userAgent(RandomStringUtils.random(32, true, true))
                    .maxBodySize(0)
                    .get();

            Elements elements = html.select("#tbl_proxy_list tbody tr[data-proxy-id]");

            elements.forEach(els -> {
                String host = els.select("td:eq(0) script").toString();
                host = host.substring(host.indexOf("'") + 1, host.lastIndexOf("'"));
                String port = els.select("td:eq(1)").text().trim();

                proxies.add(new InetSocketAddress(host, Integer.parseInt(port)));

            });

        } catch (Exception ex) {
            logger.error("ProxyNovaSource", ex);
        }

        return proxies;
    }

    public static void main(String[] args) {
        new ProxyNovaSource().proxy().forEach(v -> System.out.println(v.toString()));
    }
}
