package com.foursquare.takehome;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public final class Person {
    private int id;
    private String name;
    private long arriveTime;
    private long leaveTime;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getArriveTime() {
        return arriveTime;
    }

    public long getLeaveTime() {
        return leaveTime;
    }

    public Person(int id, String name, long arriveTime, long leaveTime) {
        this.id = id;
        this.name = name;
        this.arriveTime = arriveTime;
        this.leaveTime = leaveTime;
    }
}
