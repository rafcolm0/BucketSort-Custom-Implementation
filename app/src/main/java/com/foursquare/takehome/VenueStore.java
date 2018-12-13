package com.foursquare.takehome;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Simple data store to retrieve Venue data.
 * ***DO NOT EDIT***
 */
public final class VenueStore {
    private static VenueStore instance;

    public static String peopleFile = "people.json";

    public static VenueStore get() {
        if (instance == null) {
            instance = new VenueStore();
        }
        return instance;
    }

    private VenueStore() {
    }

    public Venue getVenue(Context context) {
        try {
            InputStream is = context.getAssets().open(peopleFile);
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            PeopleHereJsonResponse response = new Gson().fromJson(inputStreamReader, PeopleHereJsonResponse.class);
            return response.getVenue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
