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
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class SubmitProgress extends AppCompatActivity {

    String JobId;
    ImageView selectImage;
    EditText progressDescriptionEditText;
    ButtonRectangle submitProgressBtn;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    String key;

    private static final int PICK_IMAGE = 100;
    Uri ImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_progress);

        Intent intent = getIntent();
        JobId = intent.getStringExtra("JobId");

        selectImage = (ImageView) findViewById(R.id.selectImage);
        progressDescriptionEditText = (EditText) findViewById(R.id.progressDescriptionEditText);
        submitProgressBtn = (ButtonRectangle) findViewById(R.id.submitProgressBtn);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        key = databaseReference.push().getKey();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("Progress_Images/"+key+"/"+key+".jpg");

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });

        submitProgressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), ImageUri);
                    UploadImage(bitmap);

                }catch (Exception e){}
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
            Intent intent = new Intent(SubmitProgress.this,UserMainActivity.class);
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
                    final AlertDialog progressDialog = new SpotsDialog(SubmitProgress.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(SubmitProgress.this,login.class);
                            startActivity(intent);
                            finish();
                        }
                    },2000);
                }
            });
        }

        return super.onOptionsItemSelected(item);
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
            Picasso.with(SubmitProgress.this)
                    .load(ImageUri)
                    .resize(imageWidth , imageHeight)
                    .centerInside()
                    .into(selectImage);
        }
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

    private void UploadData() {
        try {
            String data = makeJSONobject().toString();
            URL CloudFunctionUrl = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/InsertProgress?text="+data);
            new AsyncInsertProgressInfo().execute(CloudFunctionUrl);

            String data2 = makeJSONobject2().toString();
            URL CloudFunctionUrl2 = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/InsertProgressGallery?text="+data2);
            new AsyncInsertProgressInfo().execute(CloudFunctionUrl2);
        }catch (Exception e){}
    }

    private JSONObject makeJSONobject(){

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("JobId",JobId);
            jsonObject.put("key",key);
            jsonObject.put("ProgressDescription",progressDescriptionEditText.getText().toString().trim());
        }catch (Exception e){}

        return jsonObject;
    }

    private JSONObject makeJSONobject2(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("JobId",JobId);
            jsonObject.put("ImageUrl","Progress_Images/"+key+"/"+key+".jpg");
            jsonObject.put("key",key);
        }catch (Exception e){}
        return jsonObject;
    }
}
class AsyncInsertProgressInfo extends AsyncTask<URL,Void,Void> {
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