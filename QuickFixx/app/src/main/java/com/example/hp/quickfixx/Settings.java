package com.example.hp.quickfixx;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dmax.dialog.SpotsDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class Settings extends Fragment {

    ButtonRectangle sendVerificationEmail,changePassword;
    FirebaseUser firebaseUser;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        sendVerificationEmail = (ButtonRectangle) view.findViewById(R.id.sendVerificationEmail);
        changePassword = (ButtonRectangle) view.findViewById(R.id.changePassword);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final AlertDialog progressDialog = new SpotsDialog(getActivity(),R.style.Custom);

        sendVerificationEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Verification Email sent",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Error, Verification Email not sent",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = firebaseUser.getEmail();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                final EditText editText = new EditText(getActivity());
                editText.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);

                alertDialog.setTitle("Edit Password");
                alertDialog.setMessage("Enter your old password");
                alertDialog.setView(editText);

                alertDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.show();
                        String oldPass = editText.getText().toString().trim();
                        AuthCredential credential = EmailAuthProvider.getCredential(email,oldPass);

                        firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    progressDialog.dismiss();
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                                    final EditText editText = new EditText(getActivity());
                                    editText.setInputType(InputType.TYPE_CLASS_TEXT |
                                            InputType.TYPE_TEXT_VARIATION_PASSWORD);

                                    alertDialog.setTitle("Edit Password");
                                    alertDialog.setMessage("Enter your new password");
                                    alertDialog.setView(editText);

                                    alertDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            progressDialog.show();
                                            String newPass = editText.getText().toString().trim();
                                            firebaseUser.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getActivity(),"Password updated successfully",Toast.LENGTH_SHORT).show();
                                                        FirebaseAuth.getInstance().signOut();
                                                        Intent intent = new Intent(getActivity(),SplashScreen.class);
                                                        startActivity(intent);
                                                    }else {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getActivity(),"Something went wrong",Toast.LENGTH_SHORT).show();
                                                    }
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
                                else {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(),"Authentication failed",Toast.LENGTH_SHORT).show();
                                }
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
        });



        return view;
    }

}
