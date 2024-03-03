package com.example.hp.quickfixx;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.LoginFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.quickfixx.R;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;


public class login extends AppCompatActivity {

    private EditText emailLogin,passwordLogin;
    private ButtonRectangle loginBtn;
    private ButtonRectangle createAccountBtn;
    private TextView forgetPassword;
    static String CloudFunctionResponse = null;
    private static Context mContext;
    static String email,password;

    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;

    FirebaseAuth auth;
    static AlertDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;

        //initializing different fields
        emailLogin = (EditText) findViewById(R.id.emailLogin);
        passwordLogin = (EditText) findViewById(R.id.passwordLogin);
        loginBtn = (ButtonRectangle) findViewById(R.id.loginBtn);
        createAccountBtn = (ButtonRectangle) findViewById(R.id.createAccountBtn);
        forgetPassword = (TextView) findViewById(R.id.forgetPassword);

        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("users");
        auth = FirebaseAuth.getInstance();

        progressDialog = new SpotsDialog(login.this,R.style.Custom);

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(login.this,registration.class);
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailLogin.onEditorAction(EditorInfo.IME_ACTION_DONE);
                passwordLogin.onEditorAction(EditorInfo.IME_ACTION_DONE);
                progressDialog.show();
                loginUser();
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(login.this,ForgetPassword.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        email = emailLogin.getText().toString().trim();
        password = passwordLogin.getText().toString().trim();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        try {
                            URL CloudFunctionUrl = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/CheckUserExistsOrNot?text="+email);
                            new AsyncCheckBasicInfo().execute(CloudFunctionUrl);
                        }
                        catch (Exception e){}
                    } else {
                        Toast.makeText(login.this, "User login unsuccessful", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
        else{
            progressDialog.dismiss();
            Toast.makeText(login.this,"Please fill in both fields",Toast.LENGTH_SHORT).show();
        }
    }

    public static void MoveToNextAcitivity() {
        if(CloudFunctionResponse.equals("exists")){
            progressDialog.dismiss();
            if (email.equals("hjavaid2014@namal.edu.pk")){
                SplashScreen.UserEmailPublic = "hjavaid2014@namal.edu.pk";
                String token_id = FirebaseInstanceId.getInstance().getToken();
                String current_user = email.replace(".",",");

                Map<String,Object> map = new HashMap<String, Object>();
                map.put("token_id",token_id);

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                firebaseDatabase.getReference("users").child(current_user).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mContext, "User logged in successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext,VolunteerMainActivity.class);
                        intent.putExtra("UserEmail",email);
                        mContext.startActivity(intent);
                    }
                });
            }
            else if (email.equals("qfix.boss@gmail.com")){
                SplashScreen.UserEmailPublic = email;
                Intent intent = new Intent(mContext,AdminMainActivity.class);
                intent.putExtra("UserEmail",email);
                mContext.startActivity(intent);
                return;
            }
            else{
                SplashScreen.UserEmailPublic = email;
                String token_id = FirebaseInstanceId.getInstance().getToken();
                String current_user = email.replace(".",",");

                Map<String,Object> map = new HashMap<String, Object>();
                map.put("token_id",token_id);

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                firebaseDatabase.getReference("users").child(current_user).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mContext, "User logged in successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext,UserMainActivity.class);
                        intent.putExtra("UserEmail",email);
                        mContext.startActivity(intent);
                    }
                });
            }
        }
        else{
            Intent intent = new Intent(mContext,UserBasicInfo.class);
            intent.putExtra("UserEmail",email);
            mContext.startActivity(intent);
        }
    }
}

class AsyncCheckBasicInfo extends AsyncTask<URL,Void,Void> {
    public  String response;
    public  String saveResponse;

    @Override
    protected void onPostExecute(Void aVoid)
    {
     login.MoveToNextAcitivity();
    }

    @Override
    protected Void doInBackground(URL... urls) {
        Log.i("back","back");
        try{
            URLConnection connection = urls[0].openConnection();
            connection.setConnectTimeout(5000);
            connection.getContentLength();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            while ((response = in.readLine()) != null)
            {
                saveResponse = response;
            }

            in.close();

        }
        catch (SocketTimeoutException s){
            Log.i("Socket","Connection request timeout");
        }
        catch (Exception e){
        }
        login.CloudFunctionResponse = saveResponse;
        return null;
    }
}