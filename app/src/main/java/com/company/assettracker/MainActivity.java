package com.company.assettracker;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.company.assettracker.databinding.ActivityMainBinding;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;
    CodeScanner codeScanner;
    private boolean isSuccess = false;
    String[] permissions = {CAMERA};
    int PERMISSION_REQUEST_CODE = 100;
//    private String admin = "ritesh.mishra@nxp.com";
    private String admin = "rishabh9807mishra@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        codeScanner = new CodeScanner(this, activityMainBinding.csvScanner);
        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            Log.i("QR_RESULT",result.getText());
            isSuccess = true;
            showDetails(result.getText());

        }));
        activityMainBinding.csvScanner.setOnClickListener(view -> codeScanner.startPreview());
        activityMainBinding.ftbSetting.setOnClickListener(view -> changeEmail());
    }

    private void showDetails(String details) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.popup_details, viewGroup, false);
        TextView tvDetails = dialogView.findViewById(R.id.tv_details);
        tvDetails.setText(details);
        RadioGroup rgType = dialogView.findViewById(R.id.rg_type);
        RadioButton rbPlace = dialogView.findViewById(R.id.rb_place);
        RadioButton rbUser = dialogView.findViewById(R.id.rb_user);
        EditText etUserPlace = dialogView.findViewById(R.id.et_user_place);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSend = dialogView.findViewById(R.id.btn_send);
        AtomicReference<String> message = new AtomicReference<>("Enter place name");
        rgType.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.rb_place:
                    etUserPlace.setHint("Enter place name");
                    message.set("Enter place name");
                    break;

                case R.id.rb_user:
                    etUserPlace.setHint("Enter user name");
                    message.set("Enter user name");
                    break;
            }
        });
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        btnCancel.setOnClickListener(view -> alertDialog.dismiss());
        btnSend.setOnClickListener(view -> {
            if(etUserPlace.getText().toString().length() <= 0) {
                Toast.makeText(this, message.get(), Toast.LENGTH_SHORT).show();
                return;
            }
            if(rbPlace.isChecked()) {
                sendEmail("Asset moved to place: " + etUserPlace.getText().toString(), details);
                alertDialog.dismiss();
            } else {
                sendEmail("Asset assigned to user: " + etUserPlace.getText().toString(), details);
                alertDialog.dismiss();
            }

        });
        alertDialog.show();
    }

    private void changeEmail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.change_email, viewGroup, false);
        EditText etEmail = dialogView.findViewById(R.id.et_email);
        etEmail.setText(admin);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        btnCancel.setOnClickListener(view -> alertDialog.dismiss());
        btnSubmit.setOnClickListener(view -> {
            if(validate(etEmail.getText().toString())) {
                admin = etEmail.getText().toString();
                alertDialog.dismiss();
            } else {
                Toast.makeText(this, "Enter NXP email id", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@nxp.com$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    private void sendEmail(String s, String data) {
        SendMail sm = new SendMail(this, admin, s, data);
        sm.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AndroidPermissionCheck.hasPermission(this, permissions)) {
            codeScanner.startPreview();
        } else {
            AndroidPermissionCheck.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }
}