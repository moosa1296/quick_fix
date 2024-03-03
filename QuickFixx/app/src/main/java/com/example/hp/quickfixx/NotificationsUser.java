package com.example.hp.quickfixx;


import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.quickfixx.Model.Notification;
import com.example.hp.quickfixx.Model.User;
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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsUser extends Fragment {

    ListView listView;
    ListView listView2;
    ArrayList<String> profileImages = new ArrayList<>();
    ArrayList<String> notifications = new ArrayList<>();

    ArrayList<String> profileImages2 = new ArrayList<>();
    ArrayList<String> notifications2 = new ArrayList<>();

    static String notificationerName = "";
    static String notificationerName2 = "";
    static String last_seen = "";

    customAdapter myAdapter;
    customAdapter2 myAdapter2;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);


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

        readData(UserMainActivity.UserEmail);
        listView = (ListView) view.findViewById(R.id.notificationsList);
        listView2 = (ListView) view.findViewById(R.id.notificationsList2);
        myAdapter = new customAdapter();
        myAdapter2 = new customAdapter2();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(myAdapter2);
                listView2.setAdapter(myAdapter);

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userEmail",UserMainActivity.UserEmail.replace(".",","));

                    URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/UpdateLastSeen?text="+jsonObject.toString());
                    new AsyncUpdateLastSeen().execute(url);
                }catch (Exception e){}


            }
        },7000);

        return view;
    }

    private void readData(String userEmail) {
        Log.i("user",userEmail);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference notificationsRef = firebaseDatabase.getReference("notifications").child(userEmail.replace(".",","));
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
                            notifications2.add(notification.getNotificationMessage() + " from " + readName(notification.getNotificationBy()));
                            String currentUser = notification.getNotificationBy();
                            if (currentUser.equals("QFix")){
                                profileImages2.add("Profile_Images/logo.png");
                            }else {
                                readProfileImage(currentUser);
                            }
                        }
                        else {
                            notifications.add(notification.getNotificationMessage() + " from " + readName(notification.getNotificationBy()));
                            String currentUser = notification.getNotificationBy();
                            if (currentUser.equals("QFix")){
                                profileImages.add("Profile_Images/logo.png");
                            }else {
                                readProfileImage(currentUser);
                            }
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

    private String readName(String notificationBy) {
        String user = notificationBy.replace(".",",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot !=null){
                    User user = dataSnapshot.getValue(User.class);
                    notificationerName = user.getFirstName2()+" "+user.getLastName2();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return notificationerName;
    }

    private void readProfileImage(String currentUser) {
        String user = currentUser.replace(".",",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot !=null){
                    User user = dataSnapshot.getValue(User.class);
                    Log.i("userURL",user.getProfilePicUrl2());
                    profileImages.add(user.getProfilePicUrl2());
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
            return notifications.size();
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
            view = inflater.inflate(R.layout.notifications_list,null);

            final ImageView profileImage = (ImageView) view.findViewById(R.id.profileImage);
            final TextView notification = (TextView) view.findViewById(R.id.notification);

            notification.setText(notifications.get(i));

//            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//            StorageReference dateRef = storageRef.child(profileImages.get(i));
//            dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                @Override
//                public void onSuccess(Uri downloadUrl) {
//                    int imageWidth =1280;
//                    int imageHeight = 960 ;
//                    Picasso.with(getActivity())
//                            .load(downloadUrl)
//                            .resize(imageWidth , imageHeight)
//                            .centerInside()
//                            .into(profileImage);
//                }
//            });

            return view;
        }
    }

    class customAdapter2 extends BaseAdapter{
        @Override
        public int getCount() {
            return notifications2.size();
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
            view = inflater.inflate(R.layout.notifications_list,null);

            final ImageView profileImage = (ImageView) view.findViewById(R.id.profileImage);
            final TextView notification = (TextView) view.findViewById(R.id.notification);

            notification.setText(notifications2.get(i));

//            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//            StorageReference dateRef = storageRef.child(profileImages2.get(i));
//            dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                @Override
//                public void onSuccess(Uri downloadUrl) {
//                    int imageWidth =1280;
//                    int imageHeight = 960 ;
//                    Picasso.with(getActivity())
//                            .load(downloadUrl)
//                            .resize(imageWidth , imageHeight)
//                            .centerInside()
//                            .into(profileImage);
//                }
//            });

            return view;
        }
    }

    class AsyncUpdateLastSeen extends AsyncTask<URL,Void,Void> {

        @Override
        protected void onPostExecute(Void aVoid) {

        }

        @Override
        protected Void doInBackground(URL... urls) {
            try{
                URLConnection connection = urls[0].openConnection();
                connection.setConnectTimeout(5000);
                connection.getContentLength();

            }
            catch (SocketTimeoutException s){
                Log.i("Socket","Connection request timeout");
            }
            catch (Exception e){
            }
            return null;
        }
    }
}
