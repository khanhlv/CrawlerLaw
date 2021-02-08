package com.crawler.law.enums;

public enum  Crawler {
    AMAZON_CO_UK("queue_amazon_co_uk", "data_amazon_co_uk", "crawler_amazon_co_uk", "https://www.amazon.co.uk"),
    AMAZON_COM("queue_amazon_com", "data_amazon_com", "crawler_amazon_com","https://www.amazon.com");

    Crawler(String tableQueue, String tableData, String tableCrawler, String site) {
        this.tableQueue = tableQueue;
        this.tableData = tableData;
        this.tableCrawler = tableCrawler;
        this.site = site;
    }

    private String tableQueue;
    private String tableData;
    private String tableCrawler;

    private String site;

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getTableQueue() {
        return tableQueue;
    }

    public void setTableQueue(String tableQueue) {
        this.tableQueue = tableQueue;
    }

    public String getTableData() {
        return tableData;
    }

    public void setTableData(String tableData) {
        this.tableData = tableData;
    }

    public String getTableCrawler() {
        return tableCrawler;
    }

    public void setTableCrawler(String tableCrawler) {
        this.tableCrawler = tableCrawler;
    }
}
