package com.base.android.others;

import android.util.Log;

import timber.log.Timber;

public class MyTimberReleaseTree extends Timber.Tree {

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.ERROR || priority == Log.WARN) {
            Log.println(priority, tag, message);
        }
    }
}
