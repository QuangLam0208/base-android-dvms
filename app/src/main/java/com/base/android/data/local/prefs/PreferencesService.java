package com.base.android.data.local.prefs;

import android.content.SharedPreferences;

public interface PreferencesService {
    public static final String KEY_BEARER_TOKEN="KEY_BEARER_TOKEN";
    public static final String KEY_BEARER_REFRESH_TOKEN="KEY_BEARER_REFRESH_TOKEN";

    String getToken();
    String getRefreshToken();
    void setToken(String token);
    void setRefreshToken(String refreshToken);

    void removeKey(String key);
    void removeAllKeys();
    boolean containKey(String key);
    void registerChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener);
    void unregisterChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener);

    void setBoolean(String key, boolean val);
    boolean getBooleanVal(String key);

    void setString(String key, String val);
    String getStringVal(String key);

    void setInt(String key, int val);
    int getIntVal(String key);

    void setLong(String key, long val);
    long getLongVal(String key);

    void setFloat(String key, float val);
    float getFloatVal(String key);

    <T> T getObjectVal(String key, Class<T> mModelClass);
}
