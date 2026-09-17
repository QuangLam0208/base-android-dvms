package com.base.android.ui.main.account.profile;

import com.base.android.MVVMApplication;
import com.base.android.constant.Constants;
import com.base.android.data.Repository;
import com.base.android.data.local.prefs.PreferencesService;
import com.base.android.ui.base.fragment.BaseFragmentViewModel;

import androidx.databinding.ObservableField;
import com.base.android.R;
import com.base.android.helper.LocaleHelper;

public class ProfileViewModel extends BaseFragmentViewModel {

    public final ObservableField<String> currentThemeDisplay = new ObservableField<>();
    public final ObservableField<String> currentLanguageDisplay = new ObservableField<>();

    public ProfileViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
        updateThemeDisplay();
        updateLanguageDisplay();
    }

    public void updateThemeDisplay() {
        String currentTheme = getTheme();
        if (PreferencesService.THEME_MODE_LIGHT.equals(currentTheme)) {
            currentThemeDisplay.set(application.getString(R.string.theme_light));
        } else if (PreferencesService.THEME_MODE_SYSTEM.equals(currentTheme)) {
            currentThemeDisplay.set(application.getString(R.string.theme_system));
        } else {
            currentThemeDisplay.set(application.getString(R.string.theme_dark));
        }
    }

    public void updateLanguageDisplay() {
        String currentLang = getLanguage();
        if (LocaleHelper.LANGUAGE_EN.equals(currentLang)) {
            currentLanguageDisplay.set(application.getString(R.string.language_en));
        } else {
            currentLanguageDisplay.set(application.getString(R.string.language_vi));
        }
    }

    public static final String KEY_USER_AVATAR_URI = "KEY_USER_AVATAR_URI";

    public void saveAvatarUri(String uriString) {
        repository.getSharedPreferences().setString(KEY_USER_AVATAR_URI, uriString);
    }

    public String getSavedAvatarUri() {
        return repository.getSharedPreferences().getStringVal(KEY_USER_AVATAR_URI);
    }

    public void logout() {
        this.token = null;
        repository.setToken(Constants.VALUE_BEARER_TOKEN_DEFAULT);
        repository.getSharedPreferences().removeKey(PreferencesService.KEY_BEARER_TOKEN);
        repository.getSharedPreferences().removeKey(PreferencesService.KEY_BEARER_REFRESH_TOKEN);
    }
}
