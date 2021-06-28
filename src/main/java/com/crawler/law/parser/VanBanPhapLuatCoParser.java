package com.crawler.law.parser;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawler.law.core.Consts;
import com.crawler.law.core.UserAgent;
import com.crawler.law.model.Law;
import com.google.gson.Gson;

public class VanBanPhapLuatCoParser {
    private static final Logger logger = LoggerFactory.getLogger(VanBanPhapLuatCoParser.class);
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static Map<String, Long> mapLawStatus = new HashMap<>();

    static  {
        mapLawStatus.put("Còn hiệu lực", 1L);
        mapLawStatus.put("Hết hiệu lực", 2L);
        mapLawStatus.put("Không xác định", 3L);
        mapLawStatus.put("Chưa có hiệu lực", 4L);
        mapLawStatus.put("Không còn phù hợp", 5L);
    }

    public Law readDetail(Law law) throws Exception {
        String url = "https://vanbanphapluat.co/api/search?kwd=" + URLEncoder.encode(law.getCrawlerTypeName() + " " + law.getNumber(), "UTF-8");
        System.out.println(url + " ["+law.getId()+"]");
        Connection connection = Jsoup.connect(url)
                .userAgent(UserAgent.getUserAgent())
                .timeout(Consts.TIMEOUT)
                .ignoreContentType(true)
                .maxBodySize(0);

        String data = connection.get().text();

        Map dataMap = new Gson().fromJson(data, Map.class);

        ArrayList<Map> listItems = (ArrayList<Map>)dataMap.get("Items");

        Map dataResult = listItems.stream().filter(v -> {
            boolean equalNumber = StringUtils.trimToEmpty(law.getNumber()).equals(StringUtils.trimToEmpty((String)v.get("SoHieu")));
            boolean equalDateIssued = false;
            boolean equalAgency = v.get("CoQuanBanHanh").toString().toLowerCase().contains(law.getCrawlerAgencyName().toLowerCase());
            boolean equalType = v.get("LoaiVanBan").toString().toLowerCase().contains(law.getCrawlerTypeName().toLowerCase());

            try {
                if (v.containsKey("NgayBanHanh")) {
                    String ngayBanHanh = (String) v.get("NgayBanHanh");
                    Date dateNgayBanHanh = dateFormat.parse(ngayBanHanh);
                    equalDateIssued = dateNgayBanHanh.getTime() == law.getDateIssued().getTime();
                }
            } catch (ParseException e) {

            }

            if (equalNumber && equalDateIssued && equalAgency && equalType) {
                return true;
            }
            return false;
        }).findFirst().orElse(null);

        if (dataResult != null) {
//            System.out.println("SoHieu: " + dataResult.get("SoHieu"));
//            System.out.println("TrichYeu" + dataResult.get("TrichYeu"));
//            System.out.println("NgayBanHanh: " + dataResult.get("NgayBanHanh"));
//            System.out.println("NgayHieuLuc: " + dataResult.get("NgayHieuLuc"));
//            System.out.println("NgayHetHieuLuc: " + dataResult.get("NgayHetHieuLuc"));
//            System.out.println(dataResult.get("NguoiKy"));
//            System.out.println(dataResult.get("TrinhTrangHieuLuc"));

            Map<String, Object> dataStatus = (Map<String, Object>) dataResult.get("TrinhTrangHieuLuc");
//            System.out.println("TrinhTrangHieuLuc: " +dataStatus.get("Title"));

            String ngayHieuLuc = (String) dataResult.get("NgayHieuLuc");
            if (ngayHieuLuc != null) {
                Date dateNgayHieuLuc = dateFormat.parse(ngayHieuLuc);
                law.setDateEffective(dateNgayHieuLuc);
            }

            String ngayHetHieuLuc = (String)dataResult.get("NgayHetHieuLuc");
            if (ngayHetHieuLuc != null) {
                Date dateNgayHetHieuLuc = dateFormat.parse(ngayHetHieuLuc);
                law.setDateExpired(dateNgayHetHieuLuc);
            }

            law.setLawStatus(mapLawStatus.get(dataStatus.get("Title")));

            return law;
        }

        return null;
    }

    public static void main(String[] args) throws Exception {
        Law law = new Law();
        law.setId(1L);
        law.setNumber("403/QĐ-UBND");
        law.setDateIssued(new SimpleDateFormat("dd/MM/yyyy").parse("26/07/2018"));
        law.setCrawlerTypeName("Quyết định");
        law.setCrawlerAgencyName("Tỉnh Bắc Ninh");
        new VanBanPhapLuatCoParser().readDetail(law);
    }

}
