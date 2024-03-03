package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import dmax.dialog.SpotsDialog;


public class SplashScreen extends AppCompatActivity {

    static String CloudFunctionResponsee="";
    ButtonRectangle start,exit,addCoins;
    private AlertDialog progressDialogg;
    FirebaseUser firebaseUser;
    static String UserEmailPublic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressDialogg = new SpotsDialog(this, R.style.Custom);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        start = (ButtonRectangle) findViewById(R.id.start);
        exit = (ButtonRectangle) findViewById(R.id.exit);
        addCoins = (ButtonRectangle) findViewById(R.id.addCoins);

        addCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashScreen.this,addCoins.class);
                startActivity(intent);
//                FirebaseAuth.getInstance().signOut();
            }
        });

        addCoins.setVisibility(View.GONE);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialogg.show();
                        if (firebaseUser!=null){
                            try {
                                final URL CloudFunctionUrl = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/CheckUserExistsOrNot?text="+firebaseUser.getEmail().toString());
                                new AsyncCheckUserExistsOrNot().execute(CloudFunctionUrl);
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (CloudFunctionResponsee.equals("exists")){
                                            if (firebaseUser.getEmail().equals("hjavaid2014@namal.edu.pk")){
                                                UserEmailPublic = "hjavaid2014@namal.edu.pk";
                                                Intent intent = new Intent(SplashScreen.this,VolunteerMainActivity.class);
                                                intent.putExtra("UserEmail",firebaseUser.getEmail().toString());
                                                startActivity(intent);
                                                return;
                                            }
                                            else if (firebaseUser.getEmail().equals("qfix.boss@gmail.com")){
                                                UserEmailPublic = "qfix.boss@gmail.com";
                                                Intent intent = new Intent(SplashScreen.this,AdminMainActivity.class);
                                                intent.putExtra("UserEmail",firebaseUser.getEmail().toString());
                                                startActivity(intent);
                                                return;
                                            }
                                            else {
                                                UserEmailPublic = firebaseUser.getEmail();
                                                Intent intent = new Intent(SplashScreen.this,UserMainActivity.class);
                                                intent.putExtra("UserEmail",firebaseUser.getEmail().toString());
                                                startActivity(intent);
                                                return;
                                            }
                                        }
                                        else if (CloudFunctionResponsee.equals("notexists")){
                                            Intent intent = new Intent(SplashScreen.this,UserBasicInfo.class);
                                            intent.putExtra("UserEmail",firebaseUser.getEmail().toString());
                                            startActivity(intent);
                                            return;
                                        }
                                        else {
                                            Toast.makeText(SplashScreen.this,"Error, Something went wrong, maybe Internet Error",Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                    }
                                },7000);
                            }
                            catch (Exception e){}
                        }
                        else
                        {
                            Intent intent = new Intent(SplashScreen.this,login.class);
                            startActivity(intent);
                        }
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });
    }
}
class AsyncCheckUserExistsOrNot extends AsyncTask<URL,Void,Void> {
    public  String response;
    public  String saveResponse;

    @Override
    protected void onPostExecute(Void aVoid)
    {

    }

    @Override
    protected Void doInBackground(URL... urls) {
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
        SplashScreen.CloudFunctionResponsee = saveResponse;
        return null;
    }
}