package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class PostNewJob extends AppCompatActivity {


    private ImageView selectImage;
    private EditText jobTitle, jobDescription;
    private com.travijuu.numberpicker.library.NumberPicker selectCoins, selectMaxDays;
    private TextView latitude, longitude;
    private ButtonRectangle postJob;
    private RadioGroup radioGroup;


    private static final int PICK_IMAGE = 100;
    Uri ImageUri;
    String UserEmail;
    String key;
    String millisInString;
    String actualLatitude,actualLongitude;
    String comingLatitude,comingLongitude;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    private Geocoder geocoder;
    List<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_new_job);

        Intent intent = getIntent();
        UserEmail = intent.getStringExtra("UserEmail");
        comingLatitude = intent.getStringExtra("latitude");
        comingLongitude = intent.getStringExtra("longitude");

        selectImage = (ImageView) findViewById(R.id.selectImage);
        jobTitle = (EditText) findViewById(R.id.jobTitle);
        jobDescription = (EditText) findViewById(R.id.jobDescription);
        selectCoins = (com.travijuu.numberpicker.library.NumberPicker) findViewById(R.id.selectCoins);
        selectMaxDays = (com.travijuu.numberpicker.library.NumberPicker) findViewById(R.id.selectMaxDays);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        postJob = (ButtonRectangle) findViewById(R.id.postJob);
        radioGroup = (RadioGroup) findViewById(R.id.selectSponsorship);

        latitude.setText(comingLatitude);
        longitude.setText(comingLongitude);
        actualLatitude = comingLatitude;
        actualLongitude = comingLongitude;

        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        key = databaseReference.push().getKey();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("Job_Images/" + key + "/" + key + ".jpg");

        postJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.i("a","a");
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), ImageUri);
                    UploadImage(bitmap);
                    final AlertDialog progressDialog = new SpotsDialog(PostNewJob.this,R.style.Custom);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(PostNewJob.this,"Job has been posted",Toast.LENGTH_SHORT).show();
                            Intent intent1 = new Intent(PostNewJob.this,UserMainActivity.class);
                            intent1.putExtra("UserEmail",UserEmail);
                            startActivity(intent1);
                        }
                    },15000);

                }catch (Exception e){}

            }
        });

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });

    }

    //for selecting image from gallery
    private void SelectImage() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.setType("image/*");
        startActivityForResult(gallery,PICK_IMAGE);
    }
    //for getting result from SelectImage function
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE && data!=null){
            ImageUri = data.getData();
//            selectImage.setImageURI(ImageUri);

            //using PICASSO library for making image orientation vertical
            int imageWidth =1280;
            int imageHeight = 960 ;
            Picasso.with(PostNewJob.this)
                    .load(ImageUri)
                    .resize(imageWidth , imageHeight)
                    .centerInside()
                    .into(selectImage);
        }
    }

    //making JSONobject for passing in URL
    private JSONObject makeJSONobject(){
        Log.i("e","e");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        millisInString  = dateFormat.format(new Date());

        int selectedID = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(selectedID);

        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(actualLatitude), Double.parseDouble(actualLongitude),1);
        }catch (Exception e){}
        String address = addresses.get(0).getAddressLine(0);
        String sponsoredBy = radioButton.getText().toString().trim();
        if (sponsoredBy.equals("Self")){
            sponsoredBy = UserEmail;
        }

        JSONObject jobject = new JSONObject();
        try{
            Log.i("f","f");
            jobject.put("title",jobTitle.getText().toString().trim());
            jobject.put("description",jobDescription.getText().toString().trim());
            jobject.put("postedBy",UserEmail);
            jobject.put("coins",Integer.toString(selectCoins.getValue()));
            jobject.put("status","NotCompleted");
            jobject.put("visible","1");
            jobject.put("timestamp",millisInString);
            jobject.put("latitude",actualLatitude);
            jobject.put("longitude",actualLongitude);
            jobject.put("voteUp","0");
            jobject.put("voteDown","0");
            jobject.put("totalBids","0");
            jobject.put("assignedTo","");
            jobject.put("sponsoredBy",sponsoredBy);
            jobject.put("maxNoDays",Integer.toString(selectMaxDays.getValue()));
            jobject.put("address",address);
            jobject.put("key",key);
            Log.i("g","g");
        }
        catch (Exception e){
            Log.i("error",e.toString());
        }
        return jobject;
    }

    private JSONObject makeJSONobject2(){
        Log.i("h","h");
        JSONObject jobject = new JSONObject();
        try {
            jobject.put("key",key);
            jobject.put("ImageUrl","Job_Images/" + key + "/" + key + ".jpg");
            jobject.put("Description","FirstPic");
            jobject.put("Cover","1");
            jobject.put("timestamp",millisInString);
        }catch (Exception e){}

        return jobject;
    }

    //for Uploading image on storage
    private void UploadImage(Bitmap bitmap){
        Log.i("b","b");
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,20,boas);
        byte[] data = boas.toByteArray();
        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i("c","c");
                UploadData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                return;
            }
        });
    }

    private void UploadData() {
        try {
            Log.i("d","d");
            String data = makeJSONobject().toString();
            URL CloudFunctionUrl = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/InsertJobInfo?text="+data);
            new AsyncInsertJobInfo().execute(CloudFunctionUrl);

            String data2 = makeJSONobject2().toString();
            URL CloudFunctionUrl2 = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/InsertDataToImageGallery?text="+data2);
            new AsyncInsertJobInfo().execute(CloudFunctionUrl2);

        }catch (Exception e){

        }

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
            Intent intent = new Intent(PostNewJob.this,UserMainActivity.class);
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
                    final AlertDialog progressDialog = new SpotsDialog(PostNewJob.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(PostNewJob.this,login.class);
                            startActivity(intent);
                            finish();
                        }
                    },2000);
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }
}

class AsyncInsertJobInfo extends AsyncTask<URL,Void,Void> {
    @Override
    protected Void doInBackground(URL... urls) {
        try{
            URLConnection connection = urls[0].openConnection();
            connection.setConnectTimeout(8000);
            connection.getContentLength();

        }
        catch (SocketTimeoutException s){
            Log.i("Socket","Connection request timeout");
        }
        catch (Exception e){
        }
        return null;
    }
}