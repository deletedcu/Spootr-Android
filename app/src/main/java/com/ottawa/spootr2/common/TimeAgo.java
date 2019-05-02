package com.ottawa.spootr2.common;

import android.util.Log;

import java.util.Date;

/**
 * Created by king on 25/01/16.
 */
public class TimeAgo {
    private static final int MINUTE = 60;
    private static final int HOUR = (MINUTE * 60);
    private static final int DAY = (HOUR * 24);
    private static final int WEEK = (DAY * 7);
    private static final int MONTH = (DAY * 30);
    private static final int YEAR = (MONTH * 12);

    public String timeAgo(Date date) {
        long prefix = 0;
        String suffix;

        long secondsSinceNow = (System.currentTimeMillis() - date.getTime()) / 1000;
        if (secondsSinceNow < 0)
            secondsSinceNow = 1;

        // Seconds
        if (secondsSinceNow < MINUTE) {
            prefix = secondsSinceNow;
            suffix = "s";
        }
        // Minute
        else if (secondsSinceNow < HOUR) {
            prefix = secondsSinceNow / MINUTE;
            suffix = "m";
        }
        // Hour
        else if (secondsSinceNow < DAY) {
            prefix = secondsSinceNow / HOUR;
            suffix = "h";
        }
        // Day
        else if (secondsSinceNow < WEEK) {
            prefix = secondsSinceNow / DAY;
            suffix = "d";
        }
        // Week
        else if (secondsSinceNow < MONTH) {
            prefix = secondsSinceNow / WEEK;
            suffix = "w";
        }
        // Month
        else if (secondsSinceNow < YEAR) {
            prefix = secondsSinceNow / MONTH;
            suffix = "mo";
        }
        // Year
        else {
            prefix = secondsSinceNow / YEAR;
            suffix = "y";
        }

        return String.format("%d%s", prefix, suffix);

    }
}
