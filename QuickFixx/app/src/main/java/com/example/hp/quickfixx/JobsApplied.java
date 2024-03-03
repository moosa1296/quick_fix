package com.example.hp.quickfixx;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.quickfixx.Model.Bid;
import com.example.hp.quickfixx.Model.ImageGallery;
import com.example.hp.quickfixx.Model.Job;
import com.example.hp.quickfixx.Model.User;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.ArrayList;

import dmax.dialog.SpotsDialog;

import static com.example.hp.quickfixx.UserMainActivity.UserEmail;


/**
 * A simple {@link Fragment} subclass.
 */
public class JobsApplied extends Fragment {

    ListView listView;
    ArrayList<String> profileimages = new ArrayList<>();
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> jobimages = new ArrayList<>();
    ArrayList<String> postedBys = new ArrayList<>();
    ArrayList<String> postedTimes = new ArrayList<>();
    ArrayList<String> addresses = new ArrayList<>();
    ArrayList<String> latitudes = new ArrayList<>();
    ArrayList<String> longitudes = new ArrayList<>();
    ArrayList<String> jobids = new ArrayList<>();
    ArrayList<String> bidids = new ArrayList<>();
    ArrayList<String> coinsproposed = new ArrayList<>();
    ArrayList<String> bidssubmitted = new ArrayList<>();
    ArrayList<String> days = new ArrayList<>();
    ArrayList<String> sponsors = new ArrayList<>();
    ArrayList<String> statuses = new ArrayList<>();

    String currentJobId = null;
    String currentPostedBy = null;
    customAdapter myAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jobs_applied,container,false);
        listView = (ListView) view.findViewById(R.id.JobsAppliedListview);
        myAdapter = new customAdapter();

        readData();
        final AlertDialog progressDialog = new SpotsDialog(getActivity(),R.style.Custom);
        progressDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                listView.setAdapter(myAdapter);
            }
        },5500);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String JobId = ((TextView) view.findViewById(R.id.JobId)).getText().toString();
                Intent intent = new Intent(getActivity(),SinglePost.class);
                intent.putExtra("JobId",JobId);
                intent.putExtra("UserEmail",UserEmail);
                startActivity(intent);
            }
        });
        return view;
    }

    private void readData() {
        DatabaseReference bidRef = FirebaseDatabase.getInstance().getReference("bids");
        bidRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Bid bid = snapshot.getValue(Bid.class);
                    if (bid.getBidBy5().equals(login.email)) {
                        jobids.add(bid.getJobId5());
                        bidssubmitted.add(bid.getBidCoins5());
                        statuses.add(bid.getStatus5());
                        bidids.add(bid.getKey5());

                        currentJobId = bid.getJobId5();
                        readJobData(currentJobId);
                        readJobImage(currentJobId);
                    }
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

    private void readJobImage(String currentJobId) {
        DatabaseReference galleryRef = FirebaseDatabase.getInstance().getReference("imageGallery").child(currentJobId);
        galleryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ImageGallery imageGallery = dataSnapshot.getValue(ImageGallery.class);
                jobimages.add(imageGallery.getImageUrl1());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readJobData(final String currentJobId) {
        final DatabaseReference jobRef = FirebaseDatabase.getInstance().getReference("jobs").child(currentJobId);
        jobRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Job job = dataSnapshot.getValue(Job.class);
                titles.add(job.getJobTitle());
                postedTimes.add(job.getTimeStamp().replace(" ","  "));
                addresses.add(job.getAddress().replaceAll("[0-9]", ""));
                latitudes.add(job.getLatitude());
                longitudes.add(job.getLongitude());
                coinsproposed.add(job.getCoins());
                days.add(job.getMaxNoDays());

                String currentsponsor = job.getSponsoredBy();
                if (currentsponsor.equals("Get Sponsor")) {
                    sponsors.add("Not Available");
                } else {
                    sponsors.add("Available");
                }

                currentPostedBy = job.getPostedBy().replace(".",",");
                readProfileImage(currentPostedBy);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readProfileImage(String currentPostedBy) {
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentPostedBy);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                profileimages.add(user.getProfilePicUrl2());
                postedBys.add(user.getFirstName2()+" "+user.getLastName2());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class customAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            Log.i("size",Integer.toString(titles.size()));
            return titles.size();
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
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            view = inflater.inflate(R.layout.jobs_applied_list,null);

            final TextView titleTextview = (TextView) view.findViewById(R.id.titleTextview);
            final ImageView profileImageImageView = (ImageView) view.findViewById(R.id.profileImageImageView);
            final ImageView jobImage = (ImageView) view.findViewById(R.id.jobImage);
            final TextView postedBy = (TextView) view.findViewById(R.id.postedBy);
            final TextView postedTime = (TextView) view.findViewById(R.id.postedTime);
            final TextView address = (TextView) view.findViewById(R.id.address);
            final TextView latitude = (TextView) view.findViewById(R.id.latitude);
            final TextView longitude = (TextView) view.findViewById(R.id.longitude);
            final TextView JobId = (TextView) view.findViewById(R.id.JobId);
            final TextView BidId = (TextView) view.findViewById(R.id.BidId);
            final TextView coinsProposed = (TextView) view.findViewById(R.id.coinsTextview);
            final TextView bidTextview = (TextView) view.findViewById(R.id.bidTextview);
            final TextView Maxdays = (TextView) view.findViewById(R.id.daysTextview);
            final TextView Sponsorship = (TextView) view.findViewById(R.id.sponsorTextview);
            final ImageView showOnMaps = (ImageView) view.findViewById(R.id.showOnMaps);
            final TextView jobStatus = (TextView) view.findViewById(R.id.jobstatus);
            final ButtonRectangle ProgressBtn = (ButtonRectangle) view.findViewById(R.id.progressBtn);
            final ButtonRectangle VerifyBtn = (ButtonRectangle) view.findViewById(R.id.verifyBtn);


            titleTextview.setText(titles.get(i));
            postedBy.setText(postedBys.get(i));
            postedTime.setText(postedTimes.get(i));
            address.setText(addresses.get(i));
            latitude.setText(latitudes.get(i));
            longitude.setText(longitudes.get(i));
            JobId.setText(jobids.get(i));
            BidId.setText(bidids.get(i));
            coinsProposed.setText(coinsproposed.get(i));
            bidTextview.setText(bidssubmitted.get(i));
            Maxdays.setText(days.get(i));
            Sponsorship.setText(sponsors.get(i));
            jobStatus.setText(statuses.get(i));


            ProgressBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(),JobProgress.class);
                    intent.putExtra("JobId",String.valueOf(JobId.getText().toString().trim()));
                    intent.putExtra("JobTitle",String.valueOf(titleTextview.getText().toString().trim()));
                    startActivity(intent);
                }
            });
            VerifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("JobId",String.valueOf(JobId.getText().toString().trim()));
                        jsonObject.put("BidId",String.valueOf(BidId.getText().toString().trim()));

                        URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/SubmitForVerification?text="+jsonObject.toString());
                        new AsyncSubmitForVerification().execute(url);
                        final AlertDialog progressDialog = new SpotsDialog(getActivity(),R.style.Custom);
                        progressDialog.show();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                if (JobsDetails.JobAppliedResponse.equals("Done")){
                                    Toast.makeText(getActivity(),"Submitted for verification",Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getActivity(),"Submission failed",Toast.LENGTH_SHORT).show();
                                }
                            }
                        },5000);
                    }catch (Exception e){}

                }
            });


            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference dateRef = storageRef.child(profileimages.get(i));
            dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri downloadUrl)
                {
//                    Glide.with(getActivity()).load(downloadUrl).into(profileImageImageView);
                    int imageWidth =1280;
                    int imageHeight = 960 ;
                    Picasso.with(getActivity())
                            .load(downloadUrl)
                            .resize(imageWidth , imageHeight)
                            .centerInside()
                            .into(profileImageImageView);
                }
            });
            StorageReference dateReff = storageRef.child(jobimages.get(i));
            dateReff.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri downloadUrl)
                {
//                    Glide.with(getActivity()).load(downloadUrl).into(jobImage);
                    int imageWidth =1280;
                    int imageHeight = 960 ;
                    Picasso.with(getActivity())
                            .load(downloadUrl)
                            .resize(imageWidth , imageHeight)
                            .centerInside()
                            .into(jobImage);
                }
            });

            showOnMaps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String latitudee = String.valueOf(latitude.getText().toString().trim());
                    final String longitudee = String.valueOf(longitude.getText().toString().trim());

                    final AlertDialog progessDialog = new SpotsDialog(getActivity(),R.style.Custom);
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

            //making JobId,BidId,latitude,longitude invisible
            JobId.setVisibility(View.GONE);
            BidId.setVisibility(View.GONE);
            latitude.setVisibility(View.GONE);
            longitude.setVisibility(View.GONE);

            if (!jobStatus.getText().equals("accepted")){
                ProgressBtn.setEnabled(false);
                VerifyBtn.setEnabled(false);
            }

            return view;


        }
    }
}
class AsyncSubmitForVerification extends AsyncTask<URL,Void,Void> {
    public  String response;
    public  String saveResponse;

    @Override
    protected void onPostExecute(Void aVoid)
    {

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
        JobsDetails.JobAppliedResponse = saveResponse;
        return null;
    }
}