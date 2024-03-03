package com.example.hp.quickfixx;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.hp.quickfixx.Model.Job;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.hp.quickfixx.UserMainActivity.UserEmail;


/**
 * A simple {@link Fragment} subclass.
 */
public class Ratings extends Fragment {

    ArrayList<String> jobTitles = new ArrayList<>();
    ArrayList<String> times = new ArrayList<>();
    ArrayList<String> jobIds = new ArrayList<>();
    ArrayList<String> ratings = new ArrayList<>();
    ArrayList<String> statuses = new ArrayList<>();

    customAdapter myAdapter;
    ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ratings, container, false);

        myAdapter = new customAdapter();
        listview = (ListView) view.findViewById(R.id.ratings_listview);

        readData(UserMainActivity.UserEmail);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listview.setAdapter(myAdapter);
            }
        },5000);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

    private void readData(final String userEmail) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference jobsRef = firebaseDatabase.getReference("jobs");
        jobsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot!=null){
                    Job job = dataSnapshot.getValue(Job.class);
                    if (job.getAssignedTo().equals(userEmail)){
                        jobTitles.add(job.getJobTitle());
                        times.add(job.getTimeStamp());
                        jobIds.add(job.getJobId());
                        if (job.getRatings().equals("waiting")){
                            ratings.add("0");
                            statuses.add("In Progress");
                        }else {
                            ratings.add(job.getRatings());
                            statuses.add("Completed");
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
            view = inflater.inflate(R.layout.ratings_list,null);

            final TextView titleTextview = (TextView) view.findViewById(R.id.titleTextview);
            final TextView time = (TextView) view.findViewById(R.id.time);
            final TextView JobId = (TextView) view.findViewById(R.id.JobId);
            final TextView status = (TextView) view.findViewById(R.id.status);
            final RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);

            titleTextview.setText(jobTitles.get(i));
            time.setText(times.get(i));
            JobId.setText(jobIds.get(i));
            status.setText(statuses.get(i));
            ratingBar.setRating(Float.parseFloat(ratings.get(i)));


            JobId.setVisibility(View.GONE);
            ratingBar.setFocusable(false);

            return view;
        }
    }
}
