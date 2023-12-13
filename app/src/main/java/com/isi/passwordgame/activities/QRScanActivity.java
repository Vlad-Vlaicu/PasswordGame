package com.isi.passwordgame.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.isi.passwordgame.R;

public class QRScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start the QR code scanner
        startQRCodeScanner();
    }

    private void startQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(true);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setBeepEnabled(false);
        // Customize the prompt message if needed
        integrator.setPrompt("Scan the Join Game Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                // Handle case where scanning was canceled
                setResult(RESULT_CANCELED);
            } else {
                // Handle scanned data
                String scannedData = result.getContents();
                Intent intent = new Intent();
                intent.putExtra("SCANNED_DATA", scannedData);
                setResult(RESULT_OK, intent);
            }
            finish(); // Close the ScannerActivity
        }
    }
}