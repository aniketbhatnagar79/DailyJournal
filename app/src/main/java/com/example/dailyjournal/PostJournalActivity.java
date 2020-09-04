package com.example.dailyjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1;
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton, backgroundImageView;
    private EditText titleEditText, thoughtEditText;

    private String currentUserName, currentUserId;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    // connect to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Journal");
    private TextView currentUserTextView, dateTextView;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        progressBar = findViewById(R.id.postProgressBar);
        titleEditText = findViewById(R.id.postTitleEditText);
        thoughtEditText = findViewById(R.id.postThoughtEditText);
        currentUserTextView = findViewById(R.id.postUserNameTextView);
        dateTextView = findViewById(R.id.postDateTextView);
        saveButton = findViewById(R.id.postSaveButton);
        addPhotoButton = findViewById(R.id.postCameraButton);
        backgroundImageView = findViewById(R.id.backgroundImageView);
        saveButton.setOnClickListener(this);
        addPhotoButton.setOnClickListener(this);
        progressBar.setVisibility(View.INVISIBLE);
        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserId();

            currentUserName = JournalApi.getInstance().getUsername();
            Log.d("JP", currentUserId + " " + currentUserName);
            currentUserTextView.setText(currentUserName);

        }
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                }
            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        } else {

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.postSaveButton:
                // save journal
                saveJournal();
                break;
            case R.id.postCameraButton:
                // get image from phone
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);

                break;
        }
    }

    private void saveJournal() {
        final String title = titleEditText.getText().toString().trim();
        final String thoughts = thoughtEditText.getText().toString().trim();
        progressBar.setVisibility(View.VISIBLE);
        if (!thoughts.isEmpty() && !title.isEmpty() && imageUri != null) {

            final StorageReference filePath = storageReference.child("journal_images")
                    .child("my_image_" + Timestamp.now().getSeconds());
            filePath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    Journal journal = new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thoughts);
                                    journal.setImageUrl(imageUrl);
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUserId(currentUserId);
                                    journal.setUserName(currentUserName);
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(PostJournalActivity.this,
                                                            JournalListActivity.class));
                                                    finish();

                                                }
                                            });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        } else {

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();   // image path on phone
                backgroundImageView.setImageURI(imageUri);  // show image
            }

        }
    }
}