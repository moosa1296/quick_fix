package com.example.hp.quickfixx;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
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

public class UserMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static String UserEmail;
    static FragmentManager fm;
    static FragmentTransaction ft;
    UserMain_Home userMain_home;
    LinearLayout LinearLayout;
    ImageView profilePicImageView;
    TextView userNameMenu;
    TextView userEmailMenu;
    String profilePicRef = "";
    String profilePicUrl = "";
    String UserName = "";



    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        UserEmail = intent.getStringExtra("UserEmail");

        userMain_home = new UserMain_Home();


        fm = getFragmentManager();
        ft = fm.beginTransaction();
        ft.add(R.id.content_user_main,userMain_home);
        ft.commit();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                    Intent intent = new Intent(UserMainActivity.this,MapsActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(UserMainActivity.this,"Verify Email from settings for using this feature",Toast.LENGTH_SHORT).show();
                }

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();



        navigationView = (NavigationView) findViewById(R.id.nav_view);
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
                    Toast.makeText(UserMainActivity.this,"Internet connection is slow",Toast.LENGTH_LONG).show();
                    Intent intent1 = new Intent(UserMainActivity.this,UserMainActivity.class);
                    startActivity(intent1);
                }else {
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    StorageReference dateRef = storageRef.child(profilePicRef);
                    dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            profilePicUrl = uri.toString();
                            Picasso.with(UserMainActivity.this)
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

        if (id == R.id.Search){
            SearchJob();
        }
        if (id == R.id.Notifications){
            NotificationsUser notifications = new NotificationsUser();
            ft = fm.beginTransaction();
            ft.replace(R.id.content_user_main,notifications);
            ft.commit();
        }
        if (id == R.id.Home) {
            Intent intent = new Intent(UserMainActivity.this,UserMainActivity.class);
            intent.putExtra("UserEmail",UserEmail);
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
                    final AlertDialog progressDialog = new SpotsDialog(UserMainActivity.this);
                    progressDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(UserMainActivity.this,login.class);
                            startActivity(intent);
                            finish();
                        }
                    },2000);
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    private void SearchJob() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserMainActivity.this);
        final EditText editText = new EditText(UserMainActivity.this);
        editText.setHint("Enter Job ID");
        alertDialog.setTitle("Search Job By ID");
        alertDialog.setView(editText);
        alertDialog.setPositiveButton("SEARCH", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs");
                jobsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(editText.getText().toString().trim())){
                            Intent intent = new Intent(UserMainActivity.this,SinglePost.class);
                            intent.putExtra("JobId",editText.getText().toString().trim());
                            intent.putExtra("UserEmail",UserMainActivity.UserEmail);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(UserMainActivity.this,"Job is not available",Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();

        if (id==R.id.Jobs){
            Intent intent = new Intent(UserMainActivity.this,JobsDetails.class);
            startActivity(intent);
        }
        if (id==R.id.Profile){
            Intent intent = new Intent(UserMainActivity.this,ShowProfile.class);
            intent.putExtra("UserEmail",UserEmail);
            startActivity(intent);
        }
        if (id==R.id.Settings){
            Settings settings = new Settings();
            ft = fm.beginTransaction();
            ft.replace(R.id.content_user_main,settings);
            ft.commit();
        }
        if (id==R.id.Bookmarks){
            Bookmarks bookmarks = new Bookmarks();
            ft = fm.beginTransaction();
            ft.replace(R.id.content_user_main,bookmarks);
            ft.commit();
        }
        if (id==R.id.Inbox){
            inbox inbox = new inbox();
            ft = fm.beginTransaction();
            ft.replace(R.id.content_user_main,inbox);
            ft.commit();
        }
        if (id==R.id.Notifications){
            NotificationsUser notifications = new NotificationsUser();
            ft = fm.beginTransaction();
            ft.replace(R.id.content_user_main,notifications);
            ft.commit();
        }
        if (id==R.id.Ratings){
            Ratings ratings = new Ratings();
            ft = fm.beginTransaction();
            ft.replace(R.id.content_user_main,ratings);
            ft.commit();
        }
        item.setChecked(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
