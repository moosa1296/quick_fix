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

import com.example.hp.quickfixx.Model.User;
import com.example.hp.quickfixx.Model.Voting;
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

public class ShowAllVotes extends AppCompatActivity {


    private ListView listView;
    ArrayList<String> profileImages = new ArrayList<>();
    ArrayList<String> votedBys = new ArrayList<>();
    ArrayList<Integer> voteImages = new ArrayList<>();
    ArrayList<String> voteBysEmail = new ArrayList<>();

    customAdapter myadaptor;
    String JobId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_votes);

        listView = (ListView) findViewById(R.id.ShowAllVotes_Listview);
        myadaptor = new customAdapter();
        Intent intent = getIntent();
        JobId = intent.getStringExtra("JobId");
        readData(JobId);
        final AlertDialog progressDialog = new SpotsDialog(ShowAllVotes.this,R.style.Custom);
        progressDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                listView.setAdapter(myadaptor);
            }
        },3000);

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
            Intent intent = new Intent(ShowAllVotes.this,UserMainActivity.class);
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
                    final AlertDialog progressDialog = new SpotsDialog(ShowAllVotes.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(ShowAllVotes.this,login.class);
                            startActivity(intent);
                            finish();
                        }
                    },2000);
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private void readData(String JobId) {
        DatabaseReference votingRef = FirebaseDatabase.getInstance().getReference("voting").child(JobId);
        votingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot!=null){
                    Voting voting = dataSnapshot.getValue(Voting.class);
                    String vote = voting.getVote();

                    //checking whether cote is up or down
                    if (vote.equals("1")){
                        voteImages.add(R.drawable.thumbs_up);
                    }else if (vote.equals("0")){
                        voteImages.add(R.drawable.thumbs_down);
                    }

                    String currentVotedBy = voting.getUserEmail();
                    Log.i("CurrentVotedBy",currentVotedBy);
                    readProfileImage(currentVotedBy);
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
    private void readProfileImage(String currentVotedBy) {
        String user = currentVotedBy.replace(".",",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot !=null){
                    User user = dataSnapshot.getValue(User.class);
                    Log.i("userURL",user.getProfilePicUrl2());
                    profileImages.add(user.getProfilePicUrl2());
                    votedBys.add(user.getFirstName2()+" "+user.getLastName2());
                    voteBysEmail.add(user.getEmail2());
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
            return votedBys.size();
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.show_all_votes_list,null);

            final ImageView profileImage = (ImageView) view.findViewById(R.id.votedByProfileImage);
            final TextView votedBy = (TextView) view.findViewById(R.id.votedByTextView);
            final ImageView voteImage = (ImageView) view.findViewById(R.id.voteImage);
            final TextView votedByEmail = (TextView) view.findViewById(R.id.votedByEmail);

            votedBy.setText(votedBys.get(i));
            voteImage.setImageResource(voteImages.get(i));
            votedByEmail.setText(voteBysEmail.get(i));

            //hide emails
            votedByEmail.setVisibility(View.GONE);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference dateRef = storageRef.child(profileImages.get(i));
            dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri downloadUrl) {
                    int imageWidth =1280;
                    int imageHeight = 960 ;
                    Picasso.with(ShowAllVotes.this)
                            .load(downloadUrl)
                            .resize(imageWidth , imageHeight)
                            .centerInside()
                            .into(profileImage);
                }
            });

            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String UserEmail = String.valueOf(votedByEmail.getText().toString().trim());
                    Intent intent = new Intent(ShowAllVotes.this,ShowProfile.class);
                    intent.putExtra("UserEmail",UserEmail);
                    startActivity(intent);
                }
            });
            votedBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String UserEmail = String.valueOf(votedByEmail.getText().toString().trim());
                    Intent intent = new Intent(ShowAllVotes.this,ShowProfile.class);
                    intent.putExtra("UserEmail",UserEmail);
                    startActivity(intent);
                }
            });

            return view;
        }
    }
}
