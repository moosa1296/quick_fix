package com.example.hp.quickfixx;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import java.util.ArrayList;

import dmax.dialog.SpotsDialog;

import static com.example.hp.quickfixx.UserMainActivity.UserEmail;


/**
 * A simple {@link Fragment} subclass.
 */
public class JobsSponsored extends Fragment {

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
    ArrayList<String> coinsproposed = new ArrayList<>();
    ArrayList<String> days = new ArrayList<>();
    ArrayList<String> sponsors = new ArrayList<>();

    String currentJobId = null;
    String currentPostedBy = null;

    customAdapter myadaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_jobs_sponsored,container,false);

        listView = (ListView) view.findViewById(R.id.JobsSponsoredListview);
        myadaptor = new customAdapter();
        readData();

        //for removing replication
        profileimages.clear(); titles.clear(); jobimages.clear(); postedBys.clear(); postedTimes.clear(); addresses.clear(); latitudes.clear();
        longitudes.clear(); jobids.clear(); coinsproposed.clear(); days.clear(); sponsors.clear();

        final AlertDialog progressDialog = new SpotsDialog(getActivity(),R.style.Custom);
        progressDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                listView.setAdapter(myadaptor);
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
        DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs");
        jobsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot !=null){
                    Job job = dataSnapshot.getValue(Job.class);
                    if (job.getVisible().equals("1")) {
                        if (job.getSponsoredBy().equals(UserEmail) && !job.getPostedBy().equals(UserEmail)) {
                            titles.add(job.getJobTitle());
                            jobids.add(job.getJobId());
                            coinsproposed.add(job.getCoins());
                            days.add(job.getMaxNoDays());
                            latitudes.add(job.getLatitude());
                            longitudes.add(job.getLongitude());

                            //check sponsorship
                            String currentsponsor = job.getSponsoredBy();
                            if (currentsponsor.equals("Get Sponsor")) {
                                sponsors.add("Not Available");
                            } else {
                                sponsors.add("Available");
                            }

                            //replace integers
                            String address = job.getAddress().replaceAll("[0-9]", "");
                            addresses.add(address);

                            //space b/w timestamp
                            String postedtime = job.getTimeStamp().replace(" ", "   ");
                            postedTimes.add(postedtime);

                            currentJobId = job.getJobId();
                            currentPostedBy = job.getPostedBy();
                            readJobImage(currentJobId);
                            readProfileImage(currentPostedBy);
                        }
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

    private void readProfileImage(String currentPostedBy) {
        String user = currentPostedBy.replace(".",",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot !=null){
                    User user = dataSnapshot.getValue(User.class);
                    profileimages.add(user.getProfilePicUrl2());
                    postedBys.add(user.getFirstName2()+" "+user.getLastName2());
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
                    jobimages.add(imageGallery.getImageUrl1());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    class customAdapter extends BaseAdapter {

        @Override
        public int getCount() {
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
            view = inflater.inflate(R.layout.jobs_sponsored_list,null);

            final TextView titleTextview = (TextView) view.findViewById(R.id.titleTextview);
            final ImageView profileImageImageView = (ImageView) view.findViewById(R.id.profileImageImageView);
            final ImageView jobImage = (ImageView) view.findViewById(R.id.jobImage);
            final TextView postedBy = (TextView) view.findViewById(R.id.postedBy);
            final TextView postedTime = (TextView) view.findViewById(R.id.postedTime);
            final TextView address = (TextView) view.findViewById(R.id.address);
            final TextView latitude = (TextView) view.findViewById(R.id.latitude);
            final TextView longitude = (TextView) view.findViewById(R.id.longitude);
            final TextView JobId = (TextView) view.findViewById(R.id.JobId);
            final TextView coinsProposed = (TextView) view.findViewById(R.id.coinsTextview);
            final TextView Maxdays = (TextView) view.findViewById(R.id.daysTextview);
            final TextView Sponsorship = (TextView) view.findViewById(R.id.sponsorTextview);
            final ImageView showOnMaps = (ImageView) view.findViewById(R.id.showOnMaps);
            final ButtonRectangle progressBtn = (ButtonRectangle) view.findViewById(R.id.progressBtn);

            titleTextview.setText(titles.get(i));
            postedBy.setText(postedBys.get(i));
            postedTime.setText(postedTimes.get(i));
            address.setText(addresses.get(i));
            latitude.setText(latitudes.get(i));
            longitude.setText(longitudes.get(i));
            JobId.setText(jobids.get(i));
            coinsProposed.setText(coinsproposed.get(i));
            Maxdays.setText(days.get(i));
            Sponsorship.setText(sponsors.get(i));

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

            progressBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(),JobProgress.class);
                    intent.putExtra("JobId",String.valueOf(JobId.getText().toString().trim()));
                    intent.putExtra("JobTitle",String.valueOf(titleTextview.getText().toString().trim()));
                    startActivity(intent);
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

            //making JobId,latitude,longitude invisible
            JobId.setVisibility(View.GONE);
            latitude.setVisibility(View.GONE);
            longitude.setVisibility(View.GONE);

            return view;


        }
    }

}
