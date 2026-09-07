package com.base.android.ui.courses;

import com.base.android.MVVMApplication;
import com.base.android.data.Repository;
import com.base.android.ui.base.fragment.BaseFragmentViewModel;

public class CoursesViewModel extends BaseFragmentViewModel {
    public CoursesViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
