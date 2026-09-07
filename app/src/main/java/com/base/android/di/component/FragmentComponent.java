package com.base.android.di.component;


import com.base.android.di.module.FragmentModule;
import com.base.android.di.scope.FragmentScope;
import com.base.android.ui.company.CompanyFragment;
import com.base.android.ui.courses.CoursesFragment;
import com.base.android.ui.mentor.MentorFragment;
import com.base.android.ui.reviews.ReviewsFragment;

import dagger.Component;

@FragmentScope
@Component(modules = {FragmentModule.class},dependencies = AppComponent.class)
public interface FragmentComponent {
    void inject(CoursesFragment fragment);
    void inject(ReviewsFragment fragment);
    void inject(MentorFragment fragment);
    void inject(CompanyFragment fragment);
}
