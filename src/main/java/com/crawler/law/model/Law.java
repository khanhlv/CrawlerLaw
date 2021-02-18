package com.crawler.law.model;

import java.util.ArrayList;
import java.util.Date;

public class Law {
    private Long id;
    private String name;
    private String number;
    private long agencyId;
    private long typeId;
    private String numberPublic;
    private Date dateIssued;
    private Date dateExpired;
    private Date datePublic;
    private String signed;
    private Date updatedDate;
    private String content;
    private long lawStatus;
    private String file;
    private String crawlerSource;
    private long status;
    private String crawlerTypeName;
    private String crawlerAgencyName;
    private String crawlerCategoryName;
    private String crawlerLawRefer;
    private ArrayList<String> category;
    private String metaUrl;
    private String googleDriveId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public long getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(long agencyId) {
        this.agencyId = agencyId;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public String getNumberPublic() {
        return numberPublic;
    }

    public void setNumberPublic(String numberPublic) {
        this.numberPublic = numberPublic;
    }

    public Date getDateIssued() {
        return dateIssued;
    }

    public void setDateIssued(Date dateIssued) {
        this.dateIssued = dateIssued;
    }

    public Date getDateExpired() {
        return dateExpired;
    }

    public void setDateExpired(Date dateExpired) {
        this.dateExpired = dateExpired;
    }

    public Date getDatePublic() {
        return datePublic;
    }

    public void setDatePublic(Date datePublic) {
        this.datePublic = datePublic;
    }

    public String getSigned() {
        return signed;
    }

    public void setSigned(String signed) {
        this.signed = signed;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getLawStatus() {
        return lawStatus;
    }

    public void setLawStatus(long lawStatus) {
        this.lawStatus = lawStatus;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getCrawlerSource() {
        return crawlerSource;
    }

    public void setCrawlerSource(String crawlerSource) {
        this.crawlerSource = crawlerSource;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public String getCrawlerTypeName() {
        return crawlerTypeName;
    }

    public void setCrawlerTypeName(String crawlerTypeName) {
        this.crawlerTypeName = crawlerTypeName;
    }

    public String getCrawlerAgencyName() {
        return crawlerAgencyName;
    }

    public void setCrawlerAgencyName(String crawlerAgencyName) {
        this.crawlerAgencyName = crawlerAgencyName;
    }

    public String getCrawlerCategoryName() {
        return crawlerCategoryName;
    }

    public void setCrawlerCategoryName(String crawlerCategoryName) {
        this.crawlerCategoryName = crawlerCategoryName;
    }

    public ArrayList<String> getCategory() {
        return category;
    }

    public void setCategory(ArrayList<String> category) {
        this.category = category;
    }

    public String getMetaUrl() {
        return metaUrl;
    }

    public void setMetaUrl(String metaUrl) {
        this.metaUrl = metaUrl;
    }

    public String getCrawlerLawRefer() {
        return crawlerLawRefer;
    }

    public void setCrawlerLawRefer(String crawlerLawRefer) {
        this.crawlerLawRefer = crawlerLawRefer;
    }

    public String getGoogleDriveId() {
        return googleDriveId;
    }

    public void setGoogleDriveId(String googleDriveId) {
        this.googleDriveId = googleDriveId;
    }
}
