package com.base.android;

import android.app.Application;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import es.dmoral.toasty.Toasty;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import lombok.Getter;
import lombok.Setter;
import com.base.android.di.component.AppComponent;
import com.base.android.di.component.DaggerAppComponent;
import com.base.android.others.MyTimberDebugTree;
import com.base.android.others.MyTimberReleaseTree;
import android.content.Context;
import com.base.android.constant.Constants;
import com.base.android.data.local.prefs.AppPreferencesService;
import com.base.android.data.local.prefs.PreferencesService;
import com.base.android.helper.LocaleHelper;
import com.base.android.utils.DialogUtils;
import com.google.gson.Gson;
import timber.log.Timber;

public class MVVMApplication extends Application implements LifecycleObserver {
    @Setter
    private AppCompatActivity currentActivity;

    @Getter
    private AppComponent appComponent;
    private Boolean inBackground;

    @Override
    protected void attachBaseContext(Context base) {
        PreferencesService prefs = new AppPreferencesService(base, Constants.PREF_NAME, new Gson());
        String langCode = prefs.getAppLanguage();
        super.attachBaseContext(LocaleHelper.setLocale(base, langCode));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new MyTimberDebugTree());
        }else{
            Timber.plant(new MyTimberReleaseTree());
        }

        appComponent = DaggerAppComponent.builder()
                .application(this)
                .build();
        appComponent.inject(this);

        // Init Toasty
        Toasty.Config.getInstance()
                .allowQueue(false)
                .apply();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
//        insertMock();
//        startOrderSchedule();
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        //App in background
        Timber.d("APP IN BACKGROUND");
        inBackground = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        // App in foreground
        Timber.d("APP IN FOREGROUND");
        inBackground = false;
    }


    public void getUser(){
        appComponent.getRepository().getRoomService().userDao().loadAll()
                .subscribeOn(Schedulers.io())
                .subscribe();
    }


    public PublishSubject<Integer> showDialogNoInternetAccess(){
        final PublishSubject<Integer> subject = PublishSubject.create();
        currentActivity.runOnUiThread(() ->
                DialogUtils.dialogConfirm(currentActivity, currentActivity.getResources().getString(R.string.newtwork_error),
                        currentActivity.getResources().getString(R.string.newtwork_error_button_retry),
                        (dialogInterface, i) -> subject.onNext(1), currentActivity.getResources().getString(R.string.newtwork_error_button_exit),
                        (dialogInterface, i) -> System.exit(0))
        );
        return subject;
    }

}
