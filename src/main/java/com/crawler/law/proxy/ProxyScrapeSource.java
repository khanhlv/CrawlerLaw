package com.crawler.law.proxy;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProxyScrapeSource {

    private static final Logger logger = LoggerFactory.getLogger(ProxyScrapeSource.class);

    public static List<InetSocketAddress> proxy() {

        final List<InetSocketAddress> proxies = new ArrayList<>();

        try {
            Connection.Response response = Jsoup.connect("https://api.proxyscrape.com/v2/?request=getproxies&protocol=http&timeout=10000&country=all&ssl=yes&anonymity=all&simplified=true")
                    .userAgent(RandomStringUtils.random(32, true, true))
                    .maxBodySize(0)
                    .method(Connection.Method.GET)
                    .execute();

            List<String> proxyList = Arrays.asList(StringUtils.split(response.body(), "\n"));

            proxyList.forEach(els -> {
                if (StringUtils.isNotBlank(els.trim())) {
                    String[] proxy = StringUtils.split(els.trim(), ":");
                    proxies.add(new InetSocketAddress(proxy[0], Integer.parseInt(proxy[1])));
                }
            });
        } catch (Exception ex) {
            logger.error("ProxyScrapeSource", ex);
        }

        return proxies;
    }

    public static void main(String[] args) throws Exception {
        new ProxyScrapeSource().proxy().forEach(v -> System.out.println(v.toString()));
    }
}
