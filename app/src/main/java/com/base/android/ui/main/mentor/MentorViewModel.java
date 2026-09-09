package com.base.android.ui.main.mentor;

import com.base.android.MVVMApplication;
import com.base.android.data.Repository;
import com.base.android.data.model.api.response.mentor.MentorResponse;
import com.base.android.ui.base.fragment.BaseFragmentViewModel;
import com.base.android.ui.main.MainCallback;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class MentorViewModel extends BaseFragmentViewModel {

    public MentorViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void getPublicMentors(MainCallback<List<MentorResponse>> callback) {
        showLoading();
        compositeDisposable.add(
                repository.getApiService()
                        .getPublicMentors()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    hideLoading();
                                    if (response != null && response.isResult() && response.getData() != null) {
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
