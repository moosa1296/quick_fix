package com.example.hp.quickfixx;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.gc.materialdesign.views.ButtonRectangle;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.net.URL;

public class addCoins extends AppCompatActivity {

    EditText emailUser,coinsAdd;
    ButtonRectangle addCoins;

    FirebaseDatabase firebaseDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_coins);

        emailUser = (EditText) findViewById(R.id.emailUser);
        coinsAdd = (EditText) findViewById(R.id.coinsAdd);
        addCoins = (ButtonRectangle) findViewById(R.id.addBtn);

        addCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject jsonObject = new JSONObject();
                try{
                    jsonObject.put("user",emailUser.getText().toString().trim().replace(".",","));
                    jsonObject.put("coins",coinsAdd.getText().toString().trim());

                    URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/InsertCoins?text="+jsonObject.toString().trim());
                    new AsyncInsertJobInfo().execute(url);
                }catch (Exception e){}
            }
        });
    }
}
