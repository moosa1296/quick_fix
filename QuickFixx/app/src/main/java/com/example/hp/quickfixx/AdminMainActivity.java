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

import dmax.dialog.SpotsDialog;

public class AdminMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String UserEmail;
    static FragmentManager fm;
    static FragmentTransaction ft;
    LinearLayout LinearLayout;
    ImageView profilePicImageView;
    TextView userNameMenu;
    TextView userEmailMenu;
    String profilePicRef = "";
    String profilePicUrl = "";
    String UserName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        UserEmail = intent.getStringExtra("UserEmail");


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
                if (profilePicRef.equals("")){
                    Toast.makeText(AdminMainActivity.this,"Internet connection is slow",Toast.LENGTH_LONG).show();
                    Intent intent1 = new Intent(AdminMainActivity.this,UserMainActivity.class);
                    startActivity(intent1);
                }else {
                    userNameMenu.setText(UserName);
                    userEmailMenu.setText(UserEmail);
                    LinearLayout.setBackgroundColor(Color.parseColor("#1E88E5"));
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    StorageReference dateRef = storageRef.child(profilePicRef);
                    dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            profilePicUrl = uri.toString();
                            Picasso.with(AdminMainActivity.this)
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
        getMenuInflater().inflate(R.menu.admin_menu, menu);
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
            return true;
        }
        if (id == R.id.Logout){
            final AlertDialog progressDialog = new SpotsDialog(AdminMainActivity.this);
            progressDialog.show();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(AdminMainActivity.this,login.class);
                    startActivity(intent);
                    finish();
                }
            },2000);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.Reports) {
            AdminReports adminReports = new AdminReports();
            fm = getFragmentManager();
            ft = fm.beginTransaction();
            ft.replace(R.id.content_admin_main,adminReports);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
