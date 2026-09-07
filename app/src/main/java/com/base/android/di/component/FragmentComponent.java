package com.base.android.di.component;


import com.base.android.di.module.FragmentModule;
import com.base.android.di.scope.FragmentScope;

import dagger.Component;

@FragmentScope
@Component(modules = {FragmentModule.class},dependencies = AppComponent.class)
public interface FragmentComponent {

}
