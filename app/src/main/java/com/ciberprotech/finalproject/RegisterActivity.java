package com.ciberprotech.finalproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ciberprotech.finalproject.model.User;
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private SignInClient signInClient;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        signInClient = Identity.getSignInClient(getApplicationContext());

        //Animation
        ImageView logo = findViewById(R.id.logo);
        TextView title = findViewById(R.id.registerTitle);
        TextView fName = findViewById(R.id.textViewFName);
        TextView lName = findViewById(R.id.textViewLName);
        TextView email = findViewById(R.id.textViewEmail1);
        TextView mobile = findViewById(R.id.textViewMobile);
        TextView password = findViewById(R.id.textViewPassword1);
        EditText firstNameText = findViewById(R.id.editTextFName);
        EditText lastNameText = findViewById(R.id.editTextLName);
        EditText emailText = findViewById(R.id.editTextEmail1);
        EditText mobileText = findViewById(R.id.editTextMobile);
        EditText passwordText = findViewById(R.id.editTextPassword1);
        Button loginBtn = findViewById(R.id.loginBtn1);
        Button registerBtn = findViewById(R.id.registerBtn1);
        ImageView googleBtn = findViewById(R.id.googleBtn1);

        Animation downFlip = AnimationUtils.loadAnimation(RegisterActivity.this,R.anim.downflip);
        downFlip.setDuration(2000);
        downFlip.setFillAfter(true);
        Animation upFlip = AnimationUtils.loadAnimation(RegisterActivity.this,R.anim.upflip);
        upFlip.setDuration(2000);
        upFlip.setFillAfter(true);
        Animation rightFlip = AnimationUtils.loadAnimation(RegisterActivity.this,R.anim.rightflip);
        rightFlip.setDuration(2000);
        rightFlip.setFillAfter(true);
        Animation leftFlip = AnimationUtils.loadAnimation(RegisterActivity.this,R.anim.leftflip);
        leftFlip.setDuration(2000);
        leftFlip.setFillAfter(true);

        logo.startAnimation(downFlip);
        title.startAnimation(downFlip);
        fName.startAnimation(leftFlip);
        lName.startAnimation(leftFlip);
        email.startAnimation(leftFlip);
        mobile.startAnimation(leftFlip);
        password.startAnimation(leftFlip);
        firstNameText.startAnimation(rightFlip);
        lastNameText.startAnimation(rightFlip);
        emailText.startAnimation(rightFlip);
        mobileText.startAnimation(rightFlip);
        passwordText.startAnimation(rightFlip);
        loginBtn.startAnimation(upFlip);
        registerBtn.startAnimation(upFlip);
        googleBtn.startAnimation(upFlip);

        final Handler handler = new Handler();
        Runnable animationRunnable = new Runnable() {
            @Override
            public void run() {
                // Animation code here
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SpringAnimation anim = new SpringAnimation(logo, DynamicAnimation.TRANSLATION_Y);
                        anim.setStartValue(0);
                        anim.animateToFinalPosition(-10);
                        anim.getSpring().setStiffness(SpringForce.STIFFNESS_VERY_LOW);
                    }
                });

                // Schedule the next iteration
                handler.postDelayed(this, 500); // 5000 milliseconds (5 seconds) delay
            }
        };
        // Start the animation initially
        handler.post(animationRunnable);
        //Animation

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetSignInIntentRequest signInIntentRequest = GetSignInIntentRequest.builder()
                        .setServerClientId(getString(R.string.web_client_id)).build();

                Task<PendingIntent> signInIntent = signInClient.getSignInIntent(signInIntentRequest);
                signInIntent.addOnSuccessListener(new OnSuccessListener<PendingIntent>() {
                    @Override
                    public void onSuccess(PendingIntent pendingIntent) {
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(pendingIntent).build();
                        signInLauncher.launch(intentSenderRequest);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String firstName = firstNameText.getText().toString();
                String lastName = lastNameText.getText().toString();
                String email = emailText.getText().toString();
                String mobile = mobileText.getText().toString();
                String password = passwordText.getText().toString();

                if(firstName.isEmpty()){
                    Toast.makeText(RegisterActivity.this,"Please enter First Name",Toast.LENGTH_SHORT).show();
                } else if (lastName.isEmpty()) {
                    Toast.makeText(RegisterActivity.this,"Please enter Last Name",Toast.LENGTH_SHORT).show();
                } else if (email.isEmpty()) {
                    Toast.makeText(RegisterActivity.this,"Please enter Email",Toast.LENGTH_SHORT).show();
                } else if (mobile.isEmpty()) {
                    Toast.makeText(RegisterActivity.this,"Please enter Mobile",Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this,"Please enter Password",Toast.LENGTH_SHORT).show();
                }else {

                    firestore.collection("Users").get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                boolean isexist = false;

                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                        User exitsUser = snapshot.toObject(User.class);
                                        if (email.equals(exitsUser.getEmail())) {
                                            isexist = true;
                                        }
                                    }

                                    if (isexist) {
                                        Toast.makeText(RegisterActivity.this, "This email already exists", Toast.LENGTH_SHORT).show();
                                    } else {
                                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {

                                                            FirebaseUser user = firebaseAuth.getCurrentUser();
                                                            user.sendEmailVerification();

                                                            String id = String.valueOf(System.currentTimeMillis());
                                                            User newUser = new User(id, firstName,lastName, email, mobile, password,"","","","");

                                                            firestore.collection("Users").add(newUser)
                                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                        @Override
                                                                        public void onSuccess(DocumentReference documentReference) {
                                                                            Toast.makeText(RegisterActivity.this, "Register Success", Toast.LENGTH_LONG);
                                                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Toast.makeText(RegisterActivity.this,"js"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });

                                                        } else {
                                                            Toast.makeText(RegisterActivity.this, "User already exits.", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    }
                                }
                            });

                }
            }

        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        Task<AuthResult> authResultTask = firebaseAuth.signInWithCredential(authCredential);
        authResultTask.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    Toast.makeText(getApplicationContext(),"Register Successful and Verify Email",Toast.LENGTH_SHORT).show();
                    user.sendEmailVerification();
                    updateUI(user);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        }
    }

    private void handleSignInResult(Intent intent) {
        try {
            SignInCredential signInCredential = signInClient.getSignInCredentialFromIntent(intent);
            String idToken = signInCredential.getGoogleIdToken();
            firebaseAuthWithGoogle(idToken);
        } catch (ApiException e) {
        }
    }

    private final ActivityResultLauncher<IntentSenderRequest> signInLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    handleSignInResult(o.getData());
                }
            }
    );
}