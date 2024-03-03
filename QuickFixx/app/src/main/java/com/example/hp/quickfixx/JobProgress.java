package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
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

import com.example.hp.quickfixx.Model.Progress;
import com.example.hp.quickfixx.Model.ProgressGallery;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class JobProgress extends AppCompatActivity {
    String JobId,JobTitle,currentKey;
    TextView titleTextview;
    ButtonRectangle submitProgressBtn;
    ListView listview;
    ArrayList<String> progressImages = new ArrayList<>();
    ArrayList<String> progressDescriptions = new ArrayList<>();
    customAdapter myAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_progress);
        Intent intent = getIntent();
        JobId = intent.getStringExtra("JobId");
        JobTitle = intent.getStringExtra("JobTitle");

        myAdapter = new customAdapter();
        titleTextview = (TextView) findViewById(R.id.titleTextview);
        submitProgressBtn = (ButtonRectangle) findViewById(R.id.submitNewBtn);
        listview = (ListView) findViewById(R.id.jobProgressListview);
        readData(JobId);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listview.setAdapter(myAdapter);
            }
        },5000);

        titleTextview.setText(JobTitle);
        submitProgressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(JobProgress.this,SubmitProgress.class);
                intent1.putExtra("JobId",JobId);
                startActivity(intent1);
            }
        });

        if (SplashScreen.UserEmailPublic.equals("hjavaid2014@namal.edu.pk")){
            submitProgressBtn.setVisibility(View.GONE);
        }

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
            Intent intent = new Intent(JobProgress.this,UserMainActivity.class);
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
                    final AlertDialog progressDialog = new SpotsDialog(JobProgress.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(JobProgress.this,login.class);
                            startActivity(intent);
                            finish();
                        }
                    },2000);
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private void readData(final String jobId) {
        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference("progress").child(jobId);
        progressRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot!=null){
                    Progress progress = dataSnapshot.getValue(Progress.class);
                    progressDescriptions.add(progress.getProgressDescription6());

                    currentKey = progress.getKey6();
                    Log.i("currentKey",currentKey);
                    readImage(jobId,currentKey);

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

    private void readImage(String jobId,String currentKey) {
        final DatabaseReference progressGallery = FirebaseDatabase.getInstance().getReference("progressGallery").child(jobId).child(currentKey);
        progressGallery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    ProgressGallery progressGallery = dataSnapshot.getValue(ProgressGallery.class);
                    progressImages.add(progressGallery.getImageUrl7());
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
            return progressDescriptions.size();
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
            view = getLayoutInflater().inflate(R.layout.progress_list,null);

            final ImageView progressImage = (ImageView) view.findViewById(R.id.progressImage);
            TextView progressDescription = (TextView) view.findViewById(R.id.progressDescription);
            TextView progressNumber = (TextView) view.findViewById(R.id.progressNumber);

            progressDescription.setText(progressDescriptions.get(i));
            progressNumber.setText("Progress Number "+Integer.toString(i+1));

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference dateRef = storageRef.child(progressImages.get(i));
            dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri downloadUrl)
                {
//                    Glide.with(getActivity()).load(downloadUrl).into(profileImageImageView);
                    int imageWidth =1280;
                    int imageHeight = 960 ;
                    Picasso.with(JobProgress.this)
                            .load(downloadUrl)
                            .resize(imageWidth , imageHeight)
                            .centerInside()
                            .into(progressImage);
                }
            });

            return view;
        }
    }
}
