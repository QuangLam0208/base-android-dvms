package com.base.android.di.module;

import com.base.android.data.Repository;
import com.base.android.di.scope.FragmentScope;
import com.base.android.ui.base.fragment.BaseFragment;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentModule {

    private BaseFragment<?, ?> fragment;

    public FragmentModule(BaseFragment<?, ?> fragment) {
        this.fragment = fragment;
    }

    @Named("access_token")
    @Provides
    @FragmentScope
    String provideToken(Repository repository) {
        return repository.getToken();
    }

}
