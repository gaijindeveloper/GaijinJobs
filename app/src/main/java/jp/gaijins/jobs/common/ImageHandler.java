package jp.gaijins.jobs.common;

import android.content.Context;

import com.squareup.picasso.Picasso;

import java.util.concurrent.Executors;

/**
 * Created by nayak.vishal on 2015/12/10.
 */
public class ImageHandler {

    private static Picasso instance;

    public static Picasso getSharedInstance(Context context) {
        if (instance == null) {
            instance = new Picasso.Builder(context).executor(Executors.newSingleThreadExecutor()).build();
        }
        return instance;
    }
}
