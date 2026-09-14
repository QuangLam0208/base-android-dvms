package com.base.android.ui.main.chart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.base.android.MVVMApplication;
import com.base.android.R;
import com.base.android.data.Repository;
import com.base.android.ui.base.fragment.BaseFragmentViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ChartViewModel extends BaseFragmentViewModel {

    private final MutableLiveData<ChartType> selectedChartType = new MutableLiveData<>(ChartType.BAR);
    private final MutableLiveData<String> startDateText = new MutableLiveData<>();
    private final MutableLiveData<String> endDateText = new MutableLiveData<>();
    private final MutableLiveData<String> filterDisplayText = new MutableLiveData<>();
    private final MutableLiveData<Long> dataVersion = new MutableLiveData<>(0L);
    private final MutableLiveData<ChartSummaryData> chartSummary = new MutableLiveData<>();

    private long startDateMillis;
    private long endDateMillis;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public ChartViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
        initDefaultDates();
    }

    private void initDefaultDates() {
        Calendar calendar = Calendar.getInstance();
        // Clear time to midnight for exact date comparison
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        endDateMillis = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, -7);
        startDateMillis = calendar.getTimeInMillis();

        updateDateTexts();
        refreshSummary();
    }

    public LiveData<ChartType> getSelectedChartType() {
        return selectedChartType;
    }

    public LiveData<String> getStartDateText() {
        return startDateText;
    }

    public LiveData<String> getEndDateText() {
        return endDateText;
    }

    public LiveData<String> getFilterDisplayText() {
        return filterDisplayText;
    }

    public LiveData<Long> getDataVersion() {
        return dataVersion;
    }

    public LiveData<ChartSummaryData> getChartSummary() {
        return chartSummary;
    }

    public void setChartType(ChartType chartType) {
        if (selectedChartType.getValue() != chartType) {
            selectedChartType.setValue(chartType);
            refreshSummary();
        }
    }

    public boolean setStartDate(long startMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long normalizedStart = cal.getTimeInMillis();

        if (normalizedStart > endDateMillis) {
            showErrorMessage(application.getString(R.string.error_start_date_after_end));
            return false;
        }

        this.startDateMillis = normalizedStart;
        updateDateTexts();
        notifyDataChanged();
        return true;
    }

    public boolean setEndDate(long endMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(endMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long normalizedEnd = cal.getTimeInMillis();

        if (normalizedEnd < startDateMillis) {
            showErrorMessage(application.getString(R.string.error_end_date_before_start));
            return false;
        }

        this.endDateMillis = normalizedEnd;
        updateDateTexts();
        notifyDataChanged();
        return true;
    }

    public long getSeedModifier() {
        long dayStart = startDateMillis / (1000 * 60 * 60 * 24);
        long dayEnd = endDateMillis / (1000 * 60 * 60 * 24);
        return dayStart + dayEnd;
    }

    private void updateDateTexts() {
        String startStr = dateFormat.format(new Date(startDateMillis));
        String endStr = dateFormat.format(new Date(endDateMillis));
        startDateText.setValue(startStr);
        endDateText.setValue(endStr);

        if (startDateMillis == endDateMillis) {
            filterDisplayText.setValue(startStr);
        } else {
            filterDisplayText.setValue(startStr + " - " + endStr);
        }
    }

    private void notifyDataChanged() {
        long nextVersion = (dataVersion.getValue() != null ? dataVersion.getValue() : 0) + 1;
        dataVersion.setValue(nextVersion);
        refreshSummary();
    }

    public void refreshSummary() {
        ChartType type = selectedChartType.getValue();
        if (type == null) type = ChartType.BAR;
        ChartSummaryData summary = ChartDataGenerator.generateSummary(application, type, getSeedModifier());
        chartSummary.setValue(summary);
    }

    public long getStartDateMillis() {
        return startDateMillis;
    }

    public long getEndDateMillis() {
        return endDateMillis;
    }
}
