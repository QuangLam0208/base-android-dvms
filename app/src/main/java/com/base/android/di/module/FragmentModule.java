package com.base.android.di.module;

import android.content.Context;

import androidx.core.util.Supplier;
import androidx.lifecycle.ViewModelProvider;

import com.base.android.MVVMApplication;
import com.base.android.ViewModelProviderFactory;
import com.base.android.data.Repository;
import com.base.android.di.scope.FragmentScope;
import com.base.android.ui.base.fragment.BaseFragment;
import com.base.android.ui.main.account.profile.ProfileViewModel;
import com.base.android.ui.main.company.CompanyViewModel;
import com.base.android.ui.main.courses.CoursesViewModel;
import com.base.android.ui.main.mentor.MentorViewModel;
import com.base.android.ui.main.reviews.ReviewsViewModel;

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


    @Provides
    @FragmentScope
    CoursesViewModel provideCoursesViewModel(Repository repository, Context application) {
        Supplier<CoursesViewModel> supplier = () -> new CoursesViewModel(repository, (MVVMApplication) application);
        ViewModelProviderFactory<CoursesViewModel> factory = new ViewModelProviderFactory<>(CoursesViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(CoursesViewModel.class);
    }

    @Provides
    @FragmentScope
    ReviewsViewModel provideReviewsViewModel(Repository repository, Context application) {
        Supplier<ReviewsViewModel> supplier = () -> new ReviewsViewModel(repository, (MVVMApplication) application);
        ViewModelProviderFactory<ReviewsViewModel> factory = new ViewModelProviderFactory<>(ReviewsViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(ReviewsViewModel.class);
    }

    @Provides
    @FragmentScope
    MentorViewModel provideMentorViewModel(Repository repository, Context application) {
        Supplier<MentorViewModel> supplier = () -> new MentorViewModel(repository, (MVVMApplication) application);
        ViewModelProviderFactory<MentorViewModel> factory = new ViewModelProviderFactory<>(MentorViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(MentorViewModel.class);
    }

    @Provides
    @FragmentScope
    CompanyViewModel provideCompanyViewModel(Repository repository, Context application) {
        Supplier<CompanyViewModel> supplier = () -> new CompanyViewModel(repository, (MVVMApplication) application);
        ViewModelProviderFactory<CompanyViewModel> factory = new ViewModelProviderFactory<>(CompanyViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(CompanyViewModel.class);
    }

    @Provides
    @FragmentScope
    ProfileViewModel provideProfileViewModel(Repository repository, Context application) {
        Supplier<ProfileViewModel> supplier = () -> new ProfileViewModel(repository, (MVVMApplication) application);
        ViewModelProviderFactory<ProfileViewModel> factory = new ViewModelProviderFactory<>(ProfileViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(ProfileViewModel.class);
    }

}
