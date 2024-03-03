package com.example.hp.quickfixx;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
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

import com.example.hp.quickfixx.Model.User;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.tasks.OnSuccessListener;
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

import dmax.dialog.SpotsDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class inbox extends Fragment {

    ArrayList<String> sendBys = new ArrayList<>();
    ArrayList<String> sendByEmails = new ArrayList<>();
    ArrayList<String> profileImages = new ArrayList<>();
    ArrayList<Integer> totalMessages = new ArrayList<>();

    ArrayList<String> arrayList = new ArrayList<>();
    customAdapter myAdapter;
    FirebaseDatabase firebaseDatabase;
    ListView listview;
    ButtonRectangle composeNewMsg;

    static String cloudFunctionResponse = "";
    static String CloudFunctionResponse = "";
    String millisInString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_inbox,container,false);
        listview = (ListView) view.findViewById(R.id.inbox_Listview);
        composeNewMsg = (ButtonRectangle) view.findViewById(R.id.composeNewMessageBtn);
        myAdapter = new customAdapter();
        readData();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listview.setAdapter(myAdapter);
            }
        },5000);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String secondPerson = ((TextView) view.findViewById(R.id.sendByEmail)).getText().toString();
                String firstPerson = UserMainActivity.UserEmail.replace(".",",");
                String child;
                int compare = firstPerson.compareTo(secondPerson);
                if (compare<0){
                    child = firstPerson+secondPerson;
                }else {
                    child = secondPerson+firstPerson;
                }
                Intent intent = new Intent(getActivity(),Message_Thread.class);
                intent.putExtra("child",child);
                intent.putExtra("secondPerson",secondPerson.replace(",","."));
                startActivity(intent);
            }
        });
        composeNewMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                final EditText editText = new EditText(getActivity());
                editText.setHint("Enter Email");

                final EditText editText2 = new EditText(getActivity());
                editText2.setHint("Enter Message");

                LinearLayout linearLayout = new LinearLayout(getActivity());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(editText);
                linearLayout.addView(editText2);

                alertDialog.setTitle("Send Message");
                alertDialog.setView(linearLayout);

                final AlertDialog progressDialog = new SpotsDialog(getActivity(),R.style.Custom);

                alertDialog.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.show();
                        String sender = UserMainActivity.UserEmail.replace(".",",");
                        String receiver = editText.getText().toString().replace(".",",");
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

                        final JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("sendBy",sender);
                            jsonObject.put("sendTo",receiver);
                            jsonObject.put("message",editText2.getText().toString().trim());
                            jsonObject.put("child",child);
                            jsonObject.put("timeStamp",millisInString);

                            URL url2 = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/CheckUserExistsOrNot?text="+receiver.replace(",","."));
                            new AsyncCheckUserExists().execute(url2);
                            progressDialog.show();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (CloudFunctionResponse.equals("exists")){
                                            URL url = new URL("https://us-central1-quickfixx-10408.cloudfunctions.net/AddNewMessage?text="+jsonObject.toString());
                                            new AsyncNewMessage().execute(url);
                                            final Handler handler1 = new Handler();
                                            handler1.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressDialog.dismiss();
                                                    if (cloudFunctionResponse.equals("messageAdded")){
                                                        Toast.makeText(getActivity(),"Message sent",Toast.LENGTH_SHORT).show();
                                                        myAdapter.notifyDataSetChanged();
                                                    }else {
                                                        Toast.makeText(getActivity(),"Message sending failed",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            },6000);
                                        }
                                        else {
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(),"User not exists",Toast.LENGTH_SHORT).show();
                                        }
                                    }catch (Exception e){}
                                }
                            },5000);
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

        return view;
    }

    private void readData() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference messagesRef = firebaseDatabase.getReference("messages");
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot: dataSnapshot.getChildren()){
                    arrayList.add(childDataSnapshot.getKey());
                }
                for (int i=0;i<arrayList.size();i++){
                    if (arrayList.get(i).contains(UserMainActivity.UserEmail.replace(".",","))){
                        sendByEmails.add(arrayList.get(i).replace(UserMainActivity.UserEmail.replace(".",","),""));
                        readProfileImage(arrayList.get(i).replace(UserMainActivity.UserEmail.replace(".",","),""));
                        countMessages(arrayList.get(i));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void countMessages(String s) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(s);
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot childDataSnapshot: dataSnapshot.getChildren()){
                    count++;
                }
                totalMessages.add(count);
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
                    sendBys.add(user.getFirstName2()+" "+user.getLastName2());
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
            return sendByEmails.size();
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
                view = inflater.inflate(R.layout.inbox_list,null);
            }

            final ImageView profileImage = (ImageView) view.findViewById(R.id.profileImage);
            final TextView sendBy = (TextView) view.findViewById(R.id.sendBy);
            final TextView sendByEmail = (TextView) view.findViewById(R.id.sendByEmail);
            final TextView totalMessage = (TextView) view.findViewById(R.id.totalMesssages);


            sendByEmail.setText(sendByEmails.get(i));
            sendBy.setText(sendBys.get(i));
            if (totalMessages.get(i)<2){
                totalMessage.setText(totalMessages.get(i)+" message");
            }else {
                totalMessage.setText(totalMessages.get(i)+" messages");
            }

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference dateRef = storageRef.child(profileImages.get(i));
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
                            .into(profileImage);
                }
            });
            sendByEmail.setVisibility(View.GONE);


            return  view;
        }
    }

}
class AsyncNewMessage extends AsyncTask<URL,Void,Void> {
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
        inbox.cloudFunctionResponse = saveResponse;
        return null;
    }
}
class AsyncCheckUserExists extends AsyncTask<URL,Void,Void> {
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
        inbox.CloudFunctionResponse = saveResponse;
        return null;
    }
}