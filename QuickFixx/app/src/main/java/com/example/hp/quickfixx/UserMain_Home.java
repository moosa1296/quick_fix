package com.example.hp.quickfixx;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.hp.quickfixx.Model.Bookmark;
import com.example.hp.quickfixx.Model.ImageGallery;
import com.example.hp.quickfixx.Model.Job;
import com.example.hp.quickfixx.Model.Notification;
import com.example.hp.quickfixx.Model.User;
import com.example.hp.quickfixx.Model.Voting;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.os.Handler;
import android.widget.Toast;

import dmax.dialog.SpotsDialog;

import static com.example.hp.quickfixx.UserMainActivity.UserEmail;

public class UserMain_Home extends Fragment {

    ArrayList<String> profileimages = new ArrayList<>();
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> jobimages = new ArrayList<>();
    ArrayList<String> postedBys = new ArrayList<>();
    ArrayList<String> postedTimes = new ArrayList<>();
    ArrayList<String> addresses = new ArrayList<>();
    ArrayList<String> jobids = new ArrayList<>();
    ArrayList<String> totalvoteups = new ArrayList<>();
    ArrayList<String> totalvotedowns = new ArrayList<>();
    ArrayList<String> coinsproposed = new ArrayList<>();
    ArrayList<String> days = new ArrayList<>();
    ArrayList<String> sponsors = new ArrayList<>();
    ArrayList<String> assigners = new ArrayList<>();
    ArrayList<String> postedBysEmail = new ArrayList<>();
    ArrayList<String> latitudes = new ArrayList<>();
    ArrayList<String> longitudes = new ArrayList<>();
    ArrayList<String> comments = new ArrayList<>();
    ArrayList<String> votesupjobids = new ArrayList<>();
    ArrayList<String> votesdownjobids = new ArrayList<>();
    ArrayList<String> bookmarksjobids = new ArrayList<>();
    String currentJobId = null;
    String currentPostedBy = null;
    ListView listView;
    static String result;
    customAdapter myadaptor;

    FirebaseUser firebaseUser;

    ImageView profileImageImageView;
    TextView userName,numberofnotifications;
    LinearLayout newPostLinearLyout;
    SearchableSpinner searchableSpinner;
    static String cloudFunctionResponse = "";
    String UserName = "";
    String profilePicRef = "";
    String profilePicUrl = "";
    static String last_seen = "";
    int notifications_number = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_main__home,container,false);
        listView = (ListView)view.findViewById(R.id.user_main_homeListview);
        profileImageImageView = (ImageView) view.findViewById(R.id.profileImageImageView);
        userName = (TextView) view.findViewById(R.id.userName);
//        numberofnotifications = (TextView) view.findViewById(R.id.numberofnotifications);
        newPostLinearLyout = (LinearLayout) view.findViewById(R.id.newPostLinearLayout);
        searchableSpinner = (SearchableSpinner) view.findViewById(R.id.spinner);

        String[] cities=getResources().getStringArray(R.array.array_cities);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_layout,R.id.text, cities);
        searchableSpinner.setAdapter(adapter);
        searchableSpinner.setTitle("Select City");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(UserMainActivity.UserEmail.replace(".",","));
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    User user = dataSnapshot.getValue(User.class);
                    last_seen = user.getLast_seen();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        readNotifications();
        readnewPostProfileImage(UserEmail);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myadaptor = new customAdapter();

//        readData();
//        readBookmarks(UserMainActivity.UserEmail);
        final AlertDialog progressDialog = new SpotsDialog(getActivity(),R.style.Custom);
        progressDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                listView.setAdapter(myadaptor);

                userName.setText(UserName);

                if (profilePicRef.equals("")){
                    Toast.makeText(getActivity(),"Internet connection is slow",Toast.LENGTH_LONG).show();
                    Intent intent1 = new Intent(getActivity(),UserMainActivity.class);
                    startActivity(intent1);
                }else {
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    StorageReference dateRef = storageRef.child(profilePicRef);
                    dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                         profilePicUrl = uri.toString();
                         Picasso.with(getActivity())
                                 .load(uri)
                                 .resize(1280,960)
                                 .centerInside()
                                 .into(profileImageImageView);

                        }
                });
                }

            }
        },5000);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!firebaseUser.isEmailVerified()){
                    Toast.makeText(getActivity(),"Verify Email from settings for using this feature",Toast.LENGTH_SHORT).show();
                }
                else{
                    String JobId = ((TextView) view.findViewById(R.id.JobId)).getText().toString();
                    Intent intent = new Intent(getActivity(),SinglePost.class);
                    intent.putExtra("JobId",JobId);
                    intent.putExtra("UserEmail",UserEmail);
                    startActivity(intent);
                }
            }
        });

        newPostLinearLyout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!firebaseUser.isEmailVerified()){
                    Toast.makeText(getActivity(),"Verify Email from settings for using this feature",Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intent = new Intent(getActivity(),MapsActivity.class);
                    startActivity(intent);
                }
            }
        });

        searchableSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = searchableSpinner.getSelectedItem().toString();
                if (selectedItem.equals("ShowAll")){
                    profileimages.clear(); titles.clear(); jobimages.clear(); postedBys.clear(); postedTimes.clear(); addresses.clear(); latitudes.clear();
                    longitudes.clear(); jobids.clear(); totalvoteups.clear(); totalvotedowns.clear(); coinsproposed.clear(); days.clear(); sponsors.clear();
                    votesdownjobids.clear(); votesupjobids.clear(); assigners.clear(); postedBysEmail.clear(); bookmarksjobids.clear();
                    readData();
                    readBookmarks(UserMainActivity.UserEmail);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listView.setAdapter(new customAdapter());
//                            numberofnotifications.setText(Integer.toString(notifications_number));
                            notifications_number = 0;
                        }
                    },5000);
                }else {
                    profileimages.clear(); titles.clear(); jobimages.clear(); postedBys.clear(); postedTimes.clear(); addresses.clear(); latitudes.clear();
                    longitudes.clear(); jobids.clear(); totalvoteups.clear(); totalvotedowns.clear(); coinsproposed.clear(); days.clear(); sponsors.clear();
                    votesdownjobids.clear(); votesupjobids.clear(); assigners.clear(); postedBysEmail.clear(); bookmarksjobids.clear();
                    readData2(selectedItem);
                    readBookmarks(UserMainActivity.UserEmail);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listView.setAdapter(new customAdapter());
//                            numberofnotifications.setText(Integer.toString(notifications_number));
                            notifications_number = 0;
                        }
                    },5000);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }



    private void readData2(final String selectedItem) {
        DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs");
        jobsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot !=null){
                    Job job = dataSnapshot.getValue(Job.class);
                    if (job.getVisible().equals("1")) {
                        if (!(job.getPostedBy().equals(UserEmail))){
                            Log.i("job.getAdress",job.getAddress().toLowerCase());
                            Log.i("selectedItem",selectedItem.toLowerCase());
                            if (job.getAddress().toLowerCase().contains(selectedItem.toLowerCase())){
                                titles.add(job.getJobTitle());
                                jobids.add(job.getJobId());
                                totalvoteups.add(job.getVoteUp());
                                totalvotedowns.add(job.getVoteDown());
                                coinsproposed.add(job.getCoins());
                                days.add(job.getMaxNoDays());
                                latitudes.add(job.getLatitude());
                                longitudes.add(job.getLongitude());

                                //check sponsorship
                                String currentsponsor = job.getSponsoredBy();
                                if (currentsponsor.equals("Get Sponsor")){
                                    sponsors.add("Not Available");
                                }
                                else {
                                    sponsors.add("Available");
                                }
                                String currentAssigner = job.getAssignedTo();
                                if (currentAssigner.equals("")){
                                    assigners.add("Not Available");
                                }else {
                                    readName(currentAssigner);
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
                                readVote(currentJobId);
                                readComment(currentJobId);
                            }
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

    private void readComment(String currentJobId) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(currentJobId);
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                comments.add(Long.toString(count));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readBookmarks(String userEmail) {
        String user = userEmail.replace(".",",");
        DatabaseReference bookmarksRef = FirebaseDatabase.getInstance().getReference("bookmarks").child(user);
        bookmarksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot!=null){
                    Bookmark bookmark = dataSnapshot.getValue(Bookmark.class);
                    bookmarksjobids.add(bookmark.getJobId8());
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

    private void readnewPostProfileImage(String userEmail) {
        String user = userEmail.replace(".",",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                profilePicRef = user.getProfilePicUrl2();
                UserName = user.getFirstName2()+" "+user.getLastName2();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readData() {
        DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs");
        jobsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot !=null){
                    Job job = dataSnapshot.getValue(Job.class);
                    if (job.getVisible().equals("1")) {
                        if (!(job.getPostedBy().equals(UserEmail))){
                            titles.add(job.getJobTitle());
                            jobids.add(job.getJobId());
                            totalvoteups.add(job.getVoteUp());
                            totalvotedowns.add(job.getVoteDown());
                            coinsproposed.add(job.getCoins());
                            days.add(job.getMaxNoDays());
                            latitudes.add(job.getLatitude());
                            longitudes.add(job.getLongitude());

                            //check sponsorship
                            String currentsponsor = job.getSponsoredBy();
                            if (currentsponsor.equals("Get Sponsor")){
                                sponsors.add("Not Available");
                            }
                            else {
                                sponsors.add("Available");
                            }
                            String currentAssigner = job.getAssignedTo();
                            if (currentAssigner.equals("")){
                                assigners.add("Not Available");
                            }else {
                                readName(currentAssigner);
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
                            readVote(currentJobId);
                            readComment(currentJobId);
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

    private void readNotifications() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference notificationsRef = firebaseDatabase.getReference("notifications").child(UserMainActivity.UserEmail.replace(".",","));
        notificationsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    Notification notification = dataSnapshot.getValue(Notification.class);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date last_seenn = sdf.parse(last_seen);
                        Date notification_time = sdf.parse(notification.getTimeStamp());

                        if (last_seenn.compareTo(notification_time)<=0){
                            notifications_number++;
                            Log.i("notification number",Integer.toString(notifications_number));
                        }
                        else {
                            Log.i("not","not");
                        }

                    }catch (Exception e){}
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
                    postedBysEmail.add(user.getEmail2());
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
                    assigners.add(user.getFirstName2()+" "+user.getLastName2());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void readJobImage(String currentJobID) {
        DatabaseReference imageGalleryRef = FirebaseDatabase.getInstance().getReference("imageGallery").child(currentJobID);
        imageGalleryRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
    private void readVote(final String currentJobID){
        final DatabaseReference voting = FirebaseDatabase.getInstance().getReference("voting");
        voting.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(currentJobID)){
                    DatabaseReference ref = voting.child(currentJobID);
                    ref.addChildEventListener(new ChildEventListener(){
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Voting voting = dataSnapshot.getValue(Voting.class);
                            if(voting.getUserEmail().equals(UserEmail)){
                                if(voting.getVote().equals("1")){
                                    votesupjobids.add(currentJobID);
                                }
                                else {
                                    votesdownjobids.add(currentJobID);
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class customAdapter extends BaseAdapter{

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
            if (view==null){
                view = inflater.inflate(R.layout.user_home_list,null);
            }

            final TextView titleTextview = (TextView) view.findViewById(R.id.titleTextview);
            final ImageView profileImageImageView = (ImageView) view.findViewById(R.id.profileImageImageView);
            final ImageView jobImage = (ImageView) view.findViewById(R.id.jobImage);
            final TextView postedBy = (TextView) view.findViewById(R.id.postedBy);
            final TextView postedTime = (TextView) view.findViewById(R.id.postedTime);
            final TextView address = (TextView) view.findViewById(R.id.address);
            final TextView JobId = (TextView) view.findViewById(R.id.JobId);
            final TextView postedByEmail = (TextView) view.findViewById(R.id.postedByEmail);
            final TextView coinsProposed = (TextView) view.findViewById(R.id.coinsTextview);
            final TextView Maxdays = (TextView) view.findViewById(R.id.daysTextview);
            final TextView Sponsorship = (TextView) view.findViewById(R.id.sponsorTextview);
            final TextView AssignedTo = (TextView) view.findViewById(R.id.assignedToTextview);
            final TextView totalVoteUp = (TextView) view.findViewById(R.id.totalVoteUp);
            final TextView totalVoteDown = (TextView) view.findViewById(R.id.totalVoteDown);
            final TextView totalComments = (TextView) view.findViewById(R.id.totalComments);
            final LinearLayout voteUp = (LinearLayout) view.findViewById(R.id.voteUp);
            final LinearLayout voteDown = (LinearLayout) view.findViewById(R.id.voteDown);
            final LinearLayout showAllVotes = (LinearLayout) view.findViewById(R.id.showAllVotes);
            final ImageView bookmark = (ImageView) view.findViewById(R.id.bookmark);
            final EditText addNewComment = (EditText) view.findViewById(R.id.addNewComment);
            final EditText reportJob = (EditText) view.findViewById(R.id.reportJob);

            titleTextview.setText(titles.get(i));
            postedBy.setText(postedBys.get(i));
            postedTime.setText(postedTimes.get(i));
            address.setText(addresses.get(i));
            JobId.setText(jobids.get(i));
            postedByEmail.setText(postedBysEmail.get(i));
            coinsProposed.setText(coinsproposed.get(i));
            Maxdays.setText(days.get(i));
            Sponsorship.setText(sponsors.get(i));
            AssignedTo.setText(assigners.get(i));
            totalVoteUp.setText(totalvoteups.get(i));
            totalVoteDown.setText(totalvotedowns.get(i));
            totalComments.setText(comments.get(i));

            postedByEmail.setVisibility(View.GONE);

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

            jobImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String jobId = (JobId).getText().toString();
                    Intent intent = new Intent(getActivity(),SinglePost.class);
                    intent.putExtra("JobId",jobId);
                    intent.putExtra("UserEmail",UserEmail);
                    startActivity(intent);
                }
            });

            addNewComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(),Comments.class);
                    intent.putExtra("JobId",String.valueOf(JobId.getText().toString().trim()));
                    startActivity(intent);
                }
            });
            reportJob.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    final EditText editText = new EditText(getActivity());
                    editText.setHint("Enter Reason");

                    LinearLayout linearLayout = new LinearLayout(getActivity());
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.addView(editText);

                    alertDialog.setTitle("Report Job");
                    alertDialog.setView(linearLayout);

                    final AlertDialog progressDialog = new SpotsDialog(getActivity(),R.style.Custom);

                    alertDialog.setPositiveButton("REPORT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            progressDialog.show();

                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("reportBy",UserMainActivity.UserEmail.replace(".",","));
                                jsonObject.put("reason",editText.getText().toString().trim());
                                jsonObject.put("JobId",String.valueOf(JobId.getText().toString().trim()));

                                URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/ReportJob?text="+jsonObject.toString());
                                new Asynctask().execute(url);
                                final Handler handler1 = new Handler();
                                handler1.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        if (result.equals("job reported")){
                                            Toast.makeText(getActivity(),"Job reported successfully",Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(getActivity(),"Job reporting failed",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                },7000);
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

            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog progressDialog = new SpotsDialog(getActivity(),R.style.Custom);
                    progressDialog.show();
                    String jobId = String.valueOf(JobId.getText().toString().trim());
                    String bookmarkBy = UserMainActivity.UserEmail;

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("JobId",jobId);
                        jsonObject.put("bookmarkBy",bookmarkBy.replace(".",","));

                        URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/BookmarkPost?text="+jsonObject.toString());
                        Log.i("url",url.toString());
                        final Asynctask asynctask = new Asynctask();
                        asynctask.execute(url);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(UserMain_Home.result.equals("bookmark Successful")){
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(),"bookmark added",Toast.LENGTH_SHORT).show();
                                    bookmark.setImageResource(R.drawable.bookmark_icon2);
                                }
                                else{
                                    progressDialog.dismiss();
                                    asynctask.cancel(true);
                                    Toast.makeText(getActivity(),"Error,bookmark not added",Toast.LENGTH_SHORT).show();
                                }
                            }
                        },5000);


                    }catch (Exception e){}
                }
            });
            for (int j=0;j<bookmarksjobids.size();j++){
                if (JobId.getText().equals(bookmarksjobids.get(j))){
                    bookmark.setImageResource(R.drawable.bookmark_icon2);
                    bookmark.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog("Alert Message","Bookmark already added");
                        }
                    });
                }
            }

            for (int j=0;j<votesupjobids.size();j++){
                if (JobId.getText().equals(votesupjobids.get(j))){
                    voteUp.setBackgroundColor(Color.parseColor("#1E88E5"));
                    voteUp.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            AlertDialog("Alert Message","You already have casted your vote for this issue");
                            return true;
                        }
                    });
                    voteDown.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            AlertDialog("Alert Message","You already have casted your vote for this issue");
                            return true;
                        }
                    });
                }
            }
            for(int j=0;j<votesdownjobids.size();j++){
                if(JobId.getText().equals(votesdownjobids.get(j))){
                    voteDown.setBackgroundColor(Color.parseColor("#1E88E5"));
                    voteUp.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            AlertDialog("Alert Message","You already have casted your vote for this issue");
                            return true;
                        }
                    });
                    voteDown.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            AlertDialog("Alert Message","You already have casted your vote for this issue");
                            return true;
                        }
                    });
                }
            }

            voteUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String JobbId = String.valueOf(JobId.getText().toString());
                    String totalVoteUpp = String.valueOf(totalVoteUp.getText().toString());
                    String postedBy = String.valueOf(postedByEmail.getText().toString());

                    final AlertDialog progressDialog = new SpotsDialog(getActivity(),R.style.Custom);
                    progressDialog.show();
                    JSONObject jobj = new JSONObject();
                    try{
                        jobj.put("JobId",JobbId);
                        jobj.put("voteUp",totalVoteUpp);
                        jobj.put("UserEmail",UserEmail);
                        jobj.put("postedByEmail",postedBy);
                        jobj.put("replace",postedBy.replace(".",","));

                        URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/VoteUp?text="+jobj.toString());
                        Log.i("url",url.toString());
                        final Asynctask asynctask = new Asynctask();
                        asynctask.execute(url);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(UserMain_Home.result.equals("ok")){
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(),"Vote has been casted",Toast.LENGTH_SHORT).show();
                                    voteUp.setBackgroundColor(Color.parseColor("#1E88E5"));
                                    int voteUps = Integer.parseInt(totalVoteUp.getText().toString().trim())+1;
                                    totalVoteUp.setText(Integer.toString(voteUps));
                                }
                                else{
                                    progressDialog.dismiss();
                                    asynctask.cancel(true);
                                    Toast.makeText(getActivity(),"Error,Vote not casted",Toast.LENGTH_SHORT).show();
                                }
                            }
                        },8000);
                    }
                    catch (Exception e){}
                }
            });
            voteDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String JobbId = String.valueOf(JobId.getText().toString());
                    String totalVoteDownn = String.valueOf(totalVoteDown.getText().toString());
                    String postedBy = String.valueOf(postedByEmail.getText().toString());

                    final AlertDialog progressDialog = new SpotsDialog(getActivity(),R.style.Custom);
                    progressDialog.show();
                    JSONObject jobj = new JSONObject();
                    try{
                        jobj.put("JobId",JobbId);
                        jobj.put("voteDown",totalVoteDownn);
                        jobj.put("UserEmail",UserEmail);
                        jobj.put("postedByEmail",postedBy);
                        jobj.put("replace",postedBy.replace(".",","));


                        URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/VoteDown?text="+jobj.toString());
                        Log.i("url",url.toString());
                        final Asynctask asynctask = new Asynctask();
                        asynctask.execute(url);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(UserMain_Home.result.equals("ok")){
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(),"Vote has been casted",Toast.LENGTH_SHORT).show();
                                    voteDown.setBackgroundColor(Color.parseColor("#1E88E5"));
                                    int voteDowns = Integer.parseInt(totalVoteDown.getText().toString().trim())+1;
                                    totalVoteDown.setText(Integer.toString(voteDowns));
                                }
                                else{
                                    progressDialog.dismiss();
                                    asynctask.cancel(true);
                                    Toast.makeText(getActivity(),"Error,Vote not casted",Toast.LENGTH_SHORT).show();
                                }
                            }
                        },5000);
                    }
                    catch (Exception e){}
                }
            });
            showAllVotes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String JobbId = String.valueOf(JobId.getText().toString());
                    Intent intent = new Intent(getActivity(),ShowAllVotes.class);
                    intent.putExtra("JobId",JobbId);
                    startActivity(intent);
                }
            });

            profileImageImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String UserEmail = String.valueOf(postedByEmail.getText().toString().trim());
                    Intent intent = new Intent(getActivity(),ShowProfile.class);
                    intent.putExtra("UserEmail",UserEmail);
                    startActivity(intent);
                }
            });
            postedBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String UserEmail = String.valueOf(postedByEmail.getText().toString().trim());
                    Intent intent = new Intent(getActivity(),ShowProfile.class);
                    intent.putExtra("UserEmail",UserEmail);
                    startActivity(intent);
                }
            });

            return view;
        }

        private void AlertDialog(String title,String message){
            final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(title);
            alertDialog.setMessage(message);

            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }
}
class Asynctask extends AsyncTask<URL,Void,Void>{
    String inputLine,inputLine2;
    boolean running = true;
    @Override
    protected void onCancelled() {
        running = false;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(URL... urls) {
        while (running) {
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
            UserMain_Home.result = inputLine2;
            running = false;
        }
        return null;
    }
}

/*class AsyncUsersRetreival extends AsyncTask<URL,Void,Void>{
    String inputLine;
    String inputLine2;
    @Override
    protected void onPostExecute(Void aVoid) {
        List<User> users = new ArrayList<>();
        Log.i("andar","aya3");
        try {
            JSONArray jsonArray = new JSONArray(UserMain_Home.Usersdata);
            Log.i("andar","aya3.1");
            Log.i("length",Integer.toString(jsonArray.length()));
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                User user = new User();
                user.setAddress(jsonObject.getString("Address"));
                user.setEmail(jsonObject.getString("Email"));
                user.setFirstName(jsonObject.getString("FirstName"));
                user.setLastName(jsonObject.getString("LastName"));
                user.setPhone(jsonObject.getString("Phone"));
                user.setProfilePicUrl(jsonObject.getString("ProfilePicUrl"));
                user.setType(jsonObject.getString("Type"));
                user.setVerificationStatus(jsonObject.getString("VerificationStatus"));
                user.setKey(jsonObject.getString("key"));

                users.add(user);
            }
            Log.i("users",Integer.toString(users.size()));
            Log.i("jobsEmails",Integer.toString(UserMain_Home.jobsEmails.size()));
            for(int j=0;j<users.size();j++){
                for(int z=0;z<UserMain_Home.jobsEmails.size();z++){
                    if(users.get(j).getEmail()==UserMain_Home.jobsEmails.get(z)){
                        UserMain_Home.ProfilePicUrls.add(users.get(j).getProfilePicUrl());
                        Log.i("aya","ji");
                    }
                }
            }
            for (int z=0;z<UserMain_Home.ProfilePicUrls.size();z++){
                Log.i("data"+z,UserMain_Home.ProfilePicUrls.get(z));
            }

        }catch (Exception e){}
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

            while ((inputLine = in.readLine()) != null){
                inputLine2 = inputLine;
            }

            in.close();

        }catch (Exception e){}
        UserMain_Home.Usersdata = inputLine2;
        Log.i("andar","aya2");
        return null;
    }
}

class AsyncJobsRetreival extends AsyncTask<URL,Void,Void>{
    String inputLine;
    String inputLine2;
    @Override
    protected Void doInBackground(URL... urls) {
        try {
            URLConnection connection = urls[0].openConnection();
            connection.setConnectTimeout(7000);
            connection.getContentLength();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            while ((inputLine = in.readLine()) != null){
                inputLine2 = inputLine;
            }

            in.close();

        }catch (Exception e){}
        UserMain_Home.Jobsdata = inputLine2;
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        List<Job> jobs = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(UserMain_Home.Jobsdata);
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Job job = new Job();
                job.setJobId(jsonObject.getString("JobId"));
                job.setAddress(jsonObject.getString("address"));
                job.setAssignedTo(jsonObject.getString("assignedTo"));
                job.setCoins(jsonObject.getString("coins"));
                job.setJobDescription(jsonObject.getString("jobDescription"));
                job.setJobTitle(jsonObject.getString("jobTitle"));
                job.setLatitude(jsonObject.getString("latitude"));
                job.setLongitude(jsonObject.getString("longitude"));
                job.setMaxNoDays(jsonObject.getString("maxNoDays"));
                job.setPostedBy(jsonObject.getString("postedBy"));
                job.setSponsoredBy(jsonObject.getString("sponsoredBy"));
                job.setStatus(jsonObject.getString("status"));
                job.setTimeStamp(jsonObject.getString("timeStamp"));
                job.setVisible(jsonObject.getString("visible"));
                job.setVoteDown(jsonObject.getString("voteDown"));
                job.setVoteUp(jsonObject.getString("voteUp"));

                jobs.add(job);

            }
        }catch (Exception e){}
        for (int j=0;j<jobs.size();j++){
            UserMain_Home.titles.add(jobs.get(j).getJobTitle());
            UserMain_Home.jobsEmails.add(jobs.get(j).getPostedBy());
            Log.i("andar","aya1");
        }
        super.onPostExecute(aVoid);
    }
}*/

