package com.codepath.apps.mysimpletweets.utils;

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by gauravb on 3/24/17.
 */

public class DateHelper {
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            if (relativeDate.equals("Yesterday")) {
                return "1d";
            } else if (relativeDate.startsWith("In")) {
                return "now";
            }
            relativeDate = relativeDate.replace("ago", "");
            relativeDate = relativeDate.replace("seconds", "s");
            relativeDate = relativeDate.replace("second", "s");
            relativeDate = relativeDate.replace("months", "m");
            relativeDate = relativeDate.replace("month", "m");
            relativeDate = relativeDate.replace("weeks", "w");
            relativeDate = relativeDate.replace("week", "w");
            relativeDate = relativeDate.replace("days", "d");
            relativeDate = relativeDate.replace("day", "d");
            relativeDate = relativeDate.replace("hours", "h");
            relativeDate = relativeDate.replace("hour", "h");
            relativeDate = relativeDate.replace("minutes", "m");
            relativeDate = relativeDate.replace("minute", "m");
            relativeDate = relativeDate.replace("years", "y");
            relativeDate = relativeDate.replace("year", "y");
            String[] a = relativeDate.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String s : a) {
                sb.append(s);
            }
            return sb.toString();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }
}
