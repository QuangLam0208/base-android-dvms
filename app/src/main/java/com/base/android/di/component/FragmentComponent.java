package com.base.android.di.component;


import com.base.android.di.module.FragmentModule;
import com.base.android.di.scope.FragmentScope;
import com.base.android.ui.main.account.profile.ProfileFragment;
import com.base.android.ui.main.company.CompanyFragment;
import com.base.android.ui.main.courses.CoursesFragment;
import com.base.android.ui.main.mentor.MentorFragment;
import com.base.android.ui.main.reviews.ReviewsFragment;

import dagger.Component;

@FragmentScope
@Component(modules = {FragmentModule.class},dependencies = AppComponent.class)
public interface FragmentComponent {
    void inject(CoursesFragment fragment);
    void inject(ReviewsFragment fragment);
    void inject(MentorFragment fragment);
    void inject(CompanyFragment fragment);
    void inject(ProfileFragment fragment);
}
