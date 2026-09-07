package com.base.android.di.component;

import com.base.android.di.module.ActivityModule;
import com.base.android.di.scope.ActivityScope;
import com.base.android.ui.main.MainActivity;

import dagger.Component;

@ActivityScope
@Component(modules = {ActivityModule.class}, dependencies = AppComponent.class)
public interface ActivityComponent {
    void inject(MainActivity activity);
}

