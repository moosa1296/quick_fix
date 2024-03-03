package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import dmax.dialog.SpotsDialog;

public class ForgetPassword extends AppCompatActivity {

    private EditText emailForgetPassword;
    private ButtonRectangle resetbtn;

    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        emailForgetPassword = (EditText) findViewById(R.id.emailForgetPassword);
        resetbtn = (ButtonRectangle) findViewById(R.id.resetBtn);

        firebaseAuth = FirebaseAuth.getInstance();

        final AlertDialog progressDialog = new SpotsDialog(ForgetPassword.this,R.style.Custom);

        resetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailForgetPassword.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(ForgetPassword.this, "Enter your Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.show();
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(ForgetPassword.this, "Password reset Email sent", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            Intent intent = new Intent(ForgetPassword.this,login.class);
                            startActivity(intent);
                            return;
                        }else {
                            Toast.makeText(ForgetPassword.this, "Failed to send reset Email", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }
}
