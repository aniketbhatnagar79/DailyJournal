package com.example.dailyjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private Button createAccButton;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    // Connect FireStore
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");

    private EditText emailText,passwordText,usernameText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        createAccButton=findViewById(R.id.createButton);
        progressBar=findViewById(R.id.createAccProgressBar);
        emailText=findViewById(R.id.emailAccount);
        passwordText=findViewById(R.id.passwordAccount);
        usernameText=findViewById(R.id.userName);

        firebaseAuth=FirebaseAuth.getInstance();
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser=firebaseAuth.getCurrentUser();
                if(currentUser!=null){
                    // user logged in
                }else{

                }

            }
        };
        createAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username=usernameText.getText().toString();
                String email=emailText.getText().toString();
                String password=passwordText.getText().toString();
                if(!username.isEmpty() && !email.isEmpty() && !password.isEmpty()){
                    createUserEmailAccount(username,email,password);
                }
                else{
                    Toast.makeText(CreateAccountActivity.this, "Empty Fields are not allowed", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void createUserEmailAccount(final String username, String email, String password){
        if(!username.isEmpty() && !email.isEmpty() && !password.isEmpty()){
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                // now go to add journal activity
                                currentUser=firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                final String currentUserId=currentUser.getUid();
                                Map<String,String> userObject=new HashMap<>();
                                userObject.put("userId",currentUserId);
                                userObject.put("userName",username);
                               // Log.d("UserObject",userObject.toString());
                                // save users to database
                                collectionReference.add(userObject).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(Objects.requireNonNull(task.getResult()).exists()){
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    String name=task.getResult().getString("userName");
                                                 //   Log.d("JP2",name);


                                                    // GlobalApi
                                                    JournalApi journalApi=JournalApi.getInstance();
                                                    journalApi.setUserId(currentUserId);
                                                    journalApi.setUsername(name);
                                                    Intent intent=new Intent(CreateAccountActivity.this,
                                                            JournalListActivity.class);
                                                    intent.putExtra("username",name);
                                                    intent.putExtra("userId",currentUserId);
                                                    startActivity(intent);
                                                }
                                                else{
                                                    Toast.makeText(CreateAccountActivity.this, "failure", Toast.LENGTH_SHORT).show();
                                                    progressBar.setVisibility(View.INVISIBLE);

                                                }

                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.INVISIBLE);

                                    }
                                });

                            }else{
                                // something went wrong

                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            })
            ;

        }
        else{

        }
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
       currentUser=firebaseAuth.getCurrentUser();
       firebaseAuth.addAuthStateListener(authStateListener);
    }
}