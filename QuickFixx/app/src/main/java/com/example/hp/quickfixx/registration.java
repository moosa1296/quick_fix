package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hp.quickfixx.R;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.net.URL;

import dmax.dialog.SpotsDialog;

public class registration extends AppCompatActivity {

    private EditText registerEmail,registerPassword,registerPasswordAgain;
    private ButtonRectangle createBtn;
    FirebaseAuth auth;
    Intent intent;
    AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //initializing different fields
        registerEmail = (EditText) findViewById(R.id.registerEmail);
        registerPassword = (EditText) findViewById(R.id.registerPassword);
        registerPasswordAgain = (EditText) findViewById(R.id.registerPasswordAgain);
        createBtn = (ButtonRectangle) findViewById(R.id.createBtn);

        //intializing Firebase Authentication
        auth = FirebaseAuth.getInstance();

        //intializing progress Dialog
        progressDialog = new SpotsDialog(registration.this,R.style.Custom);

        //Intent for moving to the login screen
        intent = new Intent(registration.this,login.class);

        //create Button actionListener
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();

                createUserAccount();
            }
        });

    }

    private void createUserAccount() {
        final String email,password,passwordAgain;
        email = registerEmail.getText().toString().trim();
        password = registerPassword.getText().toString().trim();
        passwordAgain = registerPasswordAgain.getText().toString().trim();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordAgain)){
            if (password.equals(passwordAgain)){
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){
                        Toast.makeText(registration.this,"Account successfully created",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                        JSONObject jsonObject = new JSONObject();
                        try{
                            jsonObject.put("user",email.replace(".",","));
                            jsonObject.put("coins","0");

                            URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/InsertCoins?text="+jsonObject.toString().trim());
                            new AsyncInsertJobInfo().execute(url);
                        }catch (Exception e){}

                        Intent intent = new Intent(registration.this,login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(registration.this,"Account creation failed",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
            }
            else {
                Toast.makeText(registration.this,"Password not matched",Toast.LENGTH_SHORT).show();
                registerPassword.setText("");
                registerPasswordAgain.setText("");
                progressDialog.dismiss();
            }
        }
        else{
            Toast.makeText(registration.this,"Please fill both fields", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }
}
