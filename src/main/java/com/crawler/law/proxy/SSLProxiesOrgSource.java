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

public class SSLProxiesOrgSource {

    private static final Logger logger = LoggerFactory.getLogger(SSLProxiesOrgSource.class);

    public static List<InetSocketAddress> proxy() {

        final List<InetSocketAddress> proxies = new ArrayList<>();

        try {
            Document html = Jsoup.connect("http://www.sslproxies.org")
                    .userAgent(RandomStringUtils.random(32, true, true))
                    .get();

            Elements addrs = html.select("table td:eq(0)");
            Elements ports = html.select("table td:eq(1)");
            Elements protocols = html.select("table td:eq(6)");

            for (int i = 0; i < addrs.size(); i++) {
                if ("yes".equals(protocols.get(i).text())) {
                    proxies.add(new InetSocketAddress(addrs.get(i).text(), Integer.parseInt(ports.get(i).text())));
                }
            }
        } catch (Exception ex) {
            logger.error("SSLProxiesOrgSource", ex);
        }

        return proxies;
    }

    public static void main(String[] args) throws Exception {
        new SSLProxiesOrgSource().proxy().forEach(v -> System.out.println(v.toString()));
    }
}
