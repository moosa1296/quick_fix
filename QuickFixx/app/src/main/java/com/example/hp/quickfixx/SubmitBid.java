package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.os.Handler;
import android.widget.Toast;

import dmax.dialog.SpotsDialog;

public class SubmitBid extends AppCompatActivity {

    TextView titleTextView;
    ButtonRectangle submitBtn;
    EditText bidDescriptionEditText,bidCoinsEditText;
    String JobTitle;
    String JobId;
    String UserEmail;
    String millisInString;
    String totalBids;
    static String CloudFunctionResponse = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_bid);

        titleTextView = (TextView) findViewById(R.id.titleTextview);
        submitBtn = (ButtonRectangle) findViewById(R.id.submitBtn);
        bidDescriptionEditText = (EditText) findViewById(R.id.bidDescriptionEditText);
        bidCoinsEditText = (EditText) findViewById(R.id.bidCoinsEditText);

        Intent intent = getIntent();
        JobTitle = intent.getStringExtra("JobTitle");
        JobId = intent.getStringExtra("JobId");
        UserEmail = intent.getStringExtra("UserEmail");
        totalBids = intent.getStringExtra("totalBids");


        titleTextView.setText(JobTitle);
        final AlertDialog progressDialog = new SpotsDialog(SubmitBid.this,R.style.Custom);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                String data = makeJSONobject().toString();
                try{
                    URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/InsertNewBid?text="+data.trim());
                    Log.i("Url",url.toString());
                    new SubmitBidAsync().execute(url);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                          if (CloudFunctionResponse.equals("ok")){
                              progressDialog.dismiss();
                              Toast.makeText(SubmitBid.this,"Bid submitted",Toast.LENGTH_SHORT).show();
                              Intent intent = new Intent(SubmitBid.this,SinglePost.class);
                              intent.putExtra("JobId",JobId);
                              intent.putExtra("UserEmail", UserEmail);
                              startActivity(intent);
                          } else{
                              progressDialog.dismiss();
                              Toast.makeText(SubmitBid.this,"Error, Bid not submitted",Toast.LENGTH_SHORT).show();
                          }
                        }
                    },8000);
                }
                catch (Exception e){}

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.Home) {
            Intent intent = new Intent(SubmitBid.this,UserMainActivity.class);
            intent.putExtra("UserEmail",UserMainActivity.UserEmail);
            startActivity(intent);
        }
        if (id == R.id.Logout){
            String token_id = "";
            String current_user = UserMainActivity.UserEmail.replace(".",",");
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("token_id",token_id);
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.getReference("users").child(current_user).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    FirebaseAuth.getInstance().signOut();
                    final AlertDialog progressDialog = new SpotsDialog(SubmitBid.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(SubmitBid.this,login.class);
                            startActivity(intent);
                            finish();
                        }
                    },2000);
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private JSONObject makeJSONobject() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        millisInString  = dateFormat.format(new Date());
        Log.i("totalBids",totalBids);
        JSONObject jobject = new JSONObject();
        try{
            jobject.put("JobId",JobId);
            jobject.put("BidBy",UserEmail);
            jobject.put("BidCoins",bidCoinsEditText.getText().toString().trim());
            jobject.put("description",bidDescriptionEditText.getText().toString().trim());
            jobject.put("status","pending");
            jobject.put("timeStamp",millisInString);
            jobject.put("totalBids",totalBids);
        }catch (Exception e){

        }
        return jobject;
    }
}

class SubmitBidAsync extends AsyncTask<URL,Void,Void>{
    public  String response;
    public  String saveResponse;
    @Override
    protected void onPostExecute(Void aVoid) {

    }

    @Override
    protected Void doInBackground(URL... urls) {
        try{
            URLConnection connection = urls[0].openConnection();
            connection.setConnectTimeout(9000);
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
        SubmitBid.CloudFunctionResponse = saveResponse;
        return null;
    }
}
