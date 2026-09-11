package com.base.android.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spanned;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.LeadingMarginSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.Px;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom LeadingMarginSpan tạo bullet list với hiệu ứng Hanging Indent:
 * - Dấu chấm tròn (bullet) có kích thước (radius) và khoảng cách (gap) tùy chỉnh được.
 * - Text sau bullet có khoảng cách cố định (không dùng space string " ").
 * - Khi text xuống dòng, các dòng tiếp theo tự động thụt lề bằng đúng lề của dòng 1,
 *   thẳng hàng với chữ đầu tiên (hanging indent).
 */
public class HangingBulletSpan implements LeadingMarginSpan {

    private final int bulletRadius;
    private final int gapWidth;
    private final int bulletMarginStart;
    private final Integer bulletColor;

    public HangingBulletSpan(@Px int bulletRadius, @Px int gapWidth) {
        this(bulletRadius, gapWidth, 0, null);
    }

    public HangingBulletSpan(@Px int bulletRadius, @Px int gapWidth, @ColorInt Integer bulletColor) {
        this(bulletRadius, gapWidth, 0, bulletColor);
    }

    public HangingBulletSpan(@Px int bulletRadius, @Px int gapWidth, @Px int bulletMarginStart, @ColorInt Integer bulletColor) {
        this.bulletRadius = bulletRadius;
        this.gapWidth = gapWidth;
        this.bulletMarginStart = bulletMarginStart;
        this.bulletColor = bulletColor;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        // Cả dòng đầu (first = true) và dòng xuống hàng (first = false) đều nhận cùng độ rộng margin.
        // Nhờ vậy, dòng tiếp theo khi wrap sẽ bắt đầu thẳng hàng với chữ đầu tiên của dòng 1.
        return bulletMarginStart + (bulletRadius * 2) + gapWidth;
    }

    @Override
    public void drawLeadingMargin(Canvas canvas, Paint paint, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout layout) {
        // Chỉ vẽ bullet ở dòng đầu tiên của đoạn văn bản
        if (first && (text instanceof Spanned) && ((Spanned) text).getSpanStart(this) == start) {
            Paint.Style oldStyle = paint.getStyle();
            int oldColor = paint.getColor();

            if (bulletColor != null) {
                paint.setColor(bulletColor);
            }
            paint.setStyle(Paint.Style.FILL);

            // Tọa độ tâm X của bullet
            float xCenter = x + dir * (bulletMarginStart + bulletRadius);

            // Tọa độ tâm Y của bullet canh giữa theo chiều cao nét chữ (font metrics)
            float yCenter = baseline + (paint.ascent() + paint.descent()) / 2.0f;

            canvas.drawCircle(xCenter, yCenter, bulletRadius, paint);

            paint.setColor(oldColor);
            paint.setStyle(oldStyle);
        }
    }

    /**
     * Tiện ích định dạng chuỗi nhiều dòng thành SpannableStringBuilder có bullet list và hanging indent.
     */
    public static SpannableStringBuilder formatBulletList(
            String rawText,
            @Px int bulletRadius,
            @Px int gapWidth,
            @Px int bulletMarginStart,
            @ColorInt Integer bulletColor) {
        if (TextUtils.isEmpty(rawText)) {
            return new SpannableStringBuilder();
        }

        String[] rawLines = rawText.split("\r?\n");
        List<String> lines = new ArrayList<>();
        for (String rawLine : rawLines) {
            String trimmed = rawLine.trim();
            if (!trimmed.isEmpty()) {
                // Loại bỏ ký tự bullet nếu chuỗi đã có sẵn (-, •, *)
                if (trimmed.startsWith("•") || trimmed.startsWith("-") || trimmed.startsWith("*")) {
                    trimmed = trimmed.substring(1).trim();
                }
                if (!trimmed.isEmpty()) {
                    lines.add(trimmed);
                }
            }
        }

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            int start = ssb.length();
            ssb.append(lines.get(i));
            int end = ssb.length();

            ssb.setSpan(
                    new HangingBulletSpan(bulletRadius, gapWidth, bulletMarginStart, bulletColor),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            if (i < lines.size() - 1) {
                ssb.append("\n");
            }
        }

        return ssb;
    }
}
