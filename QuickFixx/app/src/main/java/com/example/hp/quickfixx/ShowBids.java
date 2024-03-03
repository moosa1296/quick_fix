package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.quickfixx.Model.Bid;
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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class ShowBids extends AppCompatActivity {

    ArrayList<String> profileImages = new ArrayList<>();
    ArrayList<String> bidByEmails = new ArrayList<>();
    ArrayList<String> bidBys = new ArrayList<>();
    ArrayList<String> times = new ArrayList<>();
    ArrayList<String> bidIds = new ArrayList<>();
    ArrayList<String> bidPrices = new ArrayList<>();
    ArrayList<String> bidDescriptions = new ArrayList<>();
    ArrayList<String> bidstatuses = new ArrayList<>();

    ListView listView;
    showBidsAdapter adapter;
    String JobId;
    String Sponsorship;
    static String CloudFunctionResponse = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_bids);

        Intent intent = getIntent();
        JobId = intent.getStringExtra("JobId");
        Sponsorship = intent.getStringExtra("Sponsorship");

        listView = (ListView) findViewById(R.id.showBidsListview);
        adapter = new showBidsAdapter();
        readData(JobId);

        final AlertDialog progressDialog = new SpotsDialog(ShowBids.this,R.style.Custom);
        progressDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                listView.setAdapter(adapter);
            }
        },5000);

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
            Intent intent = new Intent(ShowBids.this,UserMainActivity.class);
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
                    final AlertDialog progressDialog = new SpotsDialog(ShowBids.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(ShowBids.this,login.class);
                            startActivity(intent);
                            finish();
                        }
                    },2000);
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private void readData(String jobId) {
        DatabaseReference bidsRef = FirebaseDatabase.getInstance().getReference("bids").child(jobId);
        bidsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot!=null){
                    Bid bid = dataSnapshot.getValue(Bid.class);
                    times.add(bid.getTimeStamp5());
                    bidIds.add(bid.getKey5());
                    bidPrices.add(bid.getBidCoins5());
                    bidDescriptions.add(bid.getDescription5());
                    bidByEmails.add(bid.getBidBy5());
                    bidstatuses.add(bid.getStatus5());

                    String currentBidBy = bid.getBidBy5();
                    readProfileImage(currentBidBy);
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

    private void readProfileImage(String currentPostedBy) {
        String user = currentPostedBy.replace(".",",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot !=null){
                    User user = dataSnapshot.getValue(User.class);
                    profileImages.add(user.getProfilePicUrl2());
                    bidBys.add(user.getFirstName2()+" "+user.getLastName2());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    class showBidsAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return bidBys.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.show_bids_list,null);
            final ImageView profileImageImageView = view.findViewById(R.id.profileImageImageView);
            final TextView bidByTextview = view.findViewById(R.id.bidBy);
            final TextView timeTextview = view.findViewById(R.id.bidTime);
            final TextView bidIdTextview = view.findViewById(R.id.bidId);
            final TextView bidByEmailTextview = view.findViewById(R.id.bidByEmail);
            final TextView bidPriceTextview = view.findViewById(R.id.bidPrice);
            final TextView bidStatusTextview = view.findViewById(R.id.bidStatus);
            final TextView bidDescriptionTextview = view.findViewById(R.id.bidDescription);
            final ButtonRectangle acceptBtn = view.findViewById(R.id.acceptBtn);
            final ButtonRectangle rejectBtn = view.findViewById(R.id.rejectBtn);

            bidByTextview.setText(bidBys.get(i));
            timeTextview.setText(times.get(i));
            bidIdTextview.setText(bidIds.get(i));
            bidByEmailTextview.setText(bidByEmails.get(i));
            bidPriceTextview.setText(bidPrices.get(i));
            bidDescriptionTextview.setText(bidDescriptions.get(i));
            bidStatusTextview.setText(bidstatuses.get(i));

            bidByEmailTextview.setVisibility(View.GONE);
            bidIdTextview.setVisibility(View.GONE);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference dateRef = storageRef.child(profileImages.get(i));
            dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri downloadUrl)
                {
//                    Glide.with(getActivity()).load(downloadUrl).into(profileImageImageView);
                    int imageWidth =1280;
                    int imageHeight = 960 ;
                    Picasso.with(ShowBids.this)
                            .load(downloadUrl)
                            .resize(imageWidth , imageHeight)
                            .centerInside()
                            .into(profileImageImageView);
                }
            });

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("jobs").child(JobId);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Job job = dataSnapshot.getValue(Job.class);
                    if (!(job.getAssignedTo().equals(""))){
                        acceptBtn.setEnabled(false);
                        rejectBtn.setEnabled(false);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            profileImageImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String UserEmail = String.valueOf(bidByEmailTextview.getText().toString().trim());
                    Intent intent = new Intent(ShowBids.this,ShowProfile.class);
                    intent.putExtra("UserEmail",UserEmail);
                    startActivity(intent);
                }
            });
            bidByTextview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String UserEmail = String.valueOf(bidByEmailTextview.getText().toString().trim());
                    Intent intent = new Intent(ShowBids.this,ShowProfile.class);
                    intent.putExtra("UserEmail",UserEmail);
                    startActivity(intent);
                }
            });

            if (bidStatusTextview.getText().equals("rejected")){
                acceptBtn.setEnabled(false);
                rejectBtn.setEnabled(false);
            }

            rejectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String bidId = String.valueOf(bidIdTextview.getText().toString().trim());

                    JSONObject jsonObject = new JSONObject();
                    try{
                        jsonObject.put("BidId",bidId);
                        jsonObject.put("JobId",JobId);

                        URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/RejectBid?text="+jsonObject.toString());
                        new AcceptBidAsync().execute(url);

                        final AlertDialog progressDialog = new SpotsDialog(ShowBids.this,R.style.Custom);
                        progressDialog.show();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                if (CloudFunctionResponse.equals("BidRejected")){
                                    Toast.makeText(ShowBids.this,"Bid has been rejected",Toast.LENGTH_SHORT).show();
                                    rejectAllOthers(bidId,JobId);
                                }
                                else {
                                    Toast.makeText(ShowBids.this,"Bid not rejected",Toast.LENGTH_SHORT).show();
                                }
                            }
                        },7000);

                    }catch (Exception e){}
                }
            });

            acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String bidId = String.valueOf(bidIdTextview.getText().toString().trim());
                    final String bidBy = String.valueOf(bidByEmailTextview.getText().toString().trim());

                    JSONObject jsonObject = new JSONObject();
                    try{
                        jsonObject.put("BidId",bidId);
                        jsonObject.put("JobId",JobId);
                        jsonObject.put("BidBy",bidBy);

                        URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/AcceptBid?text="+jsonObject.toString());
                        new AcceptBidAsync().execute(url);

                        final AlertDialog progressDialog = new SpotsDialog(ShowBids.this,R.style.Custom);
                        progressDialog.show();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                if (CloudFunctionResponse.equals("BidAccepted")){
                                    Toast.makeText(ShowBids.this,"Bid has been accepted",Toast.LENGTH_SHORT).show();
                                    rejectAllOthers(bidId,JobId);
                                }
                                else {
                                    Toast.makeText(ShowBids.this,"Bid not accepted",Toast.LENGTH_SHORT).show();
                                }
                            }
                        },10000);

                    }catch (Exception e){}
                }
            });

            if (Sponsorship.equals("Not Available")){
                acceptBtn.setEnabled(false);
                rejectBtn.setEnabled(false);
            }

            return view;
        }

        private void rejectAllOthers(String bidId, String jobId) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("BidId",bidId);
                jsonObject.put("JobId",jobId);

                URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/RejectAllOtherBids?text="+jsonObject.toString());
                Log.i("url",url.toString());
                new AcceptBidAsync().execute(url);
            }catch (Exception e){}
        }
    }
}

class AcceptBidAsync extends AsyncTask<URL,Void,Void> {
    String inputLine,inputLine2;
    boolean running = true;
    @Override
    protected void onCancelled() {
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(URL... urls) {
            try {
                URLConnection connection = urls[0].openConnection();
                connection.setConnectTimeout(10000);
                connection.getContentLength();

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));

                while ((inputLine = in.readLine()) != null) {
                    inputLine2 = inputLine;
                }

                in.close();

            } catch (Exception e) {
            }
            ShowBids.CloudFunctionResponse = inputLine2;
        return null;
    }
}