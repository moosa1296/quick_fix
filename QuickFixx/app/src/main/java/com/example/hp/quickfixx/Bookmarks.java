package com.example.hp.quickfixx;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.quickfixx.Model.Bookmark;
import com.example.hp.quickfixx.Model.ImageGallery;
import com.example.hp.quickfixx.Model.Job;
import com.example.hp.quickfixx.Model.User;
import com.example.hp.quickfixx.Model.Voting;
import com.gc.materialdesign.views.ButtonRectangle;
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
public class Bookmarks extends Fragment {

    ArrayList<String> profileimages = new ArrayList<>();
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> jobimages = new ArrayList<>();
    ArrayList<String> postedBys = new ArrayList<>();
    ArrayList<String> postedTimes = new ArrayList<>();
    ArrayList<String> addresses = new ArrayList<>();
    ArrayList<String> jobids = new ArrayList<>();
    ArrayList<String> bookmarkids = new ArrayList<>();
    ArrayList<String> coinsproposed = new ArrayList<>();
    ArrayList<String> days = new ArrayList<>();
    ArrayList<String> sponsors = new ArrayList<>();
    ArrayList<String> assigners = new ArrayList<>();
    ArrayList<String> postedBysEmail = new ArrayList<>();

    static FragmentManager fm;
    static FragmentTransaction ft;

    String currentJobId = null;
    String currentPostedBy = null;
    ListView listView;
    static String cloudFunctionReponse = "";
    customAdapter myadaptor;

    FirebaseUser firebaseUser;

    String UserName = "";
    String profilePicRef = "";
    String profilePicUrl = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        listView = (ListView) view.findViewById(R.id.bookmarksListview);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myadaptor = new customAdapter();
        fm = getFragmentManager();


        readData();
        final AlertDialog progressDialog = new SpotsDialog(getActivity(), R.style.Custom);
        progressDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                listView.setAdapter(myadaptor);

            }
        }, 5500);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!firebaseUser.isEmailVerified()) {
                    Toast.makeText(getActivity(), "Verify Email for using this feature", Toast.LENGTH_SHORT).show();
                } else {
                    String JobId = ((TextView) view.findViewById(R.id.JobId)).getText().toString();
                    Intent intent = new Intent(getActivity(), SinglePost.class);
                    intent.putExtra("JobId", JobId);
                    intent.putExtra("UserEmail", UserEmail);
                    startActivity(intent);
                }
            }
        });
        return view;
    }

    private void readData() {
        DatabaseReference bookmarksRef = FirebaseDatabase.getInstance().getReference("bookmarks").child(UserEmail.replace(".", ","));
        bookmarksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    Bookmark bookmark = dataSnapshot.getValue(Bookmark.class);
                    bookmarkids.add(bookmark.getKey8());
                    DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs").child(bookmark.getJobId8());
                    jobsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot != null) {
                                Job job = dataSnapshot.getValue(Job.class);
                                titles.add(job.getJobTitle());
                                jobids.add(job.getJobId());
                                coinsproposed.add(job.getCoins());
                                days.add(job.getMaxNoDays());
                                //check sponsorship
                                String currentsponsor = job.getSponsoredBy();
                                if (currentsponsor.equals("Get Sponsor")) {
                                    sponsors.add("Not Available");
                                } else {
                                    sponsors.add("Available");
                                }
                                String currentAssigner = job.getAssignedTo();
                                if (currentAssigner.equals("")) {
                                    assigners.add("Not Available");
                                } else {
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
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

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
        String user = currentPostedBy.replace(".", ",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    User user = dataSnapshot.getValue(User.class);
                    profileimages.add(user.getProfilePicUrl2());
                    postedBys.add(user.getFirstName2() + " " + user.getLastName2());
                    postedBysEmail.add(user.getEmail2());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readName(String currentAssigner) {
        String user = currentAssigner.replace(".", ",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    User user = dataSnapshot.getValue(User.class);
                    assigners.add(user.getFirstName2() + " " + user.getLastName2());
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
                if (dataSnapshot != null) {
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
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.bookmarks_list, null);

            final TextView titleTextview = (TextView) view.findViewById(R.id.titleTextview);
            final ImageView profileImageImageView = (ImageView) view.findViewById(R.id.profileImageImageView);
            final ImageView jobImage = (ImageView) view.findViewById(R.id.jobImage);
            final TextView postedBy = (TextView) view.findViewById(R.id.postedBy);
            final TextView postedTime = (TextView) view.findViewById(R.id.postedTime);
            final TextView address = (TextView) view.findViewById(R.id.address);
            final TextView JobId = (TextView) view.findViewById(R.id.JobId);
            final TextView BookmarkId = (TextView) view.findViewById(R.id.BookmarkId);
            final TextView postedByEmail = (TextView) view.findViewById(R.id.postedByEmail);
            final TextView coinsProposed = (TextView) view.findViewById(R.id.coinsTextview);
            final TextView Maxdays = (TextView) view.findViewById(R.id.daysTextview);
            final TextView Sponsorship = (TextView) view.findViewById(R.id.sponsorTextview);
            final TextView AssignedTo = (TextView) view.findViewById(R.id.assignedToTextview);
            final ButtonRectangle deleteBtn = (ButtonRectangle) view.findViewById(R.id.deleteBtn);

            titleTextview.setText(titles.get(i));
            postedBy.setText(postedBys.get(i));
            postedTime.setText(postedTimes.get(i));
            address.setText(addresses.get(i));
            JobId.setText(jobids.get(i));
            BookmarkId.setText(bookmarkids.get(i));
            postedByEmail.setText(postedBysEmail.get(i));
            coinsProposed.setText(coinsproposed.get(i));
            Maxdays.setText(days.get(i));
            Sponsorship.setText(sponsors.get(i));
            AssignedTo.setText(assigners.get(i));

            postedByEmail.setVisibility(View.GONE);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference dateRef = storageRef.child(profileimages.get(i));
            dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri downloadUrl) {
//                    Glide.with(getActivity()).load(downloadUrl).into(profileImageImageView);
                    int imageWidth = 1280;
                    int imageHeight = 960;
                    Picasso.with(getActivity())
                            .load(downloadUrl)
                            .resize(imageWidth, imageHeight)
                            .centerInside()
                            .into(profileImageImageView);
                }
            });
            StorageReference dateReff = storageRef.child(jobimages.get(i));
            dateReff.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri downloadUrl) {
//                    Glide.with(getActivity()).load(downloadUrl).into(jobImage);
                    int imageWidth = 1280;
                    int imageHeight = 960;
                    Picasso.with(getActivity())
                            .load(downloadUrl)
                            .resize(imageWidth, imageHeight)
                            .centerInside()
                            .into(jobImage);
                }
            });

            //making JobId invisible
            JobId.setVisibility(View.GONE);
            BookmarkId.setVisibility(View.GONE);

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String Bookmarkid = String.valueOf(BookmarkId.getText().toString().trim());

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("userEmail", UserMainActivity.UserEmail.replace(".", ","));
                        jsonObject.put("BookmarkId", Bookmarkid);

                        URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/DeleteBookmark?text=" + jsonObject.toString());
                        new AsyncDeleteBookmark().execute(url);

                        final AlertDialog alertDialog = new SpotsDialog(getActivity(),R.style.Custom);
                        alertDialog.show();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (cloudFunctionReponse.equals("bookmark removed")){
                                    alertDialog.dismiss();
                                    Toast.makeText(getActivity(),"Bookmark Removed",Toast.LENGTH_SHORT).show();

                                    Bookmarks bookmarks = new Bookmarks();
                                    ft = fm.beginTransaction();
                                    ft.replace(R.id.content_user_main,bookmarks);
                                    ft.commit();


                                }else {
                                    alertDialog.dismiss();
                                    Toast.makeText(getActivity(),"Some error",Toast.LENGTH_SHORT).show();
                                }
                            }
                        },6000);

                    } catch (Exception e) {
                    }
                }
            });

            profileImageImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String UserEmail = String.valueOf(postedByEmail.getText().toString().trim());
                    Intent intent = new Intent(getActivity(), ShowProfile.class);
                    intent.putExtra("UserEmail", UserEmail);
                    startActivity(intent);
                }
            });
            postedBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String UserEmail = String.valueOf(postedByEmail.getText().toString().trim());
                    Intent intent = new Intent(getActivity(), ShowProfile.class);
                    intent.putExtra("UserEmail", UserEmail);
                    startActivity(intent);
                }
            });


            return view;
        }
    }

    class AsyncDeleteBookmark extends AsyncTask<URL, Void, Void> {
        public String response;
        public String saveResponse;

        @Override
        protected void onPostExecute(Void aVoid) {

        }

        @Override
        protected Void doInBackground(URL... urls) {
            try {
                URLConnection connection = urls[0].openConnection();
                connection.setConnectTimeout(6000);
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
            Bookmarks.cloudFunctionReponse = saveResponse;
            return null;
        }
    }
}