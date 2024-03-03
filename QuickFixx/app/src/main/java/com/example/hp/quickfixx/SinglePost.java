package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.*;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.quickfixx.Model.Balance;
import com.example.hp.quickfixx.Model.Bid;
import com.example.hp.quickfixx.Model.ImageGallery;
import com.example.hp.quickfixx.Model.Job;
import com.example.hp.quickfixx.Model.User;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class SinglePost extends AppCompatActivity {

    String JobId;
    ImageView profileImageImageView,jobImage;
    TextView showOnMaps,postedBy,postedByEmail,address,latitude,longitude,postedTime,titleTextView,coinsTextView,daysTextView,sponsorTextView,assignedToTextView,totalVoteUp,totalVoteDown,jobDescription,totalPeopleApplied;
    ButtonRectangle applyBtn,sponsorBtn;
    String profilePicRef;
    String profilePicUrl;
    String jobPicRef;
    String UserEmail = "";
    String totalBids;
    boolean alreadyBid = false;

    String quickFixBalance;

    int coinsAvailable;
    int coinsProposed;

    static String CloudFunctionResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        Intent intent = getIntent();
        JobId = intent.getStringExtra("JobId");
        UserEmail = intent.getStringExtra("UserEmail");
        Toast.makeText(SinglePost.this,JobId,Toast.LENGTH_SHORT).show();

        getQuickFixBalance();

        profileImageImageView = (ImageView) findViewById(R.id.profileImageImageView);
        jobImage = (ImageView) findViewById(R.id.jobImage);
        showOnMaps = (TextView) findViewById(R.id.showOnMaps);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        postedBy = (TextView) findViewById(R.id.postedBy);
        address = (TextView) findViewById(R.id.address);
        postedTime = (TextView) findViewById(R.id.postedTime);
        titleTextView = (TextView) findViewById(R.id.titleTextview);
        coinsTextView = (TextView) findViewById(R.id.coinsTextview);
        daysTextView = (TextView) findViewById(R.id.daysTextview);
        sponsorTextView = (TextView) findViewById(R.id.sponsorTextview);
        assignedToTextView = (TextView) findViewById(R.id.assignedToTextview);
        totalVoteUp = (TextView) findViewById(R.id.totalVoteUp);
        totalVoteDown = (TextView) findViewById(R.id.totalVoteDown);
        totalPeopleApplied = (TextView) findViewById(R.id.totalPeopleApplied);
        jobDescription = (TextView) findViewById(R.id.jobDescription);
        postedByEmail = (TextView) findViewById(R.id.postedByEmail);
        applyBtn = (ButtonRectangle) findViewById(R.id.applyBtn);
        sponsorBtn = (ButtonRectangle) findViewById(R.id.sponsorBtn);

        if (UserEmail.equals("")){
            sponsorBtn.setVisibility(View.GONE);
            applyBtn.setVisibility(View.GONE);
        }

        readData(JobId);
        checkBid(JobId,UserEmail);

        latitude.setVisibility(View.GONE);
        longitude.setVisibility(View.GONE);
        postedByEmail.setVisibility(View.GONE);

        final AlertDialog progressDialog = new SpotsDialog(SinglePost.this,R.style.Custom);
        progressDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference dateRef = storageRef.child(profilePicRef);
                dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        profilePicUrl = uri.toString();
                        Picasso.with(SinglePost.this)
                                .load(uri)
                                .resize(1280,960)
                                .centerInside()
                                .into(profileImageImageView);

                    }
                });
                StorageReference imageRef = storageRef.child(jobPicRef);
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(SinglePost.this)
                                .load(uri)
                                .resize(1280,960)
                                .centerInside()
                                .into(jobImage);
                    }
                });
                if (sponsorTextView.getText().equals("Available")){
                    sponsorBtn.setEnabled(false);
                }
                if (alreadyBid == true){
                    applyBtn.setEnabled(false);
                }
                if (postedByEmail.getText().equals(login.email)){
                    sponsorBtn.setVisibility(View.GONE);
                    applyBtn.setVisibility(View.GONE);
                }

            }
        },5000);

        jobImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SinglePost.this);

                alertDialog.setTitle("Download Picture");
                alertDialog.setMessage("Want to download this picture?");

                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (ContextCompat.checkSelfPermission(SinglePost.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(SinglePost.this,"Permission denied. Allow permission from permissions",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                        }
                        else {
                            final AlertDialog progressDialog = new SpotsDialog(SinglePost.this,R.style.Custom);
                            progressDialog.show();
                            final Handler handler1 = new Handler();
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    jobImage.setDrawingCacheEnabled(true);
                                    Bitmap bmap = jobImage.getDrawingCache();
                                    MediaStore.Images.Media.insertImage(SinglePost.this.getContentResolver(), bmap, "ImageView" , null);
                                    Toast.makeText(SinglePost.this,"Download Successfully",Toast.LENGTH_SHORT).show();
                                }
                            },3000);
                        }

                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog.show();
                return true;
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(SinglePost.this,SubmitBid.class);
                intent1.putExtra("JobTitle",titleTextView.getText().toString());
                intent1.putExtra("JobId",JobId);
                intent1.putExtra("UserEmail",UserEmail);
                intent1.putExtra("totalBids",totalBids);
                startActivity(intent1);
            }
        });
        sponsorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference coinsRef = FirebaseDatabase.getInstance().getReference("coins").child("balance").child(UserEmail.replace(".",","));
                coinsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot!=null){
                            Balance balance = dataSnapshot.getValue(Balance.class);
                            coinsAvailable = Integer.parseInt(balance.getCoins1());
                            coinsProposed = Integer.parseInt(coinsTextView.getText().toString());
                            if (coinsProposed<coinsAvailable){
                                SponsorJob();
                            }
                            else {
                                Toast.makeText(SinglePost.this,"You dont have enough coins",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
        profileImageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UserEmail = postedByEmail.getText().toString().trim();
                Intent intent1 = new Intent(SinglePost.this,ShowProfile.class);
                intent1.putExtra("UserEmail",UserEmail);
                startActivity(intent1);
            }
        });
        postedBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UserEmail = postedByEmail.getText().toString().trim();
                Intent intent1 = new Intent(SinglePost.this,ShowProfile.class);
                intent1.putExtra("UserEmail",UserEmail);
                startActivity(intent1);
            }
        });
        showOnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String latitudee = String.valueOf(latitude.getText().toString().trim());
                final String longitudee = String.valueOf(longitude.getText().toString().trim());

                final AlertDialog progessDialog = new SpotsDialog(SinglePost.this,R.style.Custom);
                progessDialog.show();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progessDialog.dismiss();
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitudee +"," + longitudee);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                },5000);
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
            Intent intent = new Intent(SinglePost.this,UserMainActivity.class);
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
                    final AlertDialog progressDialog = new SpotsDialog(SinglePost.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(SinglePost.this,login.class);
                            startActivity(intent);
                            finish();
                        }
                    },2000);
                }
            });
        }

        return super.onOptionsItemSelected(item);
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

    private void SponsorJob() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(SinglePost.this);
        alertDialog.setTitle("Confirm!!");
        alertDialog.setMessage("Are you sure?");

        alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final AlertDialog progressDialog = new SpotsDialog(SinglePost.this,R.style.Custom);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("JobId",JobId);
                    jsonObject.put("sponsoredBy",UserEmail);
                    jsonObject.put("newCoins",Integer.toString(coinsAvailable-coinsProposed));
                    jsonObject.put("CoinsProposed",Integer.toString(coinsProposed));
                    jsonObject.put("quickFixBalance",quickFixBalance);
                    jsonObject.put("sponsoredBy2",UserEmail.replace(".",","));

                    URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/SponsorJob?text="+jsonObject.toString().trim());
                    new AsyncSponsorJob().execute(url);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            if (CloudFunctionResponse.equals("SponsorSuccessful")){
                                Toast.makeText(SinglePost.this,"Successfully sponsored",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SinglePost.this,SinglePost.class);
                                intent.putExtra("JobId",JobId);
                                intent.putExtra("UserEmail",UserEmail);
                                startActivity(intent);
                            }else {
                                Toast.makeText(SinglePost.this,"Some error",Toast.LENGTH_SHORT).show();
                            }
                        }
                    },7000);
                }catch (Exception e){}
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.show();
    }

    private void checkBid(String jobId, final String userEmail) {
        DatabaseReference bidsRef = FirebaseDatabase.getInstance().getReference("bids").child(jobId);
        bidsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Bid bid = dataSnapshot.getValue(Bid.class);
                if (bid.getBidBy5().equals(userEmail)){
                    alreadyBid = true;
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readData(final String jobId) {
        DatabaseReference jobRef = FirebaseDatabase.getInstance().getReference("jobs").child(jobId);
        jobRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    Job job = dataSnapshot.getValue(Job.class);

                    String addresss = job.getAddress().replaceAll("[0-9]", "");
                    address.setText(addresss);
                    postedTime.setText(job.getTimeStamp());
                    titleTextView.setText(job.getJobTitle());
                    coinsTextView.setText(job.getCoins());
                    daysTextView.setText(job.getMaxNoDays());
                    totalVoteUp.setText(job.getVoteUp());
                    totalVoteDown.setText(job.getVoteDown());
                    totalPeopleApplied.setText(job.getTotalBids());
                    jobDescription.setText(job.getJobDescription());
                    latitude.setText(job.getLatitude());
                    longitude.setText(job.getLongitude());

                    String currentAssigner = job.getAssignedTo();
                    if (currentAssigner.equals("")){
                        assignedToTextView.setText("Not Available");
                    }else {
                        readName(currentAssigner);
                    }

                    totalBids = job.getTotalBids();

                    String sponsor = job.getSponsoredBy();
                    if (sponsor.equals("Get Sponsor")){
                        sponsorTextView.setText("Not Available");
                    }
                    else {
                        sponsorTextView.setText("Available");
                    }

                    String currentPostedBy = job.getPostedBy();
                    readProfileImage(currentPostedBy);
                    readJobImage(jobId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void readName(String currentAssigner) {
        String user = currentAssigner.replace(".",",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot !=null){
                    User user = dataSnapshot.getValue(User.class);
                    assignedToTextView.setText(user.getFirstName2()+" "+user.getLastName2());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void readJobImage(String jobId) {
        DatabaseReference imageGalleryRef = FirebaseDatabase.getInstance().getReference("imageGallery").child(jobId);
        imageGalleryRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot !=null){
                    ImageGallery imageGallery = dataSnapshot.getValue(ImageGallery.class);
                    jobPicRef = imageGallery.getImageUrl1();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readProfileImage(String currentPostedBy) {
        String user = currentPostedBy.replace(".",",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot !=null){
                    User user = dataSnapshot.getValue(User.class);
                    profilePicRef = user.getProfilePicUrl2();
                    postedBy.setText(user.getFirstName2()+" "+user.getLastName2());
                    postedByEmail.setText(user.getEmail2());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
class AsyncSponsorJob extends AsyncTask<URL,Void,Void> {
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
        SinglePost.CloudFunctionResponse = saveResponse;
        return null;
    }
}