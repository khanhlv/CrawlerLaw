package com.crawler.law.proxy;

import com.crawler.law.core.Consts;
import com.crawler.law.core.ShareQueue;
import com.crawler.law.core.UserAgent;
import com.crawler.law.util.ResourceUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

public class ProxyProvider {
    private static final Logger logger = LoggerFactory.getLogger(ProxyProvider.class);

    static  ArrayList<String> duplicateStrings = new ArrayList();

    public static void setup() {
        duplicateStrings.clear();

        ShareQueue.socketAddressList.clear();

        SSLProxiesOrgSource.proxy().stream().forEach(data -> addProxyItem(data));

        HideMySource.proxy().stream().forEach(data -> addProxyItem(data));

        FPLNetSource.proxy().stream().forEach(data -> addProxyItem(data));

        USProxySource.proxy().stream().forEach(data -> addProxyItem(data));

        ProxyDBSource.proxy().stream().forEach(data -> addProxyItem(data));

        ProxyNovaSource.proxy().stream().forEach(data -> addProxyItem(data));

        ProxyScanSource.proxy().stream().forEach(data -> addProxyItem(data));

        ProxyScrapeSource.proxy().stream().forEach(data -> addProxyItem(data));

        logger.debug("PROXY_SIZE [{}]" , ShareQueue.socketAddressList.size());
    }

    public static void addProxyItem(InetSocketAddress data) {
        if (!duplicateStrings.contains(data.toString())) {
            duplicateStrings.add(data.toString());

            ShareQueue.socketAddressList.add(data);
        }
    }

    public static List<InetSocketAddress> proxyList() {
        List<InetSocketAddress> socketAddresses = new LinkedList<>();
        socketAddresses.addAll(ShareQueue.socketAddressList);

        return socketAddresses;
    }

    public static void startThread() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                setup();
            }
        };

        int time = NumberUtils.toInt(ResourceUtil.getValue("data.crawler.proxy.time"));

        Timer timer = new Timer();
        timer.schedule(timerTask, 0, time * 60 * 1000);
    }

    public static boolean isProxyOnline(InetSocketAddress socketAddress) {
        try {
            long start = System.currentTimeMillis();
            Jsoup.connect("https://www.google.com/robots.txt")
                    .userAgent(UserAgent.getUserAgent())
                    .proxy(new Proxy(Proxy.Type.HTTP, socketAddress))
                    .timeout(Consts.TIMEOUT)
                    .get();

            long end = System.currentTimeMillis() - start;
            logger.debug("ADD_PROXY [{}] TIME [{}]", socketAddress.toString(), end);

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static void main(String[] args) {
        ProxyProvider.setup();
    }
}
