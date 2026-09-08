package com.base.android.data;

import com.base.android.data.local.prefs.PreferencesService;
import com.base.android.data.local.room.RoomService;
import com.base.android.data.remote.ApiService;
import com.base.android.data.remote.MasterApiService;


public interface Repository {

    /**
     * ################################## Preference section ##################################
     */
    String getToken();
    void setToken(String token);

    PreferencesService getSharedPreferences();


    /**
     *  ################################## Remote api ##################################
     */
    ApiService getApiService();

    MasterApiService getMasterApiService();

    RoomService getRoomService();

}
