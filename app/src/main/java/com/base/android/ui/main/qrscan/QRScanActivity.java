package com.base.android.ui.main.qrscan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.base.android.BR;
import com.base.android.R;
import com.base.android.databinding.ActivityQrScanBinding;
import com.base.android.di.component.ActivityComponent;
import com.base.android.ui.base.activity.BaseActivity;
import com.base.android.utils.ImagePickerUtils;
import com.base.android.utils.PermissionUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class QRScanActivity extends BaseActivity<ActivityQrScanBinding, QRScanViewModel> {

    public static final String EXTRA_MODE      = "EXTRA_MODE";
    public static final String MODE_QR_LOGIN   = "MODE_QR_LOGIN";
    public static final String EXTRA_QR_RESULT = "EXTRA_QR_RESULT";

    private static final int REQUEST_CAMERA_PERMISSION  = 1001;

    private boolean isCameraActive = false;

    // Image picker utility (mặc định không crop ảnh)
    private final ImagePickerUtils imagePickerUtils = new ImagePickerUtils(this, new ImagePickerUtils.ImagePickerCallback() {
        @Override
        public void onImagePicked(Uri uri) {
            if (uri != null) {
                decodeQrFromGallery(uri);
            }
        }

        @Override
        public void onError(String errorMessage) {
            viewModel.showErrorMessage(errorMessage);
        }
    });

    // Camera continuous scan callback
    private final BarcodeCallback barcodeCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result == null || result.getText() == null) return;
            // Pause scanning to avoid repeated callbacks
            viewBinding.barcodeScanner.pause();
            isCameraActive = false;
            showResult(result.getText());
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        requestCameraIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isCameraActive && PermissionUtils.hasCameraPermission(this)) {
            startCameraScanning();
        } else if (isCameraActive) {
            viewBinding.barcodeScanner.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewBinding.barcodeScanner.pause();
    }

    // Camera permission & start
    private void requestCameraIfNeeded() {
        if (PermissionUtils.hasCameraPermission(this)) {
            startCameraScanning();
        } else {
            if (PermissionUtils.isPermanentlyDenied(this, Manifest.permission.CAMERA)) {
                PermissionUtils.showPermissionSettingsDialog(this, getString(R.string.permission_camera_rationale));
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        }
    }

    private void startCameraScanning() {
        isCameraActive = true;
        viewBinding.barcodeScanner.decodeContinuous(barcodeCallback);
        viewBinding.barcodeScanner.resume();
    }

    // Binding callbacks (called from XML via DataBinding)
    public void onBackClick() {
        finish();
    }

    public void onScanAgainClick() {
        viewModel.clearResult();
        viewBinding.cardResult.setVisibility(View.GONE);
        viewBinding.tvResult.setText("");
        startCameraScanning();
    }

    public void onPickImageClick() {
        imagePickerUtils.checkStoragePermissionAndOpen();
    }

    // Decode QR from gallery image
    private void decodeQrFromGallery(Uri uri) {
        try {
            // Load full-resolution bitmap:
            // - API 28+: ImageDecoder (always full-res, supports hardware bitmaps)
            // - API < 28: BitmapFactory.decodeStream via ContentResolver
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source src = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(src, (decoder, info, s) ->
                        decoder.setMutableRequired(true));
            } else {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    showNoQrFound();
                    return;
                }
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            }

            if (bitmap == null) {
                showNoQrFound();
                return;
            }

            // ZXing requires ARGB_8888 config to read pixel data correctly
            if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            }

            int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            LuminanceSource source = new RGBLuminanceSource(
                    bitmap.getWidth(), bitmap.getHeight(), pixels);

            // Try to decode with both binarizers for best coverage
            String decoded = tryDecode(source);
            if (decoded == null) {
                // Fallback: invert the image (helps with white-on-black QR codes)
                decoded = tryDecode(source.invert());
            }

            if (decoded != null) {
                viewBinding.barcodeScanner.pause();
                isCameraActive = false;
                showResult(decoded);
            } else {
                showNoQrFound();
            }
        } catch (IOException e) {
            Timber.e(e, "Error reading image from gallery");
            viewModel.showErrorMessage(getString(R.string.qr_scan_error_read_image));
        } catch (Exception e) {
            Timber.e(e, "Unexpected error decoding QR from gallery");
            showNoQrFound();
        }
    }

    /**
     * Attempts to decode a QR/barcode from the given LuminanceSource.
     * Tries HybridBinarizer first (better for photos), then falls back to
     * GlobalHistogramBinarizer (better for low-contrast / small images).
     *
     * @return decoded text, or null if nothing was found
     */
    private String tryDecode(LuminanceSource source) {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
                BarcodeFormat.QR_CODE,
                BarcodeFormat.DATA_MATRIX,
                BarcodeFormat.AZTEC,
                BarcodeFormat.PDF_417));

        // 1st attempt: HybridBinarizer
        try {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);
            if (result != null && result.getText() != null) return result.getText();
        } catch (NotFoundException ignored) { }

        // 2nd attempt: GlobalHistogramBinarizer (better for low-contrast images)
        try {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);
            if (result != null && result.getText() != null) return result.getText();
        } catch (NotFoundException ignored) { }

        return null;
    }

    // Show result / no result
    private void showResult(String text) {
        viewModel.setQrResult(text);
        if (MODE_QR_LOGIN.equals(getIntent().getStringExtra(EXTRA_MODE))) {
            // QR-login mode: return decoded text to LoginActivity and close
            Intent result = new Intent();
            result.putExtra(EXTRA_QR_RESULT, text);
            setResult(RESULT_OK, result);
            finish();
        } else {
            // Normal mode: show result card in-screen
            viewBinding.tvResult.setText(text);
            viewBinding.cardResult.setVisibility(View.VISIBLE);
        }
    }

    private void showNoQrFound() {
        viewModel.showErrorMessage(getString(R.string.qr_scan_no_result));
    }

    // Permission result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0) return;

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraScanning();
            } else {
                if (PermissionUtils.isPermanentlyDenied(this, Manifest.permission.CAMERA)) {
                    PermissionUtils.showPermissionSettingsDialog(this, getString(R.string.permission_camera_rationale));
                } else {
                    viewModel.showErrorMessage(getString(R.string.permission_camera_required));
                    finish();
                }
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_qr_scan;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }
}
