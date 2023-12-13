package com.isi.passwordgame.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.isi.passwordgame.R;
import com.isi.passwordgame.databinding.ActivityStartGameBinding;
import com.isi.passwordgame.qr.QRService;

public class StartGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        var binding = ActivityStartGameBinding.inflate(getLayoutInflater());
        var view = binding.getRoot();
        setContentView(view);
        var qrCode = binding.qrCode;

        qrCode.setImageBitmap(QRService.Companion.generateQRCode("Hello", 700, 700));
    }
}