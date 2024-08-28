package org.wildfly.extras.quickstart.microprofile.graphql.client.model;

import java.time.LocalDate;

public class Film {

    private String title;
    private Integer episodeID;
    private String director;
    private LocalDate releaseDate;
    private String desc;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getEpisodeID() {
        return episodeID;
    }

    public void setEpisodeID(Integer episodeID) {
        this.episodeID = episodeID;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}