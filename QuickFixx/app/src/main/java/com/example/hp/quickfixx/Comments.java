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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.quickfixx.Model.Comment;
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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import dmax.dialog.SpotsDialog;


public class Comments extends AppCompatActivity {

    String JobId;
    EditText newComment;
    ButtonRectangle commentBtn;

    ListView listView;
    ArrayList<String> profileImages = new ArrayList<>();
    ArrayList<String> commentBys = new ArrayList<>();
    ArrayList<String> commentByEmails = new ArrayList<>();
    ArrayList<String> comments = new ArrayList<>();

    static String response = "";
    customAdapter myAdaptor;

    String postedBy = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Intent intent = getIntent();
        JobId = intent.getStringExtra("JobId");
        Log.i("JobId",JobId);

        myAdaptor = new customAdapter();

        listView = (ListView) findViewById(R.id.commentsListview);
        newComment = (EditText) findViewById(R.id.newComment);
        commentBtn = (ButtonRectangle) findViewById(R.id.commentBtn);

        readData(JobId);
        final AlertDialog progressDialog = new SpotsDialog(Comments.this,R.style.Custom);
        progressDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                listView.setAdapter(myAdaptor);
            }
        },3000);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference jobsRef = firebaseDatabase.getReference("jobs").child(JobId);
        jobsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    Job job = dataSnapshot.getValue(Job.class);
                    postedBy = job.getPostedBy();

                    if (postedBy.equals(UserMainActivity.UserEmail)){
                        newComment.setVisibility(View.GONE);
                        commentBtn.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog progressDialog = new SpotsDialog(Comments.this,R.style.Custom);

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("JobId",JobId);
                    jsonObject.put("commentBy",UserMainActivity.UserEmail);
                    jsonObject.put("comment",newComment.getText().toString().trim());

                    URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/AddnewComment?text="+jsonObject.toString());
                    new AsyncComment().execute(url);
                    progressDialog.show();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            if (response.equals("commentSuccessful")){
                                Toast.makeText(Comments.this,"Comment successfully",Toast.LENGTH_SHORT).show();
                                newComment.setText("");
                                myAdaptor.notifyDataSetChanged();
                            }
                            else{
                                Toast.makeText(Comments.this,"Some Error",Toast.LENGTH_SHORT).show();
                            }
                        }
                    },5000);
                }catch (Exception e){}
            }
        });
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
//        if (id == R.id.Home) {
//            Intent intent = new Intent(Comments.this,UserMainActivity.class);
//            intent.putExtra("UserEmail",UserMainActivity.UserEmail);
//            startActivity(intent);
//        }
//        if (id == R.id.Logout){
//            String token_id = "";
//            String current_user = UserMainActivity.UserEmail.replace(".",",");
//            Map<String,Object> map = new HashMap<String, Object>();
//            map.put("token_id",token_id);
//            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//            firebaseDatabase.getReference("users").child(current_user).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                    FirebaseAuth.getInstance().signOut();
//                    final AlertDialog progressDialog = new SpotsDialog(Comments.this);
//                    progressDialog.show();
//                    final Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            progressDialog.dismiss();
//                            Intent intent = new Intent(Comments.this,login.class);
//                            startActivity(intent);
//                            finish();
//                        }
//                    },2000);
//                }
//            });
//        }

        return super.onOptionsItemSelected(item);
    }

    private void readData(String jobId) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(jobId);
        commentsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot!=null){
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    comments.add(comment.getComment10());

                    String currentCommentBy = comment.getCommentBy10();

                    readProfileImage(currentCommentBy);
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

    private void readProfileImage(String currentCommentBy) {
        String user = currentCommentBy.replace(".",",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot !=null){
                    User user = dataSnapshot.getValue(User.class);
                    Log.i("userURL",user.getProfilePicUrl2());
                    profileImages.add(user.getProfilePicUrl2());
                    commentBys.add(user.getFirstName2()+" "+user.getLastName2());
                    commentByEmails.add(user.getEmail2());
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
            return comments.size();
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
            view = getLayoutInflater().inflate(R.layout.comments_list,null);

            final ImageView profileImage = (ImageView) view.findViewById(R.id.commentByProfileImage);
            final TextView commentBy = (TextView) view.findViewById(R.id.commentByTextView);
            final TextView commentByEmail = (TextView) view.findViewById(R.id.commentByEmail);
            final TextView comment = (TextView) view.findViewById(R.id.comment);

            commentBy.setText(commentBys.get(i));
            commentByEmail.setText(commentByEmails.get(i));
            comment.setText(comments.get(i));

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference dateRef = storageRef.child(profileImages.get(i));
            dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri downloadUrl) {
                    int imageWidth =1280;
                    int imageHeight = 960 ;
                    Picasso.with(Comments.this)
                            .load(downloadUrl)
                            .resize(imageWidth , imageHeight)
                            .centerInside()
                            .into(profileImage);
                }
            });

            commentByEmail.setVisibility(View.GONE);

            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String UserEmail = String.valueOf(commentByEmail.getText().toString().trim());
                    Intent intent = new Intent(Comments.this,ShowProfile.class);
                    intent.putExtra("UserEmail",UserEmail);
                    startActivity(intent);
                }
            });
            commentBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String UserEmail = String.valueOf(commentByEmail.getText().toString().trim());
                    Intent intent = new Intent(Comments.this,ShowProfile.class);
                    intent.putExtra("UserEmail",UserEmail);
                    startActivity(intent);
                }
            });

            return view;
        }
    }
}
class AsyncComment extends AsyncTask<URL,Void,Void> {
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
            Comments.response = inputLine2;
            running = false;
        }
        return null;
    }
}