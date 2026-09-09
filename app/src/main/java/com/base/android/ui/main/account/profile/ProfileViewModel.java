package com.base.android.ui.main.account.profile;

import com.base.android.MVVMApplication;
import com.base.android.constant.Constants;
import com.base.android.data.Repository;
import com.base.android.data.local.prefs.PreferencesService;
import com.base.android.ui.base.fragment.BaseFragmentViewModel;

public class ProfileViewModel extends BaseFragmentViewModel {

    public ProfileViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void logout() {
        this.token = null;
        repository.setToken(Constants.VALUE_BEARER_TOKEN_DEFAULT);
        repository.getSharedPreferences().removeKey(PreferencesService.KEY_BEARER_TOKEN);
        repository.getSharedPreferences().removeKey(PreferencesService.KEY_BEARER_REFRESH_TOKEN);
    }
}
