package com.example.hp.quickfixx;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.quickfixx.Model.ImageGallery;
import com.example.hp.quickfixx.Model.Job;
import com.example.hp.quickfixx.Model.ReportedJob;
import com.example.hp.quickfixx.Model.User;
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
public class AdminReports extends Fragment {

    ListView listView;
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> profileImages = new ArrayList<>();
    ArrayList<String> reportedBys = new ArrayList<>();
    ArrayList<String> reportedByEmails = new ArrayList<>();
    ArrayList<String> jobIds = new ArrayList<>();
    ArrayList<String> reportKeys = new ArrayList<>();
    ArrayList<String> jobImages = new ArrayList<>();
    ArrayList<String> reasons = new ArrayList<>();

    static FragmentManager fm;
    static FragmentTransaction ft;

    static String CloudFunctionResponse = "";
    customAdapter myAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_reports, container, false);

        listView = (ListView) view.findViewById(R.id.admin_reportsListview);
        myAdapter = new customAdapter();

        readData();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              listView.setAdapter(myAdapter);
            }
        },5000);

        return view;
    }

    private void readData() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reportedJobs = firebaseDatabase.getReference("jobsReported");
        reportedJobs.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot!=null){
                    ReportedJob reportedJob = dataSnapshot.getValue(ReportedJob.class);
                    if (reportedJob.getAction12().equals("waiting")){
                        reportKeys.add(reportedJob.getKey12());
                        reasons.add("Reason: "+reportedJob.getReason12());
                        jobIds.add(reportedJob.getJobId12());
                        reportedByEmails.add(reportedJob.getReportBy12());
                        String currentReportedBy = reportedJob.getReportBy12();
                        String currentJobId = reportedJob.getJobId12();
                        readJobData(currentJobId);
                        readProfileImage(currentReportedBy);
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

    private void readProfileImage(String currentReportedBy) {
        String user = currentReportedBy.replace(".", ",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    User user = dataSnapshot.getValue(User.class);
                    profileImages.add(user.getProfilePicUrl2());
                    reportedBys.add(user.getFirstName2() + " " + user.getLastName2());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readJobData(final String currentJobId) {
        DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs").child(currentJobId);
        jobsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    Job job = dataSnapshot.getValue(Job.class);
                    titles.add(job.getJobTitle());
                    readJobImage(currentJobId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readJobImage(String currentJobId) {
        DatabaseReference imageGalleryRef = FirebaseDatabase.getInstance().getReference("imageGallery").child(currentJobId);
        imageGalleryRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    ImageGallery imageGallery = dataSnapshot.getValue(ImageGallery.class);
                    jobImages.add(imageGallery.getImageUrl1());
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
            return jobIds.size();
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
            view = inflater.inflate(R.layout.reports_list, null);

            final TextView titleTextview = (TextView) view.findViewById(R.id.titleTextview);
            final ImageView profileImageImageView = (ImageView) view.findViewById(R.id.profileImageImageView);
            final ImageView jobImage = (ImageView) view.findViewById(R.id.jobImage);
            final TextView reportedBy = (TextView) view.findViewById(R.id.reportedBy);
            final TextView JobId = (TextView) view.findViewById(R.id.JobId);
            final TextView reportedKey = (TextView) view.findViewById(R.id.reportKey);
            final TextView reportedByEmail = (TextView) view.findViewById(R.id.reportedByEmail);
            final EditText reason = (EditText) view.findViewById(R.id.reason);
            final LinearLayout ShowPost = (LinearLayout) view.findViewById(R.id.ShowPost);
            final LinearLayout TakeAction = (LinearLayout) view.findViewById(R.id.TakeAction);

            titleTextview.setText(titles.get(i));
            reportedBy.setText(reportedBys.get(i));
            JobId.setText(jobIds.get(i));
            reportedKey.setText(reportKeys.get(i));
            reportedByEmail.setText(reportedByEmails.get(i));
            reason.setText(reasons.get(i));

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference dateRef = storageRef.child(profileImages.get(i));
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
            StorageReference dateReff = storageRef.child(jobImages.get(i));
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

            //making jobId etc invisible
            JobId.setVisibility(View.GONE);
            reportedKey.setVisibility(View.GONE);
            reportedByEmail.setVisibility(View.GONE);


            profileImageImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String UserEmail = String.valueOf(reportedByEmail.getText().toString().trim());
                    Intent intent = new Intent(getActivity(),ShowProfile.class);
                    intent.putExtra("UserEmail",UserEmail);
                    startActivity(intent);
                }
            });
            reportedBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String UserEmail = String.valueOf(reportedByEmail.getText().toString().trim());
                    Intent intent = new Intent(getActivity(),ShowProfile.class);
                    intent.putExtra("UserEmail",UserEmail);
                    startActivity(intent);
                }
            });

            ShowPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String JobIdd = String.valueOf(JobId.getText().toString().trim());
                    Intent intent = new Intent(getActivity(),SinglePost.class);
                    intent.putExtra("JobId",JobIdd);
                    intent.putExtra("UserEmail","");
                    startActivity(intent);
                }
            });
            TakeAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                    alertDialog.setTitle("Take Action");
                    alertDialog.setMessage("Choose your decision?");

                    alertDialog.setPositiveButton("DELETE POST", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            JSONObject jsonObject = new JSONObject();
                            try{
                                jsonObject.put("JobId",String.valueOf(JobId.getText().toString().trim()));
                                jsonObject.put("key",String.valueOf(reportedKey.getText().toString().trim()));

                                URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/DeleteReport?text="+jsonObject.toString().trim());
                                new AsyncIgnoreReport().execute(url);
                                final AlertDialog alertDialog = new SpotsDialog(getActivity(),R.style.Custom);
                                alertDialog.show();
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                      alertDialog.dismiss();
                                        if (CloudFunctionResponse.equals("Job Deleted")){
                                            Toast.makeText(getActivity(),"Job successfully deleted",Toast.LENGTH_LONG).show();
                                            myAdapter.notifyDataSetChanged();
                                        }
                                        else {
                                            Toast.makeText(getActivity(),"Some error",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                },8000);
                            }catch (Exception e){}

                        }
                    }).setNegativeButton("IGNORE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("key",String.valueOf(reportedKey.getText().toString().trim()));

                                URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/IgnoreReport?text="+jsonObject.toString());
                                new AsyncIgnoreReport().execute(url);
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (CloudFunctionResponse.equals("Report Ignored")){
                                            Toast.makeText(getActivity(),"Report Ignored",Toast.LENGTH_SHORT).show();
                                            myAdapter.notifyDataSetChanged();
                                            AdminReports adminReports = new AdminReports();
                                            fm = getFragmentManager();
                                            ft = fm.beginTransaction();
                                            ft.replace(R.id.content_admin_main,adminReports);
                                            ft.commit();
                                        }else {
                                            Toast.makeText(getActivity(),"Some error",Toast.LENGTH_SHORT).show();
                                            myAdapter.notifyDataSetChanged();
                                        }
                                    }
                                },6000);

                            }catch (Exception e){}
                        }
                    });
                    alertDialog.show();
                }
            });

            return view;
        }
    }

}
class AsyncIgnoreReport extends AsyncTask<URL,Void,Void> {
    public  String response;
    public  String saveResponse;

    @Override
    protected void onPostExecute(Void aVoid) {
    }

    @Override
    protected Void doInBackground(URL... urls) {
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
        AdminReports.CloudFunctionResponse = saveResponse;
        return null;
    }
}