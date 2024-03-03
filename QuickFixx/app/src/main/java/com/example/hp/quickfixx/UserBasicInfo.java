package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

import dmax.dialog.SpotsDialog;

public class UserBasicInfo extends AppCompatActivity{

    private ImageView profile_image;
    private EditText FirstName,LastName,CellPhone,Address,Introduction,School,University,College,Age;
    private ButtonRectangle submit;

    private static final int PICK_IMAGE = 100;
    Uri ImageUri;
    String UserEmail;
    String key;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage storageRef;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_basic_info);

        Intent intent = getIntent();
        UserEmail = intent.getStringExtra("UserEmail");
        key = UserEmail.replace(".",",");

        profile_image = (ImageView) findViewById(R.id.BasicInfo_ProfileImage);
        FirstName = (EditText) findViewById(R.id.BasicInfo_FirstName);
        LastName = (EditText) findViewById(R.id.BasicInfo_LastName);
        CellPhone = (EditText) findViewById(R.id.BasicInfo_CellPhone);
        Address = (EditText) findViewById(R.id.BasicInfo_Address);
        Age = (EditText) findViewById(R.id.BasicInfo_Age);
        School = (EditText) findViewById(R.id.BasicInfo_School);
        College = (EditText) findViewById(R.id.BasicInfo_College);
        University = (EditText) findViewById(R.id.BasicInfo_University);
        Introduction = (EditText) findViewById(R.id.BasicInfo_Introduction);
        submit = (ButtonRectangle) findViewById(R.id.BasicInfo_SubmitBtn);

        firebaseDatabase = FirebaseDatabase.getInstance();

        storageRef = FirebaseStorage.getInstance();
        storageReference = storageRef.getReference().child("Profile_Images/"+key+".jpg");



        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), ImageUri);
                    UploadImage(bitmap);

                    final AlertDialog progressDialog = new SpotsDialog(UserBasicInfo.this,R.style.Custom);
                    progressDialog.show();

//                    if(downloadUrl.toString() != null){
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(UserBasicInfo.this,"Basic information added successfully",Toast.LENGTH_SHORT).show();
                            Intent intent1 = new Intent(UserBasicInfo.this,UserMainActivity.class);
                            intent1.putExtra("UserEmail",UserEmail);
                            startActivity(intent1);
                        }
                    },10000);
//                        }
//                    else{
//                        Toast.makeText(UserBasicInfo.this,"Profile Image uploading error",Toast.LENGTH_SHORT).show();
//                    }

                }
                catch (Exception e){
                    Log.i("Exception",e.toString());
                }
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
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
            Toast.makeText(UserBasicInfo.this,"Fill this form to move to main screen",Toast.LENGTH_LONG).show();
        }
        if (id == R.id.Logout){
                    FirebaseAuth.getInstance().signOut();
                    final AlertDialog progressDialog = new SpotsDialog(UserBasicInfo.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(UserBasicInfo.this,login.class);
                            startActivity(intent);
                            finish();
                        }
                    },2000);
                }

        return super.onOptionsItemSelected(item);
    }

    //making JSONobject for passing in URL
    private JSONObject makeJSONobject(){
        JSONObject jobject = new JSONObject();
        try{
            jobject.put("FirstName",FirstName.getText().toString().trim());
            jobject.put("LastName",LastName.getText().toString().trim());
            jobject.put("Email",UserEmail);
            jobject.put("Phone",CellPhone.getText().toString().trim());
            jobject.put("VerificationStatus","0");
            jobject.put("Address",Address.getText().toString().trim());
            jobject.put("ProfilePicUrl","Profile_Images/"+key+".jpg");
            jobject.put("Type","user");
            jobject.put("key",key);
            jobject.put("Introduction",Introduction.getText().toString().trim());
            jobject.put("Age",Age.getText().toString().trim());
            jobject.put("School",School.getText().toString().trim());
            jobject.put("College",College.getText().toString().trim());
            jobject.put("University",University.getText().toString().trim());
        }
        catch (Exception e){
            Log.i("error",e.toString());
        }
        return jobject;
    }

    //for Uploading image on storage
    private void UploadImage(Bitmap bitmap){
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,20,boas);
        byte[] data = boas.toByteArray();
        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                UploadData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                return;
            }
        });
    }

    private void UploadData(){
        try {
            String data = makeJSONobject().toString();
            URL CloudFunctionUrl = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/InsertBasicUserInfo?text="+data);
            new AsyncInsertBasicInfoUser().execute(CloudFunctionUrl);
        }catch (Exception e){

        }

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
            //profile_image.setImageURI(ImageUri);

            //using PICASSO library for making image orientation vertical
            int imageWidth =1280;
            int imageHeight = 960 ;
            Picasso.with(UserBasicInfo.this)
                    .load(ImageUri)
                    .resize(imageWidth , imageHeight)
                    .centerInside()
                    .into(profile_image);
        }
    }
}

class AsyncInsertBasicInfoUser extends AsyncTask<URL,Void,Void> {
    @Override
    protected Void doInBackground(URL... urls) {
        try{
            URLConnection connection = urls[0].openConnection();
            connection.setConnectTimeout(5000);
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
