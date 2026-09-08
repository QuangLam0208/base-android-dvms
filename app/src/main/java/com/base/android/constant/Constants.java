package com.base.android.constant;

import com.base.android.BuildConfig;

public class Constants {
    public static final String DB_NAME = "room";
    public static final String PREF_NAME = "mvvm.prefs";

    public static final String VALUE_BEARER_TOKEN_DEFAULT="NULL";

    //Local Action manager
    public static final String ACTION_EXPIRED_TOKEN ="ACTION_EXPIRED_TOKEN";
    public static final String INSTAGRAM_LOGIN_URL = "https://www.instagram.com/accounts/login/";
    public static final String INSTAGRAM_URL = "https://www.instagram.com/";

    public static final String MEDIA_URL = BuildConfig.MEDIA_URL;
    public static final String IMAGE_DOWNLOAD_PATH = "v1/file/download";

    public static String TOKEN_GUEST;

    private Constants(){}
}
