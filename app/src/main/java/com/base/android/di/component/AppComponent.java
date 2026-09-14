package com.base.android.di.component;


import android.app.Application;
import android.content.Context;

import com.base.android.MVVMApplication;
import com.base.android.data.Repository;
import com.base.android.data.notification.OneSignalManager;
import com.base.android.di.module.AppModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(MVVMApplication app);

    Repository getRepository();

    Context getContext();

    OneSignalManager getOneSignalManager();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }
}
