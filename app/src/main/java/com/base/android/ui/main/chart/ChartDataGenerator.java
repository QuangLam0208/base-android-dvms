package com.base.android.ui.main.chart;

import android.content.Context;
import android.graphics.Color;

import com.base.android.R;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.github.mikephil.charting.utils.EntryXComparator;

public class ChartDataGenerator {

    public static final int[] MODERN_COLORS = new int[]{
            Color.rgb(41, 128, 185),   // Blue
            Color.rgb(39, 174, 96),    // Green
            Color.rgb(230, 126, 34),   // Orange
            Color.rgb(155, 89, 182),   // Purple
            Color.rgb(231, 76, 60),    // Red
            Color.rgb(26, 188, 156),   // Teal
            Color.rgb(241, 196, 15)    // Yellow
    };

    public static BarData generateBarData(Context context, long seedModifier) {
        Random random = new Random(seedModifier);
        List<BarEntry> entries = new ArrayList<>();
        int base = 80 + (int) (Math.abs(seedModifier % 40));

        entries.add(new BarEntry(0f, base + random.nextInt(60))); // Mobile
        entries.add(new BarEntry(1f, base + random.nextInt(90))); // Frontend
        entries.add(new BarEntry(2f, base + random.nextInt(80))); // Backend
        entries.add(new BarEntry(3f, base + 40 + random.nextInt(70))); // AI & ML
        entries.add(new BarEntry(4f, base - 20 + random.nextInt(50))); // DevOps
        entries.add(new BarEntry(5f, base + 10 + random.nextInt(40))); // UI/UX

        BarDataSet set = new BarDataSet(entries, context.getString(R.string.chart_title_bar));
        set.setColors(MODERN_COLORS);
        set.setValueTextSize(11f);
        set.setValueTextColor(Color.DKGRAY);

        BarData data = new BarData(set);
        data.setBarWidth(0.6f);
        return data;
    }

    public static BarData generateHorizontalBarData(Context context, long seedModifier) {
        Random random = new Random(seedModifier + 11);
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            float rating = 4.0f + (random.nextInt(10) / 10.0f);
            if (rating > 5.0f) rating = 5.0f;
            entries.add(new BarEntry(i, rating));
        }

        BarDataSet set = new BarDataSet(entries, context.getString(R.string.chart_title_horizontal_bar));
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(52, 152, 219));
        colors.add(Color.rgb(46, 204, 113));
        colors.add(Color.rgb(241, 196, 15));
        colors.add(Color.rgb(230, 126, 34));
        colors.add(Color.rgb(155, 89, 182));
        set.setColors(colors);
        set.setValueTextSize(11f);
        set.setValueTextColor(Color.DKGRAY);

        BarData data = new BarData(set);
        data.setBarWidth(0.55f);
        return data;
    }

    public static LineData generateLineData(Context context, long seedModifier) {
        Random random = new Random(seedModifier + 22);
        List<Entry> values = new ArrayList<>();

        float current = 15f + (float) (Math.abs(seedModifier % 10));
        for (int i = 0; i < 7; i++) {
            current += (random.nextFloat() * 6f - 2.5f);
            if (current < 5f) current = 5f;
            values.add(new Entry(i, (float) Math.round(current * 10) / 10f));
        }

        LineDataSet set = new LineDataSet(values, context.getString(R.string.chart_title_line));
        set.setColor(Color.rgb(41, 128, 185));
        set.setCircleColor(Color.rgb(41, 128, 185));
        set.setLineWidth(2.5f);
        set.setCircleRadius(5f);
        set.setDrawCircleHole(true);
        set.setCircleHoleRadius(2.5f);
        set.setValueTextSize(10f);
        set.setDrawFilled(true);
        set.setFillColor(Color.rgb(187, 222, 251));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        return new LineData(set);
    }

    public static PieData generatePieData(Context context, long seedModifier) {
        Random random = new Random(seedModifier + 33);
        List<PieEntry> entries = new ArrayList<>();

        int it = 35 + random.nextInt(10);
        int business = 20 + random.nextInt(8);
        int design = 15 + random.nextInt(5);
        int language = 15 + random.nextInt(5);
        int other = 100 - (it + business + design + language);
        if (other <= 0) other = 5;

        entries.add(new PieEntry(it, "CNTT (IT)"));
        entries.add(new PieEntry(business, "Kinh tế"));
        entries.add(new PieEntry(design, "Thiết kế"));
        entries.add(new PieEntry(language, "Ngoại ngữ"));
        entries.add(new PieEntry(other, "Khác"));

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(MODERN_COLORS);
        set.setSliceSpace(3f);
        set.setSelectionShift(7f);
        set.setValueTextSize(12f);
        set.setValueTextColor(Color.WHITE);

        return new PieData(set);
    }

    public static ScatterData generateScatterData(Context context, long seedModifier) {
        Random random = new Random(seedModifier + 44);
        List<Entry> values = new ArrayList<>();

        // 18 data points representing (Study hours, Final Score)
        for (int i = 0; i < 18; i++) {
            float hours = 5f + random.nextFloat() * 35f; // 5h - 40h
            float score = 45f + (hours * 1.2f) + (random.nextFloat() * 15f - 7.5f);
            if (score > 100f) score = 99.5f;
            if (score < 40f) score = 42f;
            values.add(new Entry((float) Math.round(hours * 10) / 10f, (float) Math.round(score * 10) / 10f));
        }

        // MPAndroidChart requires all BarLineChartBase entries to be sorted by X ascending!
        Collections.sort(values, new EntryXComparator());

        ScatterDataSet set = new ScatterDataSet(values, context.getString(R.string.chart_title_scatter));
        set.setScatterShape(com.github.mikephil.charting.charts.ScatterChart.ScatterShape.CIRCLE);
        set.setScatterShapeSize(16f);
        set.setColor(Color.rgb(231, 76, 60));
        set.setValueTextSize(9f);
        set.setDrawValues(false);

        return new ScatterData(set);
    }

    public static RadarData generateRadarData(Context context, long seedModifier) {
        Random random = new Random(seedModifier + 55);
        List<RadarEntry> studentValues = new ArrayList<>();
        List<RadarEntry> avgValues = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            studentValues.add(new RadarEntry(65f + random.nextInt(32)));
            avgValues.add(new RadarEntry(60f + random.nextInt(20)));
        }

        RadarDataSet set1 = new RadarDataSet(studentValues, "Học viên (Cá nhân)");
        set1.setColor(Color.rgb(41, 128, 185));
        set1.setFillColor(Color.rgb(52, 152, 219));
        set1.setDrawFilled(true);
        set1.setFillAlpha(130);
        set1.setLineWidth(2f);
        set1.setDrawHighlightCircleEnabled(true);
        set1.setDrawHighlightIndicators(false);

        RadarDataSet set2 = new RadarDataSet(avgValues, "Trung bình lớp");
        set2.setColor(Color.rgb(230, 126, 34));
        set2.setFillColor(Color.rgb(243, 156, 18));
        set2.setDrawFilled(true);
        set2.setFillAlpha(90);
        set2.setLineWidth(1.8f);

        RadarData data = new RadarData();
        data.addDataSet(set1);
        data.addDataSet(set2);
        data.setValueTextSize(9f);
        data.setDrawValues(false);

        return data;
    }

    public static BubbleData generateBubbleData(Context context, long seedModifier) {
        Random random = new Random(seedModifier + 66);
        List<BubbleEntry> entries = new ArrayList<>();

        // X: Course duration (10 - 50 hours), Y: Rating (3.5 - 5.0), Size: Number of students (20 - 150)
        for (int i = 0; i < 7; i++) {
            float hours = 10f + i * 6f + (random.nextFloat() * 4f);
            float rating = 3.8f + random.nextFloat() * 1.1f;
            if (rating > 5.0f) rating = 4.95f;
            float students = 30f + random.nextFloat() * 120f;
            entries.add(new BubbleEntry(hours, rating, students));
        }

        // Ensure entries are strictly sorted by X ascending
        Collections.sort(entries, new EntryXComparator());

        BubbleDataSet set = new BubbleDataSet(entries, context.getString(R.string.chart_title_bubble));
        set.setColors(MODERN_COLORS);
        set.setValueTextSize(9f);
        set.setNormalizeSizeEnabled(true);

        return new BubbleData(set);
    }

    public static ChartSummaryData generateSummary(Context context, ChartType type, long seedModifier) {
        Random random = new Random(seedModifier);
        switch (type) {
            case BAR:
                int totalStudents = 620 + (int) (seedModifier % 120);
                return new ChartSummaryData(
                        context.getString(R.string.chart_title_bar),
                        "So sánh lượng học viên tham gia giữa 6 nhóm môn học chính.",
                        String.format(context.getString(R.string.chart_metric_total), totalStudents + " học viên"),
                        String.format(context.getString(R.string.chart_metric_avg), (totalStudents / 6) + " / môn"),
                        String.format(context.getString(R.string.chart_metric_max), "AI & Machine Learning")
                );
            case HORIZONTAL_BAR:
                return new ChartSummaryData(
                        context.getString(R.string.chart_title_horizontal_bar),
                        "Điểm đánh giá chất lượng giảng dạy trung bình từ học viên (thang điểm 5.0).",
                        String.format(context.getString(R.string.chart_metric_total), "5 Giảng viên tiêu biểu"),
                        String.format(context.getString(R.string.chart_metric_avg), "4.82 / 5.0 ⭐"),
                        String.format(context.getString(R.string.chart_metric_max), "Nguyễn Văn An (4.95 ⭐)")
                );
            case LINE:
                float totalHours = 145f + (seedModifier % 30);
                return new ChartSummaryData(
                        context.getString(R.string.chart_title_line),
                        "Biến động thời lượng học tập trực tuyến trong mốc thời gian được lọc.",
                        String.format(context.getString(R.string.chart_metric_total), (int) totalHours + " giờ học"),
                        String.format(context.getString(R.string.chart_metric_avg), String.format("%.1f giờ/ngày", totalHours / 7)),
                        String.format(context.getString(R.string.chart_metric_max), "26.5 giờ (Chủ nhật)")
                );
            case PIE:
                return new ChartSummaryData(
                        context.getString(R.string.chart_title_pie),
                        "Tỷ lệ cơ cấu chuyên ngành đào tạo của toàn bộ học viên theo học.",
                        String.format(context.getString(R.string.chart_metric_total), "100%"),
                        String.format(context.getString(R.string.chart_metric_avg), "5 Chuyên ngành"),
                        String.format(context.getString(R.string.chart_metric_max), "CNTT (chiếm ~38%)")
                );
            case SCATTER:
                return new ChartSummaryData(
                        context.getString(R.string.chart_title_scatter),
                        "Mối tương quan tuyến tính giữa số giờ tự học mỗi tuần và kết quả bài thi cuối khóa.",
                        String.format(context.getString(R.string.chart_metric_total), "18 Mẫu khảo sát"),
                        String.format(context.getString(R.string.chart_metric_avg), "76.4 điểm"),
                        String.format(context.getString(R.string.chart_metric_max), "98.5 điểm (36h tự học)")
                );
            case RADAR:
                return new ChartSummaryData(
                        context.getString(R.string.chart_title_radar),
                        "Đánh giá toàn diện 6 năng lực trọng tâm so với mức chuẩn chung.",
                        String.format(context.getString(R.string.chart_metric_total), "6 Năng lực"),
                        String.format(context.getString(R.string.chart_metric_avg), "78.5 / 100 điểm"),
                        String.format(context.getString(R.string.chart_metric_max), "Tư duy Logic (92 điểm)")
                );
            case BUBBLE:
            default:
                return new ChartSummaryData(
                        context.getString(R.string.chart_title_bubble),
                        "Tương quan 3 chiều: Trục X (Thời lượng khóa), Trục Y (Độ hài lòng), Kích thước (Số lượng học viên).",
                        String.format(context.getString(R.string.chart_metric_total), "7 Khóa học phân tích"),
                        String.format(context.getString(R.string.chart_metric_avg), "4.6 ⭐ hài lòng"),
                        String.format(context.getString(R.string.chart_metric_max), "Khóa Mobile Flutter (145 HV)")
                );
        }
    }
}
