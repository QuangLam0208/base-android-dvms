package com.base.android.ui.main.chart;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.base.android.BR;
import com.base.android.R;
import com.base.android.databinding.FragmentChartBinding;
import com.base.android.di.component.FragmentComponent;
import com.base.android.helper.ThemeHelper;
import com.base.android.ui.base.fragment.BaseFragment;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.Calendar;

public class ChartFragment extends BaseFragment<FragmentChartBinding, ChartViewModel> {

    public static ChartFragment newInstance() {
        return new ChartFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initChartStyling();
        setupFilterListeners();
        setupChartTypeChipListeners();
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getSelectedChartType().observe(getViewLifecycleOwner(), this::updateChartTypeUI);
        viewModel.getDataVersion().observe(getViewLifecycleOwner(), version -> reloadActiveChartData());

        viewModel.getStartDateText().observe(getViewLifecycleOwner(), text -> {
            if (text != null && binding != null) {
                binding.tvStartDateValue.setText(text);
            }
        });

        viewModel.getEndDateText().observe(getViewLifecycleOwner(), text -> {
            if (text != null && binding != null) {
                binding.tvEndDateValue.setText(text);
            }
        });

        viewModel.getChartSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null && binding != null) {
                binding.tvSummaryTitle.setText(summary.getTitle());
                binding.tvSummaryDescription.setText(summary.getDescription());
                binding.tvMetricTotal.setText(summary.getTotalMetric());
                binding.tvMetricAvg.setText(summary.getAvgMetric());
                binding.tvMetricMax.setText(summary.getMaxMetric());
            }
        });
    }

    private void setupFilterListeners() {
        binding.cardStartDate.setOnClickListener(v -> showStartDatePicker());
        binding.cardEndDate.setOnClickListener(v -> showEndDatePicker());
    }

    private void showStartDatePicker() {
        if (getContext() == null) return;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(viewModel.getStartDateMillis());

        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    viewModel.setStartDate(selected.getTimeInMillis());
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.setTitle(getString(R.string.start_date));
        dialog.show();
    }

    private void showEndDatePicker() {
        if (getContext() == null) return;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(viewModel.getEndDateMillis());

        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    viewModel.setEndDate(selected.getTimeInMillis());
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.setTitle(getString(R.string.end_date));
        dialog.show();
    }

    private void setupChartTypeChipListeners() {
        binding.chipChartBar.setOnClickListener(v -> viewModel.setChartType(ChartType.BAR));
        binding.chipChartHorizontalBar.setOnClickListener(v -> viewModel.setChartType(ChartType.HORIZONTAL_BAR));
        binding.chipChartLine.setOnClickListener(v -> viewModel.setChartType(ChartType.LINE));
        binding.chipChartPie.setOnClickListener(v -> viewModel.setChartType(ChartType.PIE));
        binding.chipChartScatter.setOnClickListener(v -> viewModel.setChartType(ChartType.SCATTER));
        binding.chipChartRadar.setOnClickListener(v -> viewModel.setChartType(ChartType.RADAR));
        binding.chipChartBubble.setOnClickListener(v -> viewModel.setChartType(ChartType.BUBBLE));
    }

    private void setFilterPillState(TextView view, boolean isSelected) {
        view.setBackgroundResource(isSelected ? R.drawable.bg_pill_selected : R.drawable.bg_pill_unselected);
        view.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#A0AEC0"));
    }

    private void updateChartTypeUI(ChartType type) {
        if (type == null) return;

        setFilterPillState(binding.chipChartBar, type == ChartType.BAR);
        setFilterPillState(binding.chipChartHorizontalBar, type == ChartType.HORIZONTAL_BAR);
        setFilterPillState(binding.chipChartLine, type == ChartType.LINE);
        setFilterPillState(binding.chipChartPie, type == ChartType.PIE);
        setFilterPillState(binding.chipChartScatter, type == ChartType.SCATTER);
        setFilterPillState(binding.chipChartRadar, type == ChartType.RADAR);
        setFilterPillState(binding.chipChartBubble, type == ChartType.BUBBLE);

        binding.chartBar.setVisibility(type == ChartType.BAR ? View.VISIBLE : View.GONE);
        binding.chartHorizontalBar.setVisibility(type == ChartType.HORIZONTAL_BAR ? View.VISIBLE : View.GONE);
        binding.chartLine.setVisibility(type == ChartType.LINE ? View.VISIBLE : View.GONE);
        binding.chartPie.setVisibility(type == ChartType.PIE ? View.VISIBLE : View.GONE);
        binding.chartScatter.setVisibility(type == ChartType.SCATTER ? View.VISIBLE : View.GONE);
        binding.chartRadar.setVisibility(type == ChartType.RADAR ? View.VISIBLE : View.GONE);
        binding.chartBubble.setVisibility(type == ChartType.BUBBLE ? View.VISIBLE : View.GONE);

        reloadActiveChartData();
    }

    private void reloadActiveChartData() {
        ChartType type = viewModel.getSelectedChartType().getValue();
        if (type == null || getContext() == null) return;
        long seed = viewModel.getSeedModifier();

        switch (type) {
            case BAR:
                binding.chartBar.setData(ChartDataGenerator.generateBarData(getContext(), seed));
                binding.chartBar.invalidate();
                binding.chartBar.animateY(800, Easing.EaseInOutQuad);
                break;
            case HORIZONTAL_BAR:
                binding.chartHorizontalBar.setData(ChartDataGenerator.generateHorizontalBarData(getContext(), seed));
                binding.chartHorizontalBar.invalidate();
                binding.chartHorizontalBar.animateY(800, Easing.EaseInOutQuad);
                break;
            case LINE:
                binding.chartLine.setData(ChartDataGenerator.generateLineData(getContext(), seed));
                binding.chartLine.invalidate();
                binding.chartLine.animateX(800, Easing.EaseInOutQuad);
                break;
            case PIE:
                binding.chartPie.setData(ChartDataGenerator.generatePieData(getContext(), seed));
                binding.chartPie.invalidate();
                binding.chartPie.animateY(900, Easing.EaseInOutQuad);
                break;
            case SCATTER:
                binding.chartScatter.setData(ChartDataGenerator.generateScatterData(getContext(), seed));
                binding.chartScatter.invalidate();
                binding.chartScatter.animateY(800, Easing.EaseInOutQuad);
                break;
            case RADAR:
                binding.chartRadar.setData(ChartDataGenerator.generateRadarData(getContext(), seed));
                binding.chartRadar.invalidate();
                binding.chartRadar.animateXY(800, 800, Easing.EaseInOutQuad);
                break;
            case BUBBLE:
                binding.chartBubble.setData(ChartDataGenerator.generateBubbleData(getContext(), seed));
                binding.chartBubble.invalidate();
                binding.chartBubble.animateY(800, Easing.EaseInOutQuad);
                break;
        }
    }

    private void initChartStyling() {
        int textColor = Color.parseColor("#A0AEC0");
        int gridColor = Color.parseColor("#263238");

        // 1. Bar Chart
        styleBaseChart(binding.chartBar);
        XAxis barX = binding.chartBar.getXAxis();
        barX.setPosition(XAxis.XAxisPosition.BOTTOM);
        barX.setTextColor(textColor);
        barX.setGridColor(gridColor);
        barX.setGranularity(1f);
        barX.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Mobile", "Frontend", "Backend", "AI/ML", "DevOps", "UI/UX"}));
        binding.chartBar.getAxisLeft().setTextColor(textColor);
        binding.chartBar.getAxisLeft().setGridColor(gridColor);
        binding.chartBar.getAxisRight().setEnabled(false);

        // 2. Horizontal Bar Chart
        styleBaseChart(binding.chartHorizontalBar);
        XAxis hX = binding.chartHorizontalBar.getXAxis();
        hX.setPosition(XAxis.XAxisPosition.BOTTOM);
        hX.setTextColor(textColor);
        hX.setGridColor(gridColor);
        hX.setGranularity(1f);
        hX.setValueFormatter(new IndexAxisValueFormatter(new String[]{"N.V. An", "L.T. Bình", "T.M. Cương", "P.H. Đăng", "H.V. Em"}));
        binding.chartHorizontalBar.getAxisLeft().setTextColor(textColor);
        binding.chartHorizontalBar.getAxisLeft().setGridColor(gridColor);
        binding.chartHorizontalBar.getAxisRight().setEnabled(false);

        // 3. Line Chart
        styleBaseChart(binding.chartLine);
        XAxis lineX = binding.chartLine.getXAxis();
        lineX.setPosition(XAxis.XAxisPosition.BOTTOM);
        lineX.setTextColor(textColor);
        lineX.setGridColor(gridColor);
        lineX.setValueFormatter(new IndexAxisValueFormatter(new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}));
        binding.chartLine.getAxisLeft().setTextColor(textColor);
        binding.chartLine.getAxisLeft().setGridColor(gridColor);
        binding.chartLine.getAxisRight().setEnabled(false);

        // 4. Pie Chart
        binding.chartPie.getDescription().setEnabled(false);
        binding.chartPie.setDrawHoleEnabled(true);
        binding.chartPie.setHoleColor(Color.TRANSPARENT);
        binding.chartPie.setTransparentCircleRadius(55f);
        binding.chartPie.setHoleRadius(45f);
        binding.chartPie.setCenterText("Học Viên");
        binding.chartPie.setCenterTextColor(Color.WHITE);
        binding.chartPie.setCenterTextSize(14f);
        binding.chartPie.setUsePercentValues(true);
        binding.chartPie.setEntryLabelColor(Color.WHITE);
        binding.chartPie.setEntryLabelTextSize(10f);
        configureLegend(binding.chartPie.getLegend(), textColor);

        // 5. Scatter Chart
        styleBaseChart(binding.chartScatter);
        XAxis scX = binding.chartScatter.getXAxis();
        scX.setPosition(XAxis.XAxisPosition.BOTTOM);
        scX.setTextColor(textColor);
        scX.setGridColor(gridColor);
        binding.chartScatter.getAxisLeft().setTextColor(textColor);
        binding.chartScatter.getAxisLeft().setGridColor(gridColor);
        binding.chartScatter.getAxisRight().setEnabled(false);

        // 6. Radar Chart
        binding.chartRadar.getDescription().setEnabled(false);
        binding.chartRadar.setWebColor(Color.parseColor("#37474F"));
        binding.chartRadar.setWebColorInner(Color.parseColor("#263238"));
        binding.chartRadar.setWebLineWidth(1.2f);
        binding.chartRadar.setWebLineWidthInner(0.8f);
        XAxis radarX = binding.chartRadar.getXAxis();
        radarX.setTextColor(Color.WHITE);
        radarX.setTextSize(10f);
        radarX.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Coding", "Logic", "Tiếng Anh", "Teamwork", "Problem", "Time"}));
        YAxis radarY = binding.chartRadar.getYAxis();
        radarY.setAxisMinimum(0f);
        radarY.setAxisMaximum(100f);
        radarY.setTextColor(textColor);
        configureLegend(binding.chartRadar.getLegend(), textColor);

        // 7. Bubble Chart
        styleBaseChart(binding.chartBubble);
        XAxis bX = binding.chartBubble.getXAxis();
        bX.setPosition(XAxis.XAxisPosition.BOTTOM);
        bX.setTextColor(textColor);
        bX.setGridColor(gridColor);
        binding.chartBubble.getAxisLeft().setTextColor(textColor);
        binding.chartBubble.getAxisLeft().setGridColor(gridColor);
        binding.chartBubble.getAxisRight().setEnabled(false);
    }

    private void styleBaseChart(com.github.mikephil.charting.charts.BarLineChartBase<?> chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(true);
        chart.setScaleEnabled(true);
        configureLegend(chart.getLegend(), Color.parseColor("#A0AEC0"));
    }

    private void configureLegend(Legend legend, int color) {
        legend.setTextColor(color);
        legend.setTextSize(11f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chart;
    }

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        binding.setLifecycleOwner(this);
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    protected void onThemeChanged(boolean isDark) {
        if (binding != null && getContext() != null) {
            initChartStyling();
            reloadActiveChartData();
        }
    }
}
