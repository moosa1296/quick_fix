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
import android.widget.ListView;
import android.widget.TextView;

import com.example.hp.quickfixx.Model.ImageGallery;
import com.example.hp.quickfixx.Model.Job;
import com.example.hp.quickfixx.Model.Verification;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class JobsVerified extends Fragment {

    ListView listView;
    ArrayList<String> jobImages = new ArrayList<>();
    ArrayList<String> jobTitles = new ArrayList<>();
    ArrayList<String> keys = new ArrayList<>();

    customAdapter myAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_jobs_verified,container,false);
        listView = (ListView) view.findViewById(R.id.volunteer_jobs_verifiedListview);
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
                String key = ((TextView) view.findViewById(R.id.key)).getText().toString();
                Intent intent = new Intent(getActivity(),VolunteerSinglePost.class);
                intent.putExtra("key",key);
                intent.putExtra("status","hide");
                startActivity(intent);
            }
        });
        return view;
    }

    private void readData() {
        DatabaseReference verificationsRef = FirebaseDatabase.getInstance().getReference("verifications");
        verificationsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot!=null){
                    Verification verification = dataSnapshot.getValue(Verification.class);
                    if (verification.getStatus().equals("verified")){
                        keys.add(verification.getKey11());
                        String currentjobid = verification.getJobId11();
                        readJobImage(currentjobid);
                        readJobTitle(currentjobid);
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

    private void readJobTitle(String currentjobid) {
        DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs").child(currentjobid);
        jobsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    Job job = dataSnapshot.getValue(Job.class);
                    jobTitles.add(job.getJobTitle());
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
                    jobImages.add(imageGallery.getImageUrl1());
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
            return jobTitles.size();
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
            if (view==null){
                view = inflater.inflate(R.layout.volunteer_home_list,null);
            }

            final TextView jobTitle = (TextView) view.findViewById(R.id.titleTextview);
            final TextView key = (TextView) view.findViewById(R.id.key);
            final ImageView jobImage = (ImageView) view.findViewById(R.id.jobImage);

            jobTitle.setText(jobTitles.get(i));
            key.setText(keys.get(i));

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference dateRef = storageRef.child(jobImages.get(i));
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
                            .into(jobImage);
                }
            });

            //making key invisible
            key.setVisibility(View.GONE);

            return view;
        }
    }

}
