package com.crawler.law.parser;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.crawler.law.util.GZipUtil;
import com.crawler.law.util.StringUtil;
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
import com.crawler.law.core.ShareApplication;
import com.crawler.law.core.UserAgent;
import com.crawler.law.model.Law;
import com.crawler.law.util.ResourceUtil;

public class ThuKyLuatParser {
    private static final Logger logger = LoggerFactory.getLogger(ThuKyLuatParser.class);
    private static String page = "https://thukyluat.vn/tim-kiem/?page=2";
    private static String detail = "https://thukyluat.vn/vb/thong-tu-17-2020-tt-btnmt-lap-ban-ve-mat-cat-hien-trang-khu-vuc-duoc-phep-khai-thac-khoang-san-70601.html";

    // https://thukyluat.vn/vb/luat-sua-doi-bo-luat-lao-dong-51766.html
    private static String fileDownload = "https://thukyluat.vn/downloadpdf/24b1a/0?googledoc=true";
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public Law readDetail(String url, Long id) throws Exception {
         Connection connection = Jsoup.connect(url)
                .userAgent(UserAgent.getUserAgent())
                .timeout(Consts.TIMEOUT)
                .maxBodySize(0);

        Document doc = connection.get();

        if (ResourceUtil.getValue("debug").equals("true")) {
            FileUtils.writeStringToFile(new File("data/html/" + id + ".html"), doc.html());
        }

        removeComments(doc);

        Elements elsContent = doc.select("#NDDayDu").select(".MainContentAll");
        elsContent.select("a").removeAttr("onmouseover");
        elsContent.select("a").removeAttr("onmouseout");
        elsContent.select("a[href]").attr("href", "#");

        String htmlContent = elsContent.html();

        Elements tip = elsContent.select("a[onclick]");

        ArrayList<String> tipList = new ArrayList<>();
        tip.stream().forEach(v -> {
            String tipType = v.attr("onclick");
            tipType = tipType.substring(tipType.indexOf("'") + 1, tipType.lastIndexOf("'"));

            String tipHtml = doc.select("#NDDayDu").select(tipType).toString();

            tipList.add(tipHtml);
        });

        if (StringUtils.isNotBlank(htmlContent)) {
            htmlContent += "<div style=\"display: none\">" + StringUtils.join(tipList, "") + "</div>";
        }

        Elements lawRefer = elsContent.select("a[href]");

        ArrayList<String> referLawList = new ArrayList<>();

        lawRefer.stream().forEach(els -> {
            String href = els.attr("href");
            if (href.contains(".html")) {
                referLawList.add(els.text().trim());
            }
        });

        FileUtils.writeStringToFile(new File(ResourceUtil.getValue("data.path") + "/data/" + id + ".html"), htmlContent, "UTF-8");

        GZipUtil.compressGZIP(htmlContent, new File(ResourceUtil.getValue("data.path") + "/data/" + id + ".html.gz"));

        Elements elsNDTomTat = doc.select("#NDTomTat").select("table tbody tr");

        if (elsNDTomTat.size() == 0) {
            elsNDTomTat = doc.select("#NDDayDu").select(".MainContent table tbody tr");
        }

        // Số hiệu
        String number = elsNDTomTat.get(0).select("td").get(1).text().trim();

        // Loại văn bản
        String type = elsNDTomTat.get(0).select("td").get(4).text().trim();

        // Nơi ban hành
        String agency = elsNDTomTat.get(1).select("td").get(1).text().trim();

        // Người ký
        String signes = elsNDTomTat.get(1).select("td").get(4).text().trim();

        // Ngày ban hành
//        System.out.println(elsNDTomTat.get(2).select("td").get(1).text());

        // Ngày hiệu lực
//        System.out.println(elsNDTomTat.get(2).select("td").get(4).text());

        // Ngày công báo
        String datePublic = elsNDTomTat.get(3).select("td").get(1).text().trim();

        // Số công báo
        String numberPublic = elsNDTomTat.get(3).select("td").get(4).text().trim();

        // Lĩnh vực
//        System.out.println(elsNDTomTat.get(4).select("td").get(1).text());

        // Tình trạng
//        System.out.println(elsNDTomTat.get(4).select("td").get(4).text());

        return toData(id, number, numberPublic, datePublic, agency, type, signes, htmlContent, StringUtils.join(referLawList, "|"));
    }

    public void removeComments(Element e) {
        e.childNodes().stream()
                .filter(n -> n.nodeName().equals("#comment")).collect(Collectors.toList())
                .forEach(n -> n.remove());
        e.children().forEach(elem -> removeComments(elem));
    }

    public List<Law> readQuery(String url) throws Exception {
        List<Law> lisData = new ArrayList<>();

        Connection.Response resp = Jsoup.connect(url)
                .userAgent(UserAgent.getUserAgent())
                .timeout(Consts.TIMEOUT_CATEGORY)
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
            Elements elementsUl = els.select("ul");

            Elements href = elementsUl.get(0).select(".vb-Tit > a");
            href.select("span").remove();

            Elements category = elementsUl.get(2).select("a");

            String title = href.text().trim();
            String crawlerSource = href.attr("href").trim();

            ArrayList<String> categoryList = new ArrayList<>();
            ArrayList<String> categoryNameList = new ArrayList<>();
            category.stream().forEach(v -> {
                String categoryN = v.select("a").text().trim();

                categoryNameList.add(categoryN);
                categoryList.add(ShareApplication.CATEGORY_MAP.get(categoryN));
            });

            Elements elsDate = els.select("ul").get(0).select(".date-vb li");
            String dateIssued = elsDate.get(0).select("span").text().trim();
            String dateUpdated = elsDate.select(".cap-nhat").select("span").text().trim();

            lisData.add(toData(title, crawlerSource, StringUtils.join(categoryNameList, "|"), categoryList, dateIssued, dateUpdated));
        });

        return lisData;
    }

    private Law toData(Long id, String number, String numberPublic,
                       String datePublic, String agency, String type,
                       String signed, String content, String lawRefer) {

        Law law = new Law();
        law.setId(id);
        law.setNumber(number);
        law.setNumberPublic(numberPublic);

        if (StringUtils.isNotBlank(datePublic)) {
            try {
                law.setDatePublic(dateFormat.parse(datePublic));
            } catch (ParseException e) {
                law.setDatePublic(new Date(0));
            }
        } else {
            law.setDatePublic(new Date(0));
        }

        law.setCrawlerAgencyName(agency);
        law.setAgencyId(NumberUtils.toLong(ShareApplication.AGENCY_MAP.get(agency)));

        law.setCrawlerTypeName(type);
        law.setTypeId(NumberUtils.toLong(ShareApplication.TYPE_MAP.get(type)));

        law.setSigned(signed);
        law.setLawStatus(0);
        law.setContent(content);
        law.setMetaUrl(StringUtil.stripAccents(type + " " +  number + " ban hành " + agency, "-"));
        law.setCrawlerLawRefer(lawRefer);

        System.out.println(law.getNumber());
        System.out.println(law.getNumberPublic());
        System.out.println(datePublic);
        System.out.println(law.getCrawlerAgencyName() + "#" + law.getAgencyId());
        System.out.println(law.getCrawlerTypeName() + "#" + law.getTypeId());
        System.out.println(law.getSigned());
        System.out.println(law.getMetaUrl());
        System.out.println(law.getCrawlerLawRefer());
//        System.out.println(law.getContent());
        System.out.println("-------------------------------------");

        return law;
    }

    private Law toData(String title, String url, String categoryName, ArrayList<String> categoryId,
                       String dateIssued, String dateUpdate) {
        Law law = new Law();
        law.setName(title);
        law.setCrawlerSource(url);
        law.setCrawlerCategoryName(categoryName);

        law.setCategory(categoryId);

        if (StringUtils.isNotBlank(dateIssued)) {
            try {
                law.setDateIssued(dateFormat.parse(dateIssued));
            } catch (ParseException e) {

            }
        }

        if (StringUtils.isNotBlank(dateUpdate)) {
            try {
                law.setUpdatedDate(dateFormat.parse(dateUpdate));
            } catch (ParseException e) {

            }
        }

//        System.out.println(law.getName());
//        System.out.println(law.getCrawlerSource());
//        System.out.println(law.getCrawlerCategoryName() + "#" + StringUtils.join(law.getCategory(), ","));
//        System.out.println(dateIssued);
//        System.out.println(dateUpdate);
//        System.out.println("-------------------------------------");
        return law;
    }

    public static void main(String[] args) {
        try {
            ThuKyLuatParser thuKyLuatParser = new ThuKyLuatParser();
            //thuKyLuatParser.readQuery("https://thukyluat.vn/tim-kiem/?page=19054");

            thuKyLuatParser.readDetail("https://thukyluat.vn/vb/luat-sua-doi-bo-luat-lao-dong-51766.html", 12L);
//            thuKyLuatParser.readDetail("https://thukyluat.vn/vb/thong-tu-12-2021-tt-btc-muc-thu-khai-nop-phi-su-dung-ket-cau-ha-tang-duong-sat-718cd.html", 12L);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
