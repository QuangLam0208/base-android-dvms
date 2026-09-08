package com.base.android.ui.main.courses;

import com.base.android.MVVMApplication;
import com.base.android.data.Repository;
import com.base.android.data.model.api.response.classroom.ClassRoomResponse;
import com.base.android.ui.base.fragment.BaseFragmentViewModel;
import com.base.android.ui.main.MainCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class CoursesViewModel extends BaseFragmentViewModel {

    public CoursesViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void getListCourse(MainCallback<List<ClassRoomResponse>> callback) {
        Map<String, Object> query = new HashMap<>();

        query.put("pageable.page", 0);
        query.put("pageable.size", 20);

        showLoading();
        compositeDisposable.add(
                repository.getApiService()
                        .getListClassRoom(query)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    hideLoading();
                                    if (response.isResult() && response.getData() != null) {
                                        callback.doSuccess(response.getData().getContent());
                                    } else {
                                        callback.doFail();
                                    }
                                },
                                throwable -> {
                                    Timber.e(throwable);
                                    hideLoading();
                                    callback.doError(throwable);
                                }
                        )
        );
    }
}


