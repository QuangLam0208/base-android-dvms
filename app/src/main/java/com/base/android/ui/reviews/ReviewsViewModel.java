package com.base.android.ui.reviews;

import com.base.android.MVVMApplication;
import com.base.android.data.Repository;
import com.base.android.ui.base.fragment.BaseFragmentViewModel;

public class ReviewsViewModel extends BaseFragmentViewModel {
    public ReviewsViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
