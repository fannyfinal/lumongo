package org.lumongo.example.medline;

import org.lumongo.cluster.message.Lumongo;
import org.lumongo.cluster.message.Lumongo.LMAnalyzer;
import org.lumongo.fields.annotations.DefaultSearch;
import org.lumongo.fields.annotations.Faceted;
import org.lumongo.fields.annotations.Indexed;
import org.lumongo.fields.annotations.Settings;
import org.lumongo.fields.annotations.Sorted;
import org.lumongo.fields.annotations.UniqueId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Settings(
        indexName = "medline",
        numberOfSegments = 4,
        storeDocumentInIndex = true,
        storeDocumentInMongo = true)
public class MedlineDocument {

    @UniqueId
    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    private String pmid;

    @DefaultSearch
    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    @Sorted(type= Lumongo.SortAs.SortType.STRING)
    @Sorted(type= Lumongo.SortAs.SortType.STRING_LC, fieldName="titleLC")
    private String title;

    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    private String journalTitle;

    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    private String journalIso;

    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    private String abstractText;

    @Faceted
    @Indexed(
            analyzer = LMAnalyzer.DATE)
    private Date publicationDate;

    @Faceted
    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    private String pubYear;

    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    private String journalVolume;

    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    private String journalIssue;

    @Faceted
    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    private String journalCountry;


    @Faceted
    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    @Sorted(type = Lumongo.SortAs.SortType.STRING)
    private String issn;

    @Faceted
    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    private List<String> authors;

    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    private String pagination;

    @Indexed(
            analyzer = LMAnalyzer.STANDARD)
    private String citation;

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getJournalTitle() {
        return journalTitle;
    }

    public void setJournalTitle(String journalTitle) {
        this.journalTitle = journalTitle;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getJournalVolume() {
        return journalVolume;
    }

    public void setJournalVolume(String journalVolume) {
        this.journalVolume = journalVolume;
    }

    public String getJournalIssue() {
        return journalIssue;
    }

    public void setJournalIssue(String journalIssue) {
        this.journalIssue = journalIssue;
    }

    public String getJournalCountry() {
        return journalCountry;
    }

    public void setJournalCountry(String journalCountry) {
        this.journalCountry = journalCountry;
    }

    public String getJournalIso() {
        return journalIso;
    }

    public void setJournalIso(String journalIso) {
        this.journalIso = journalIso;
    }


    public void setPagination(String pagination) {
        this.pagination = pagination;
    }

    public String getPagination() {
        return pagination;
    }


    public String getPubYear() {
        return pubYear;
    }

    public void setPubYear(String pubYear) {
        this.pubYear = pubYear;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public void addAuthor(String author) {
        if (this.authors == null) {
            this.authors = new ArrayList<String>();
        }

        this.authors.add(author);

    }

    public List<String> getAuthors() {
        return authors;
    }

    @Override
    public String toString() {
        return "MedlineDocument{" +
                "pmid='" + pmid + '\'' +
                ", title='" + title + '\'' +
                ", journalTitle='" + journalTitle + '\'' +
                ", journalIso='" + journalIso + '\'' +
                ", abstractText='" + abstractText + '\'' +
                ", publicationDate=" + publicationDate +
                ", pubYear='" + pubYear + '\'' +
                ", journalVolume='" + journalVolume + '\'' +
                ", journalIssue='" + journalIssue + '\'' +
                ", journalCountry='" + journalCountry + '\'' +
                ", issn='" + issn + '\'' +
                ", authors=" + authors +
                ", pagination='" + pagination + '\'' +
                ", citation='" + citation + '\'' +
                '}';
    }
}
