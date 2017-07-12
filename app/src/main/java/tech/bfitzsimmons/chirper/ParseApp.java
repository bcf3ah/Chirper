package tech.bfitzsimmons.chirper;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseLiveQueryClient;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Brian on 7/7/2017.
 */

public class ParseApp extends Application {
    public static ParseLiveQueryClient parseLiveQueryClient = null;

    @Override
    public void onCreate() {
        super.onCreate();

        //Enable us to store data locally
        Parse.enableLocalDatastore(this);

        //THIS IS FOR BACK4APP
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("YIkHrY8G4tsjjUgoYFYil2UTcHikk9Ek7nEtYYrN")
                .clientKey("DBbwI8M27M2zVDw973p4fz2CqmHKiz9JmTdBhoCQ")
                .server("https://parseapi.back4app.com/").build()
        );

        //web socket url: wss://chirper.back4app.io

        try {
            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI("wss://chirper.back4app.io"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        //Grant read/write permissions
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

    }
}
