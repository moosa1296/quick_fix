package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.quickfixx.Model.Message;
import com.example.hp.quickfixx.Model.User;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class Message_Thread extends AppCompatActivity {
    String child,secondPerson,millisInString;
    static String cloudFunctionResponse = "";
    static String CloudFunctionResponse = "";
    ArrayList<String> messages = new ArrayList<>();
    ArrayList<String> profileImages = new ArrayList<>();
    ArrayList<String> profileNames = new ArrayList<>();
    ArrayList<String> sendByEmails = new ArrayList<>();
    ArrayList<String> timeStamps = new ArrayList<>();

    customAdapter myAdapter;

    LinearLayout newMessageLinearLayout;
    ListView listview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message__thread);

        listview = (ListView) findViewById(R.id.message_thread_Listview);
        newMessageLinearLayout = (LinearLayout) findViewById(R.id.newMessageLinearLayout);
        myAdapter = new customAdapter();
        child = getIntent().getStringExtra("child");
        secondPerson = getIntent().getStringExtra("secondPerson");
        readData(child);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              listview.setAdapter(myAdapter);
            }
        },5000);

        newMessageLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Message_Thread.this);
                final EditText editText = new EditText(Message_Thread.this);
                editText.setHint("Enter Message");

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(65,0,0,0);


                final TextView textview = new TextView(Message_Thread.this);
                textview.setText("Receiver: "+secondPerson);
                textview.setLayoutParams(params);

                LinearLayout linearLayout = new LinearLayout(Message_Thread.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(textview);
                linearLayout.addView(editText);

                alertDialog.setTitle("Send Message");
                alertDialog.setView(linearLayout);

                final AlertDialog progressDialog = new SpotsDialog(Message_Thread.this,R.style.Custom);

                alertDialog.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.show();
                        String sender = UserMainActivity.UserEmail.replace(".",",");
                        String receiver = secondPerson;
                        int compare = sender.compareTo(receiver);
                        String child;

                        if (compare<0){
                            child = sender+receiver;
                        }
                        else {
                            child = receiver+sender;
                        }
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        millisInString  = dateFormat.format(new Date());

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("sendBy",sender);
                            jsonObject.put("sendTo",receiver);
                            jsonObject.put("message",editText.getText().toString().trim());
                            jsonObject.put("child",child);
                            jsonObject.put("timeStamp",millisInString);

                            URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/AddNewMessage?text="+jsonObject.toString());
                            new AsyncNewMessageThread().execute(url);
                            final Handler handler1 = new Handler();
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    if (cloudFunctionResponse.equals("messageAdded")){
                                        Toast.makeText(Message_Thread.this,"Message sent",Toast.LENGTH_SHORT).show();
                                        myAdapter.notifyDataSetChanged();
                                    }else {
                                        Toast.makeText(Message_Thread.this,"Message sending failed",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            },6000);
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
            Intent intent = new Intent(Message_Thread.this,UserMainActivity.class);
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
                    final AlertDialog progressDialog = new SpotsDialog(Message_Thread.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(Message_Thread.this,login.class);
                            startActivity(intent);
                            finish();
                        }
                    },2000);
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private void readData(String child) {
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("messages").child(child);
        messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot!=null){
                    Message message = dataSnapshot.getValue(Message.class);
                    messages.add(message.getMessage());
                    timeStamps.add(message.getTimeStamp2());

                    String currentSendBy = message.getSendBy();
                    readProfileImage(currentSendBy);
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
    private void readProfileImage(String user) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot !=null){
                    User user = dataSnapshot.getValue(User.class);
                    profileImages.add(user.getProfilePicUrl2());
                    sendByEmails.add(user.getEmail2());
                    profileNames.add(user.getFirstName2()+" "+user.getLastName2());
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
            return messages.size();
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
            if (view==null){
                view = getLayoutInflater().inflate(R.layout.message_thread_list,null);
            }

            final ImageView profileImage = (ImageView) view.findViewById(R.id.profileImage);
            final TextView sendBy = (TextView) view.findViewById(R.id.sendBy);
            final TextView message = (TextView) view.findViewById(R.id.message);
            final TextView sendByEmail = (TextView) view.findViewById(R.id.sendByEmail);
            final TextView timeStamp = (TextView) view.findViewById(R.id.time);

            sendBy.setText(profileNames.get(i));
            message.setText(messages.get(i));
            sendByEmail.setText(sendByEmails.get(i));
            timeStamp.setText(timeStamps.get(i));

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference dateRef = storageRef.child(profileImages.get(i));
            dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri downloadUrl) {
//                    Glide.with(getActivity()).load(downloadUrl).into(profileImageImageView);
                    int imageWidth = 1280;
                    int imageHeight = 960;
                    Picasso.with(Message_Thread.this)
                            .load(downloadUrl)
                            .resize(imageWidth, imageHeight)
                            .centerInside()
                            .into(profileImage);

                }

            });

            sendByEmail.setVisibility(View.GONE);

            return view;
        }
    }
}
class AsyncNewMessageThread extends AsyncTask<URL,Void,Void> {
    public  String response;
    public  String saveResponse;

    @Override
    protected void onPostExecute(Void aVoid) {

    }

    @Override
    protected Void doInBackground(URL... urls) {
        Log.i("back","back");
        try{
            URLConnection connection = urls[0].openConnection();
            connection.setConnectTimeout(6000);
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
        Message_Thread.cloudFunctionResponse = saveResponse;
        return null;
    }
}