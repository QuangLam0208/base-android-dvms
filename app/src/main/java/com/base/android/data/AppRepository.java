package com.base.android.data;

import com.base.android.data.local.prefs.PreferencesService;
import com.base.android.data.local.room.RoomService;
import com.base.android.data.remote.ApiService;
import com.base.android.data.remote.MasterApiService;
import com.base.android.di.qualifier.MasterApi;

import javax.inject.Inject;

public class AppRepository implements Repository {

    private final ApiService mApiService;
    private final MasterApiService mMasterApiService;
    private final PreferencesService mPreferencesHelper;
    private final RoomService roomService;

    @Inject
    public AppRepository(
            PreferencesService preferencesHelper,
            ApiService apiService,
            @MasterApi MasterApiService masterApiService,
            RoomService roomService
    ) {
        this.mPreferencesHelper = preferencesHelper;
        this.mApiService = apiService;
        this.mMasterApiService = masterApiService;
        this.roomService = roomService;
    }

    /**
     * ################################## Preference section ##################################
     */
    @Override
    public String getToken() {
        return mPreferencesHelper.getToken();
    }

    @Override
    public void setToken(String token) {
        mPreferencesHelper.setToken(token);
    }

    @Override
    public PreferencesService getSharedPreferences(){
        return mPreferencesHelper;
    }



    /**
    *  ################################## Remote api ##################################
    */
    @Override
    public ApiService getApiService(){
        return mApiService;
    }

    @Override
    public MasterApiService getMasterApiService() {
        return mMasterApiService;
    }

    @Override
    public RoomService getRoomService() {
        return roomService;
    }
}
