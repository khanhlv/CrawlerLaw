package com.crawler.law.util;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

public class AmazonUtil {

    public static Map<String, Object> properties(String dataInput) {

        if (dataInput.indexOf("var obj = jQuery.parseJSON('") < 0) {
            return Collections.emptyMap();
        }

        String dataParseJSON = dataInput.substring(dataInput.indexOf("var obj = jQuery.parseJSON('") + 28, dataInput.indexOf("data" +
                "[\"alwaysIncludeVideo\"]") - 4);

        Map<String, Object> mapDataParseJSON = new Gson().fromJson(dataParseJSON, Map.class);
        Map<String, Object> colorToAsin =  (Map<String, Object>) mapDataParseJSON.get("colorToAsin");

        if (colorToAsin.size() == 0) {
            return Collections.emptyMap();
        }

        Map<String, Object> colorImages =  (Map<String, Object>) mapDataParseJSON.get("colorImages");

        String dataAttribute = dataInput.substring(dataInput.indexOf("P.register('twister-js-init-dpx-data'"),
                dataInput.indexOf("useDesktopTwisterMetaAsset"));
        dataAttribute = dataAttribute.substring(dataAttribute.indexOf("var dataToReturn = ") + 19, dataAttribute.indexOf("return dataToReturn;") - 2);
        dataAttribute = dataAttribute.replaceFirst("num_total_variations", UUID.randomUUID().toString());

        Map<String, Object> mapDataAttribute = new Gson().fromJson(dataAttribute, Map.class);

        List<String> dimensionValues =  (List<String>) mapDataAttribute.get("dimensions");
        Map<String, List<String>> dimensionValuesDisplayData =  (Map<String, List<String>>) mapDataAttribute.get("dimensionValuesDisplayData");

        List<Map<String, Object>> dimensionValuesList = new ArrayList<>();

        dimensionValuesDisplayData.forEach((key, value) -> {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("asin", key);
            for(int i = 0; i < dimensionValues.size(); i++) {
                data.put(dimensionValues.get(i), value.get(i));
            }

            String name = StringUtils.join(value, " ").replaceAll("\"", "\\\\\"");

            dimensionValuesList.add(data);
            data.put("images", colorImages.get(name));
        });

        Map<String, Object> mapData = new LinkedHashMap<>();
        mapData.put("variationValues", mapDataAttribute.get("variationValues"));
        mapData.put("selectedVariations", mapDataAttribute.get("selected_variations"));
        mapData.put("dimensionValuesData", dimensionValuesList);

        return mapData;
    }
}
