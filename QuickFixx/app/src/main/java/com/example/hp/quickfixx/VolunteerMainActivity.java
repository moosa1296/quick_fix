package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.quickfixx.Model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class VolunteerMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static FragmentManager fm;
    static FragmentTransaction ft;

    LinearLayout LinearLayout;
    ImageView profilePicImageView;
    TextView userNameMenu;
    TextView userEmailMenu;
    String UserName = "";
    static String UserEmail;
    String profilePicRef = "";
    String profilePicUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        UserEmail = intent.getStringExtra("UserEmail");

        VolunteerMain_Home volunteerMain_home = new VolunteerMain_Home();


        fm = getFragmentManager();
        ft = fm.beginTransaction();
        ft.add(R.id.content_volunteer_main,volunteerMain_home);
        ft.commit();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);
        final ImageView imageView = new ImageView(this);
        LinearLayout = (LinearLayout) view.findViewById(R.id.LinearLayout);
        profilePicImageView = (ImageView) view.findViewById(R.id.profileImageImageView);
        userNameMenu = (TextView) view.findViewById(R.id.userNameMenu);
        userEmailMenu = (TextView) view.findViewById(R.id.userEmailMenu);

        readProfileImageRef(UserEmail);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                userNameMenu.setText(UserName);
                userEmailMenu.setText(UserEmail);
                LinearLayout.setBackgroundColor(Color.parseColor("#1E88E5"));

                if (profilePicRef.equals("")){
                    Toast.makeText(VolunteerMainActivity.this,"Internet connection is slow",Toast.LENGTH_LONG).show();
                    Intent intent1 = new Intent(VolunteerMainActivity.this,UserMainActivity.class);
                    startActivity(intent1);
                }else {
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    StorageReference dateRef = storageRef.child(profilePicRef);
                    dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            profilePicUrl = uri.toString();
                            Picasso.with(VolunteerMainActivity.this)
                                    .load(uri)
                                    .resize(1280, 960)
                                    .centerInside()
                                    .into(profilePicImageView);
                        }
                    });
                }
            }
        },8000);


        navigationView.setNavigationItemSelectedListener(this);
    }

    private void readProfileImageRef(String userEmail){
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.volunteer_menu, menu);
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
            Intent intent = new Intent(VolunteerMainActivity.this,VolunteerMainActivity.class);
            intent.putExtra("UserEmail",VolunteerMainActivity.UserEmail);
            startActivity(intent);
        }
        if (id == R.id.Logout){
            String token_id = "";
            String current_user = VolunteerMainActivity.UserEmail.replace(".",",");
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("token_id",token_id);
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.getReference("users").child(current_user).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    FirebaseAuth.getInstance().signOut();
                    final AlertDialog progressDialog = new SpotsDialog(VolunteerMainActivity.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(VolunteerMainActivity.this,login.class);
                            startActivity(intent);
                            finish();
                        }
                    },2000);
                }
            });
        }
        if (id == R.id.Notifications){
            if (id == R.id.Notifications){
                NotificationsVolunteer notifications = new NotificationsVolunteer();
                fm = getFragmentManager();
                ft = fm.beginTransaction();
                ft.replace(R.id.content_user_main,notifications);
                ft.commit();
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id==R.id.Settings){
            Settings settings = new Settings();
            ft = fm.beginTransaction();
            ft.replace(R.id.content_volunteer_main,settings);
            ft.commit();
        }
        if (id==R.id.Notifications){
            NotificationsVolunteer notificationsVolunteer = new NotificationsVolunteer();
            fm = getFragmentManager();
            ft = fm.beginTransaction();
            ft.replace(R.id.content_volunteer_main,notificationsVolunteer);
            ft.commit();
        }
        if (id == R.id.JobsDetails){
            Intent intent = new Intent(VolunteerMainActivity.this,VolunteerJobDetails.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
