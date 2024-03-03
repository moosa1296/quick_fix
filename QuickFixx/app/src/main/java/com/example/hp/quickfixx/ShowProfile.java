package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.quickfixx.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class ShowProfile extends AppCompatActivity {

    String UserEmail,millisInString;
    ImageView profileImageImageView, editIntro, editEducation, editContact, editResidence, message;
    TextView username, age, totalJobsDone, uploadProfileImage;
    EditText  introduction, school, college, university, address, cellPhone, Email;
    String profilePicRef;
    RatingBar ratingBar;
    Uri profilePicUri;
    static String CloudFunctionResponse = "";
    private static final int PICK_IMAGE = 100;
    Uri ImageUri;

    static String cloudFunctionResponse="";

    StorageReference dateRef;
    AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        Intent intent = getIntent();
        UserEmail = intent.getStringExtra("UserEmail");

        profileImageImageView = (ImageView) findViewById(R.id.profileImageImageView);
        editIntro = (ImageView) findViewById(R.id.editIntro);
        editEducation = (ImageView) findViewById(R.id.editEducation);
        editResidence = (ImageView) findViewById(R.id.editResidence);
        editContact = (ImageView) findViewById(R.id.editContact);
        message = (ImageView) findViewById(R.id.message);
        username = (TextView) findViewById(R.id.username);
        age = (TextView) findViewById(R.id.age);
        totalJobsDone = (TextView) findViewById(R.id.totalJobsDone);
        introduction = (EditText) findViewById(R.id.introduction);
        school = (EditText) findViewById(R.id.school);
        college = (EditText) findViewById(R.id.college);
        university = (EditText) findViewById(R.id.university);
        address = (EditText) findViewById(R.id.address);
        cellPhone = (EditText) findViewById(R.id.cellPhone);
        Email = (EditText) findViewById(R.id.Email);
        uploadProfileImage = (TextView) findViewById(R.id.uploadProfileImage);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        ratingBar.setFocusable(false);
        introduction.setEnabled(false); school.setEnabled(false); college.setEnabled(false); university.setEnabled(false);
        address.setEnabled(false); cellPhone.setEnabled(false); Email.setEnabled(false);

        if (!(UserEmail.equals(UserMainActivity.UserEmail))){
            editIntro.setVisibility(View.GONE);
            editEducation.setVisibility(View.GONE);
            editResidence.setVisibility(View.GONE);
            editContact.setVisibility(View.GONE);
            uploadProfileImage.setVisibility(View.GONE);
        }
        if (UserEmail.equals(UserMainActivity.UserEmail)){
            message.setVisibility(View.GONE);
        }

        String useremail = UserEmail.replace(".", ",");
        readData(useremail);

        progressDialog = new SpotsDialog(ShowProfile.this,R.style.Custom);
        progressDialog.show();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                dateRef = storageRef.child(profilePicRef);
                dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        profilePicUri = uri;
                        Picasso.with(ShowProfile.this)
                                .load(uri)
                                .resize(1280, 960)
                                .centerInside()
                                .into(profileImageImageView);
                    }
                });
            }
        }, 5000);

        uploadProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShowProfile.this);
                final EditText editText = new EditText(ShowProfile.this);
                editText.setHint("Enter Message");

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(65,0,0,0);

                final TextView textview = new TextView(ShowProfile.this);
                textview.setText("Receiver: "+UserEmail);
                textview.setLayoutParams(params);

                LinearLayout linearLayout = new LinearLayout(ShowProfile.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(textview);
                linearLayout.addView(editText);

                alertDialog.setTitle("Send Message");
                alertDialog.setView(linearLayout);

                final AlertDialog progressDialog = new SpotsDialog(ShowProfile.this,R.style.Custom);

                alertDialog.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.show();
                        String sender = UserMainActivity.UserEmail.replace(".",",");
                        String receiver = UserEmail.replace(".",",");
                        int compare = sender.compareTo(receiver);
                        final String child;

                        if (compare<0){
                            child = sender+receiver;
                        }
                        else {
                            child = receiver+sender;
                        }
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        millisInString  = dateFormat.format(new Date());

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("sendBy",sender);
                            jsonObject.put("sendTo",receiver);
                            jsonObject.put("message",editText.getText().toString().trim());
                            jsonObject.put("child",child);
                            jsonObject.put("timeStamp",millisInString);

                            URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/AddNewMessage?text="+jsonObject.toString());
                            new AsyncNewMessageProfile().execute(url);
                            final Handler handler1 = new Handler();
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    if (cloudFunctionResponse.equals("messageAdded")){
                                        Toast.makeText(ShowProfile.this,"Message sent",Toast.LENGTH_SHORT).show();
                                        Intent intent1 = new Intent(ShowProfile.this,Message_Thread.class);
                                        intent1.putExtra("child",child);
                                        startActivity(intent1);
                                    }else {
                                        Toast.makeText(ShowProfile.this,"Message sending failed",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            },6000);
                        } catch (Exception e) {
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog.show();
            }
        });

        editIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShowProfile.this);

                final EditText editText = new EditText(ShowProfile.this);
                editText.setText(introduction.getText().toString().trim());

                //input filter is used to limit the characters of EditText
                InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(500);
                editText.setFilters(filterArray);

                alertDialog.setTitle("Edit Introduction");
                alertDialog.setMessage("Enter your new introduction");
                alertDialog.setView(editText);

                alertDialog.setPositiveButton("Update Intro", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("UserEmail", UserEmail.replace(".", ","));
                            jsonObject.put("Introduction", editText.getText().toString().trim());

                            URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/UpdateIntroduction?text="+jsonObject.toString());
                            new UpdateProfileAsync().execute(url);
                            final ProgressDialog progressDialog1 = new ProgressDialog(ShowProfile.this);
                            progressDialog1.setTitle("Please wait");
                            progressDialog1.setMessage("Updating Introduction....");
                            progressDialog1.show();
                            final Handler handler1 = new Handler();
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                  int i=0;
                                    while (i<1){
                                        if (CloudFunctionResponse.equals("IntroductionUpdated")){
                                            Toast.makeText(ShowProfile.this,"Introduction Updated",Toast.LENGTH_SHORT).show();
                                            progressDialog1.dismiss();
                                            break;
                                        }
                                    }
                                }
                            },5000);
                        } catch (Exception e) {
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ShowProfile.this, "Cancel intro", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();

            }
        });
        editEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShowProfile.this);

                final TextView textView1 = new TextView(ShowProfile.this);
                textView1.setText("Enter School");
                textView1.setTextColor(Color.parseColor("#1E88E5"));

                final TextView textView2 = new TextView(ShowProfile.this);
                textView2.setText("Enter College");
                textView2.setTextColor(Color.parseColor("#1E88E5"));

                final TextView textView3 = new TextView(ShowProfile.this);
                textView3.setText("Enter University");
                textView3.setTextColor(Color.parseColor("#1E88E5"));

                final EditText editText1 = new EditText(ShowProfile.this);
                editText1.setText(school.getText().toString().trim());

                final EditText editText2 = new EditText(ShowProfile.this);
                editText2.setText(college.getText().toString().trim());

                final EditText editText3 = new EditText(ShowProfile.this);
                editText3.setText(university.getText().toString().trim());

                LinearLayout linearLayout = new LinearLayout(ShowProfile.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(textView1);
                linearLayout.addView(editText1);
                linearLayout.addView(textView2);
                linearLayout.addView(editText2);
                linearLayout.addView(textView3);
                linearLayout.addView(editText3);

                alertDialog.setTitle("Edit Education");
                alertDialog.setMessage("Enter your new education details");
                alertDialog.setView(linearLayout);

                alertDialog.setPositiveButton("Update Education", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("UserEmail", UserEmail.replace(".", ","));
                            jsonObject.put("School", editText1.getText().toString().trim());
                            jsonObject.put("College",editText2.getText().toString().trim());
                            jsonObject.put("University",editText3.getText().toString().trim());

                            URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/UpdateEducation?text="+jsonObject.toString());
                            new UpdateProfileAsync().execute(url);
                            final ProgressDialog progressDialog1 = new ProgressDialog(ShowProfile.this);
                            progressDialog1.setTitle("Please wait");
                            progressDialog1.setMessage("Updating Education....");
                            progressDialog1.show();
                            final Handler handler1 = new Handler();
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int i=0;
                                    while (i<1){
                                        if (CloudFunctionResponse.equals("EducationUpdated")){
                                            Toast.makeText(ShowProfile.this,"Education Updated",Toast.LENGTH_SHORT).show();
                                            progressDialog1.dismiss();
                                            break;
                                        }
                                    }
                                }
                            },5000);
                        } catch (Exception e) {
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ShowProfile.this, "Cancel intro", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();

            }
        });
        editResidence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShowProfile.this);
                final EditText editText = new EditText(ShowProfile.this);
                editText.setText(address.getText().toString().trim());

                alertDialog.setTitle("Edit Residence");
                alertDialog.setMessage("Enter your new address");
                alertDialog.setView(editText);

                alertDialog.setPositiveButton("Update Residence", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("UserEmail", UserEmail.replace(".", ","));
                            jsonObject.put("Address", editText.getText().toString().trim());

                            URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/UpdateResidence?text="+jsonObject.toString());
                            new UpdateProfileAsync().execute(url);
                            final ProgressDialog progressDialog1 = new ProgressDialog(ShowProfile.this);
                            progressDialog1.setTitle("Please wait");
                            progressDialog1.setMessage("Updating Residence....");
                            progressDialog1.show();
                            final Handler handler1 = new Handler();
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int i=0;
                                    while (i<1){
                                        if (CloudFunctionResponse.equals("ResidenceUpdated")){
                                            Toast.makeText(ShowProfile.this,"Residence Updated",Toast.LENGTH_SHORT).show();
                                            progressDialog1.dismiss();
                                            break;
                                        }
                                    }
                                }
                            },5000);
                        } catch (Exception e) {
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ShowProfile.this, "Cancel intro", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();
            }
        });
        editContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShowProfile.this);
                final EditText editText = new EditText(ShowProfile.this);
                editText.setText(cellPhone.getText().toString().trim());
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                //input filter is used to limit the characters of EditText
                InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(11);
                editText.setFilters(filterArray);

                alertDialog.setTitle("Edit Contact");
                alertDialog.setMessage("Enter your new CellPhone");
                alertDialog.setView(editText);

                alertDialog.setPositiveButton("Update Contact", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("UserEmail", UserEmail.replace(".", ","));
                            jsonObject.put("Phone", editText.getText().toString().trim());

                            URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/UpdateContact?text="+jsonObject.toString());
                            new UpdateProfileAsync().execute(url);
                            final ProgressDialog progressDialog1 = new ProgressDialog(ShowProfile.this);
                            progressDialog1.setTitle("Please wait");
                            progressDialog1.setMessage("Updating Contact....");
                            progressDialog1.show();
                            final Handler handler1 = new Handler();
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int i=0;
                                    while (i<1){
                                        if (CloudFunctionResponse.equals("ContactUpdated")){
                                            Toast.makeText(ShowProfile.this,"Contact Updated",Toast.LENGTH_SHORT).show();
                                            progressDialog1.dismiss();
                                            break;
                                        }
                                    }
                                }
                            },5000);
                        } catch (Exception e) {
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ShowProfile.this, "Cancel intro", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();
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
            Intent intent = new Intent(ShowProfile.this,UserMainActivity.class);
            intent.putExtra("UserEmail",UserEmail);
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
                    final AlertDialog progressDialog = new SpotsDialog(ShowProfile.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(ShowProfile.this,login.class);
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
            progressDialog.show();
            dateRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), ImageUri);
                            ByteArrayOutputStream boas = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG,20,boas);
                            byte[] data = boas.toByteArray();
                            UploadTask uploadTask = dateRef.putBytes(data);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()){
                                        progressDialog.dismiss();
                                        Toast.makeText(ShowProfile.this,"Profile picture updated",Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ShowProfile.this,ShowProfile.class);
                                        intent.putExtra("UserEmail",UserEmail);
                                        startActivity(intent);
                                    }else {
                                        progressDialog.dismiss();
                                        Toast.makeText(ShowProfile.this,"Profile picture updated failed",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }catch (Exception e){}
                    }
                    else {
                        Toast.makeText(ShowProfile.this,"Something wrong",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void readData(String useremail) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(useremail);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    User user = dataSnapshot.getValue(User.class);
                    username.setText(user.getFirstName2() + " " + user.getLastName2());
                    age.setText(user.getAge2());
                    introduction.setText(user.getIntroduction2());
                    school.setText(user.getSchool2());
                    college.setText(user.getCollege2());
                    university.setText(user.getUniversity2());
                    address.setText(user.getAddress2());
                    cellPhone.setText(user.getPhone2());
                    Email.setText(UserEmail);
                    profilePicRef = user.getProfilePicUrl2();
                    ratingBar.setRating(Float.parseFloat(user.getRatings1()));
                    totalJobsDone.setText(user.getTotalJobsDone());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class UpdateProfileAsync extends AsyncTask<URL, Void, Void> {
        public String response;
        public String saveResponse;

        @Override
        protected void onPostExecute(Void aVoid) {
            login.MoveToNextAcitivity();
        }

        @Override
        protected Void doInBackground(URL... urls) {
            try {
                URLConnection connection = urls[0].openConnection();
                connection.setConnectTimeout(5000);
                connection.getContentLength();

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));

                while ((response = in.readLine()) != null) {
                    saveResponse = response;
                }

                in.close();

            } catch (SocketTimeoutException s) {
                Log.i("Socket", "Connection request timeout");
            } catch (Exception e) {
            }
            ShowProfile.CloudFunctionResponse = saveResponse;
            return null;
        }
    }
}
class AsyncNewMessageProfile extends AsyncTask<URL,Void,Void> {
    public  String response;
    public  String saveResponse;

    @Override
    protected void onPostExecute(Void aVoid) {

    }

    @Override
    protected Void doInBackground(URL... urls) {
        Log.i("back","back");
        try{
            URLConnection connection = urls[0].openConnection();
            connection.setConnectTimeout(6000);
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
        ShowProfile.cloudFunctionResponse = saveResponse;
        return null;
    }
}