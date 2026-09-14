package com.base.android.data.notification;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;

import com.base.android.BuildConfig;
import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;
import com.onesignal.user.subscriptions.IPushSubscriptionObserver;
import com.onesignal.user.subscriptions.PushSubscriptionChangedState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Centralized singleton wrapper for all OneSignal SDK interactions.
 * Isolates direct OneSignal SDK calls from the rest of the application.
 */
@Singleton
public class OneSignalManager {

    private static final String PREF_NAME = "onesignal_integration_prefs";
    private static final String KEY_DIALOG_SHOWN = "key_dialog_shown";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isInitialized = false;

    // Ensures the verification dialog is shown exactly once
    private final AtomicBoolean dialogShown = new AtomicBoolean(false);

    // OneSignal stores observers weakly — keep a strong reference for the app/screen lifetime
    private IPushSubscriptionObserver pushSubscriptionObserver;

    @Inject
    public OneSignalManager() {
    }

    /**
     * Initializes the OneSignal SDK with the given App ID on a background thread.
     */
    public void initialize(Context context, String appId) {
        executor.execute(() -> {
            if (isInitialized) {
                return;
            }
            if (BuildConfig.DEBUG) {
                OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
            }
            OneSignal.initWithContext(context.getApplicationContext(), appId);
            isInitialized = true;
            Timber.i("OneSignal initialized with App ID: %s", appId);
        });
    }

    /**
     * Authenticates a user with OneSignal using an external user ID.
     */
    public void login(String externalId) {
        executor.execute(() -> {
            if (externalId != null && !externalId.isEmpty()) {
                OneSignal.login(externalId);
                Timber.d("OneSignal login called for externalId: %s", externalId);
            }
        });
    }

    /**
     * Logs out the current user session from OneSignal.
     */
    public void logout() {
        executor.execute(() -> {
            OneSignal.logout();
            Timber.d("OneSignal logout called");
        });
    }

    /**
     * Adds an email address to the current user's profile.
     */
    public void addEmail(String email) {
        executor.execute(() -> {
            if (email != null && !email.isEmpty()) {
                OneSignal.getUser().addEmail(email);
            }
        });
    }

    /**
     * Removes an email address from the current user's profile.
     */
    public void removeEmail(String email) {
        executor.execute(() -> {
            if (email != null && !email.isEmpty()) {
                OneSignal.getUser().removeEmail(email);
            }
        });
    }

    /**
     * Adds an SMS phone number to the current user's profile.
     */
    public void addSms(String number) {
        executor.execute(() -> {
            if (number != null && !number.isEmpty()) {
                OneSignal.getUser().addSms(number);
            }
        });
    }

    /**
     * Removes an SMS phone number from the current user's profile.
     */
    public void removeSms(String number) {
        executor.execute(() -> {
            if (number != null && !number.isEmpty()) {
                OneSignal.getUser().removeSms(number);
            }
        });
    }

    /**
     * Adds a key-value tag to the current user.
     */
    public void addTag(String key, String value) {
        executor.execute(() -> {
            if (key != null && value != null) {
                OneSignal.getUser().addTag(key, value);
            }
        });
    }

    /**
     * Removes a key tag from the current user.
     */
    public void removeTag(String key) {
        executor.execute(() -> {
            if (key != null) {
                OneSignal.getUser().removeTag(key);
            }
        });
    }

    /**
     * Configures the OneSignal logging level.
     */
    public void setLogLevel(LogLevel level) {
        OneSignal.getDebug().setLogLevel(level);
    }

    /**
     * Returns the current push subscription ID if available.
     */
    public String getPushSubscriptionId() {
        return OneSignal.getUser().getPushSubscription().getId();
    }

    /**
     * Requests push notification permissions.
     */
    public void requestPushPermission() {
        OneSignal.getNotifications().requestPermission(true, Continue.with(result -> {
            if (result != null) {
                Timber.d("OneSignal requestPermission success: %s", result.isSuccess());
            }
        }));
    }

    /**
     * A real, server-assigned subscription ID is non-empty and not the local- placeholder.
     */
    public boolean isRegistered(String subscriptionId) {
        return subscriptionId != null && !subscriptionId.isEmpty() && !subscriptionId.startsWith("local-");
    }

    /**
     * Sets up a push subscription observer and displays the verification dialog
     * once a server-assigned subscription ID is confirmed.
     */
    public void setupPushSubscriptionObserver(Activity activity) {
        IPushSubscriptionObserver observer = new IPushSubscriptionObserver() {
            @Override
            public void onPushSubscriptionChange(PushSubscriptionChangedState state) {
                if (state != null && state.getCurrent() != null) {
                    maybeShowIntegrationCompleteDialog(activity, state.getCurrent().getId());
                }
            }
        };
        this.pushSubscriptionObserver = observer;
        OneSignal.getUser().getPushSubscription().addObserver(observer);

        // The ID may already be server-assigned before the observer attaches,
        // so evaluate the current value immediately as well.
        maybeShowIntegrationCompleteDialog(activity, OneSignal.getUser().getPushSubscription().getId());
    }

    private boolean isDialogAlreadyShown(Context context) {
        if (dialogShown.get()) {
            return true;
        }
        if (context == null) {
            return false;
        }
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean shown = sp.getBoolean(KEY_DIALOG_SHOWN, false);
        if (shown) {
            dialogShown.set(true);
        }
        return shown;
    }

    private void markDialogAsShown(Context context) {
        dialogShown.set(true);
        if (context != null) {
            SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            sp.edit().putBoolean(KEY_DIALOG_SHOWN, true).apply();
        }
    }

    private void maybeShowIntegrationCompleteDialog(Activity activity, String subscriptionId) {
        if (activity == null || isDialogAlreadyShown(activity)) {
            return;
        }

        // If notification permission is already granted, do not show the verification dialog again
        if (OneSignal.getNotifications().getPermission()) {
            markDialogAsShown(activity);
            return;
        }

        if (isRegistered(subscriptionId) && dialogShown.compareAndSet(false, true)) {
            new Handler(Looper.getMainLooper()).post(() -> {
                showIntegrationCompleteDialog(activity);
            });
        }
    }

    /**
     * Shows the OneSignal Integration Complete alert dialog.
     */
    public void showIntegrationCompleteDialog(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            dialogShown.set(false); // allow retry if activity wasn't in a state to display
            return;
        }

        // Mark as permanently shown so it won't reappear across app restarts
        markDialogAsShown(activity);

        new AlertDialog.Builder(activity)
                .setTitle("Your OneSignal SDK integration is complete!")
                .setMessage(
                        "You can now send Push Notifications & In-App Messages through OneSignal. " +
                        "Tap below to enable push notifications."
                )
                .setPositiveButton("Got it", (dialog, which) -> {
                    requestPushPermission();
                })
                .setCancelable(false)
                .show();
    }
}
