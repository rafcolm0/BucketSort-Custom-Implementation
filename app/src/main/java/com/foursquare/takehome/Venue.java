package com.foursquare.takehome;

import java.util.List;

public final class Venue {
    private int id;
    private String name;
    private long openTime;
    private long closeTime;
    private List<Person> visitors;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getOpenTime() {
        return openTime;
    }

    public long getCloseTime() {
        return closeTime;
    }

    public List<Person> getVisitors() {
        return visitors;
    }
}
