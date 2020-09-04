package com.example.dailyjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import util.JournalApi;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton, createAccountButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");
    private AutoCompleteTextView email;
    private EditText password;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressBar = findViewById(R.id.progressBar);
        loginButton = findViewById(R.id.loginButton);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        createAccountButton = findViewById(R.id.createButtonLogin);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEmailPasswordUser(email.getText().toString().trim(), password.getText().toString().trim());

            }
        });
        firebaseAuth = FirebaseAuth.getInstance();

    }

    private void loginEmailPasswordUser(String email, String pwd) {
        progressBar.setVisibility(View.VISIBLE);
        if (!email.isEmpty() && !pwd.isEmpty()) {

            firebaseAuth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                final String currentUserId = user.getUid();
                                collectionReference.whereEqualTo("userId", currentUserId)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                                @Nullable FirebaseFirestoreException e) {
                                                assert queryDocumentSnapshots != null;
                                                if (!queryDocumentSnapshots.isEmpty()) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                        JournalApi journalApi = JournalApi.getInstance();
                                                        journalApi.setUsername(snapshot.getString("userName"));
                                                        journalApi.setUserId(snapshot.getString("userId"));
                                                        startActivity(new Intent(LoginActivity.this, JournalListActivity.class));

                                                        finish();
                                                    }

                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(LoginActivity.this, "Please Enter Correct Email and Password", Toast.LENGTH_SHORT).show();
                            }


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, "Please Enter Correct Email and Password", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Please Enter Email and Password", Toast.LENGTH_SHORT).show();
        }
    }
}