package com.crawler.law.model;

import java.util.Date;

public class Law {
    private String id;
    private String name;
    private String number;
    private long agencyId;
    private long typeId;
    private String numberPublic;
    private Date dateIssued;
    private Date dateExpried;
    private Date datePublic;
    private String signed;
    private Date updatedDate;
    private String content;
    private long lawStatus;
    private String file;
    private String crawlerSource;
    private long status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Date getDateExpried() {
        return dateExpried;
    }

    public void setDateExpried(Date dateExpried) {
        this.dateExpried = dateExpried;
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
}
