package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.hp.quickfixx.Model.Balance;
import com.example.hp.quickfixx.Model.ImageGallery;
import com.example.hp.quickfixx.Model.Job;
import com.example.hp.quickfixx.Model.User;
import com.example.hp.quickfixx.Model.Verification;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class VolunteerSinglePost extends AppCompatActivity {

    String key,status;
    String jobId,bidId;
    TextView jobTitle,latitude,longitude,showOnMaps;
    ImageView jobImage;
    ButtonRectangle showProgress,acceptBtn,rejectBtn;

    String quickFixBalance;
    String assignedTo,assignedToBalance;
    String coins;

    static android.app.FragmentManager fm;
    static FragmentTransaction ft;

    String totalJobsDone,ratings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_single_post);

        final Intent intent = getIntent();
        key = intent.getStringExtra("key");
        status = intent.getStringExtra("status");

        getQuickFixBalance();

        jobTitle = (TextView) findViewById(R.id.titleTextview);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        jobImage = (ImageView) findViewById(R.id.jobImage);
        showOnMaps = (TextView) findViewById(R.id.showOnMaps);
        showProgress = (ButtonRectangle) findViewById(R.id.progressBtn);
        acceptBtn = (ButtonRectangle) findViewById(R.id.acceptBtn);
        rejectBtn = (ButtonRectangle) findViewById(R.id.rejectBtn);

        if (status.equals("hide")){
            acceptBtn.setVisibility(View.GONE);
            rejectBtn.setVisibility(View.GONE);
        }

        readData(key);

        latitude.setVisibility(View.GONE);
        longitude.setVisibility(View.GONE);

        showProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(VolunteerSinglePost.this,JobProgress.class);
                intent1.putExtra("JobId",jobId);
                intent1.putExtra("JobTitle",jobTitle.getText().toString().trim());
                startActivity(intent1);
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final android.app.AlertDialog alertDialog = new AlertDialog.Builder(VolunteerSinglePost.this).create();

                final EditText editText = new EditText(VolunteerSinglePost.this);
                editText.setHint("Enter reason....");

                final TextView textView = new TextView(VolunteerSinglePost.this);
                textView.setText("Rate this Job:");

                LinearLayout subLinearlayout = new LinearLayout(VolunteerSinglePost.this);
                final RatingBar ratingBar = new RatingBar(VolunteerSinglePost.this);
                subLinearlayout.addView(ratingBar);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                subLinearlayout.setLayoutParams(params);

                alertDialog.setTitle("Verification");
                alertDialog.setMessage("Are you sure you want to verify?");

                LinearLayout linearLayout = new LinearLayout(VolunteerSinglePost.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                linearLayout.addView(editText);
                linearLayout.addView(textView);
                linearLayout.addView(subLinearlayout);

                alertDialog.setView(linearLayout);


                alertDialog.setButton("Verify", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject jsonObject = new JSONObject();
                        try{
                            jsonObject.put("key",key);
                            jsonObject.put("JobId",jobId);
                            jsonObject.put("BidId",bidId);
                            jsonObject.put("Description",editText.getText().toString().trim());
                            jsonObject.put("quickFixBalance",quickFixBalance); Log.i("quickFixBalance",quickFixBalance);
                            jsonObject.put("assignedTo",assignedTo.replace(".",",")); Log.i("assignedTo",assignedTo.replace(".",","));
                            jsonObject.put("assignedToBalance",assignedToBalance); Log.i("assignedToBalance",assignedToBalance);
                            jsonObject.put("coins",coins); Log.i("coins",coins);
                            jsonObject.put("ratings",Float.toString(ratingBar.getRating()));
                            jsonObject.put("userratings",ratings);
                            jsonObject.put("totalJobsDone",totalJobsDone);

                            URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/VerificationDone?text="+jsonObject.toString());

                            new AsyncInsertJobInfo().execute(url);

                        }catch (Exception e){}
                    }
                });
                alertDialog.show();
            }
        });
        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final android.app.AlertDialog alertDialog = new AlertDialog.Builder(VolunteerSinglePost.this).create();

                final EditText editText = new EditText(VolunteerSinglePost.this);
                editText.setHint("Enter reason....");

                alertDialog.setTitle("Verification");
                alertDialog.setMessage("Are you sure you want to reject?");

                LinearLayout linearLayout = new LinearLayout(VolunteerSinglePost.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                linearLayout.addView(editText);

                alertDialog.setView(linearLayout);


                alertDialog.setButton("Reject", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject jsonObject = new JSONObject();
                        try{
                            jsonObject.put("key",key);
                            jsonObject.put("JobId",jobId);
                            jsonObject.put("BidId",bidId);
                            jsonObject.put("Description",editText.getText().toString().trim());

                            URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/VerficationRejected?text="+jsonObject.toString());

                            new AsyncInsertJobInfo().execute(url);

                        }catch (Exception e){}
                    }
                });
                alertDialog.show();
            }
        });
        showOnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String latitudee = String.valueOf(latitude.getText().toString().trim());
                final String longitudee = String.valueOf(longitude.getText().toString().trim());

                final AlertDialog progessDialog = new SpotsDialog(VolunteerSinglePost.this,R.style.Custom);
                progessDialog.show();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progessDialog.dismiss();
                        Uri IntentUri = Uri.parse("google.navigation:q=" + latitudee +"," + longitudee);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, IntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                },5000);
            }
        });
    }

    private void getQuickFixBalance() {
        DatabaseReference balanceRef = FirebaseDatabase.getInstance().getReference("coins").child("balance").child("quickFix");
        balanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    Balance balance = dataSnapshot.getValue(Balance.class);
                    quickFixBalance = balance.getCoins1();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.volunteer_menu, menu);
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
            Intent intent = new Intent(VolunteerSinglePost.this,UserMainActivity.class);
            intent.putExtra("UserEmail",UserMainActivity.UserEmail);
            startActivity(intent);
        }
        if (id == R.id.Logout){
            String token_id = "";
            String current_user = VolunteerMainActivity.UserEmail.replace(".",",");
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("token_id",token_id);
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.getReference("users").child(current_user).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    FirebaseAuth.getInstance().signOut();
                    final AlertDialog progressDialog = new SpotsDialog(VolunteerSinglePost.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(VolunteerSinglePost.this,login.class);
                            startActivity(intent);
                            finish();
                        }
                    },2000);
                }
            });
        }
        if (id == R.id.Notifications){
            if (id == R.id.Notifications){
                NotificationsVolunteer notifications = new NotificationsVolunteer();
                fm = getFragmentManager();
                ft = fm.beginTransaction();
                ft.replace(R.id.content_user_main,notifications);
                ft.commit();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void readData(String key) {
        DatabaseReference verificationsRef = FirebaseDatabase.getInstance().getReference("verifications").child(key);
        verificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    Verification verification = dataSnapshot.getValue(Verification.class);
                    bidId = verification.getBidId11();

                    String currentjobId = verification.getJobId11();
                    readJobImage(currentjobId);
                    readJobTitle(currentjobId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void readJobTitle(String currentjobid) {
        DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs").child(currentjobid);
        jobsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    Job job = dataSnapshot.getValue(Job.class);
                    jobTitle.setText(job.getJobTitle());
                    jobId = job.getJobId();

                    latitude.setText(job.getLatitude());
                    longitude.setText(job.getLongitude());

                    coins = job.getCoins();
                    assignedTo = job.getAssignedTo();
                    getAssignedToBalance(assignedTo.replace(".",","));
                    totalJobsDoneAndratings(assignedTo.replace(".",","));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void totalJobsDoneAndratings(String assignedTo) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(assignedTo);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    User user = dataSnapshot.getValue(User.class);
                    totalJobsDone = user.getTotalJobsDone();
                    ratings = user.getRatings1();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedToBalance(String assignedTo) {
        DatabaseReference balanceRef = FirebaseDatabase.getInstance().getReference("coins").child("balance").child(assignedTo);
        balanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    Balance balance = dataSnapshot.getValue(Balance.class);
                    assignedToBalance = balance.getCoins1();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void readJobImage(String currentJobID) {
        DatabaseReference imageGalleryRef = FirebaseDatabase.getInstance().getReference("imageGallery").child(currentJobID);
        imageGalleryRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot !=null){
                    ImageGallery imageGallery = dataSnapshot.getValue(ImageGallery.class);
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    StorageReference dateRef = storageRef.child(imageGallery.getImageUrl1());
                    dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                    {
                        @Override
                        public void onSuccess(Uri downloadUrl)
                        {
//                    Glide.with(getActivity()).load(downloadUrl).into(profileImageImageView);
                            int imageWidth =1280;
                            int imageHeight = 960 ;
                            Picasso.with(VolunteerSinglePost.this)
                                    .load(downloadUrl)
                                    .resize(imageWidth , imageHeight)
                                    .centerInside()
                                    .into(jobImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
