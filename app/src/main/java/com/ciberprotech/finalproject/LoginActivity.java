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

public class LoginActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private SignInClient signInClient;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        signInClient = Identity.getSignInClient(getApplicationContext());

        //Animation
        ImageView logo = findViewById(R.id.logo);
        TextView title = findViewById(R.id.textViewTitle);
        TextView email = findViewById(R.id.textViewEmail);
        TextView password = findViewById(R.id.textViewPassword);
        EditText editEmail = findViewById(R.id.editTextEmail);
        EditText editPassword = findViewById(R.id.editTextPassword);
        Button loginBtn = findViewById(R.id.loginBtn);
        Button registerBtn = findViewById(R.id.registerbtn);
        ImageView googleBtn = findViewById(R.id.googleBtn);

        Animation downFlip = AnimationUtils.loadAnimation(LoginActivity.this,R.anim.downflip);
        downFlip.setDuration(2000);
        downFlip.setFillAfter(true);
        Animation upFlip = AnimationUtils.loadAnimation(LoginActivity.this,R.anim.upflip);
        upFlip.setDuration(2000);
        upFlip.setFillAfter(true);
        Animation rightFlip = AnimationUtils.loadAnimation(LoginActivity.this,R.anim.rightflip);
        rightFlip.setDuration(2000);
        rightFlip.setFillAfter(true);
        Animation leftFlip = AnimationUtils.loadAnimation(LoginActivity.this,R.anim.leftflip);
        leftFlip.setDuration(2000);
        leftFlip.setFillAfter(true);

        logo.startAnimation(downFlip);
        title.startAnimation(downFlip);
        email.startAnimation(leftFlip);
        password.startAnimation(leftFlip);
        editEmail.startAnimation(rightFlip);
        editPassword.startAnimation(rightFlip);
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
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText userEmailText = findViewById(R.id.editTextEmail);
                EditText userPasswordText = findViewById(R.id.editTextPassword);

                String email = userEmailText.getText().toString();
                String password = userPasswordText.getText().toString();

                if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Enter Email", Toast.LENGTH_SHORT).show();
                }else if (password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Enter Password", Toast.LENGTH_SHORT).show();
                } else {
                    firebaseAuth.signInWithEmailAndPassword(email,password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        if(firebaseAuth.getCurrentUser().isEmailVerified()){
                                            Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                        }else{
                                            Toast.makeText(getApplicationContext(),"Email not Verified",Toast.LENGTH_SHORT).show();
                                        }

                                    }else{
                                        Toast.makeText(getApplicationContext(),"Invalid Details",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }

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
                    String ref = String.valueOf(System.currentTimeMillis());

                    User newUser = new User(ref,user.getDisplayName(),null,user.getEmail(),((user.getPhoneNumber() == "null") ? null : user.getPhoneNumber()),null,null,null,null,null);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            firestore.collection("Users").get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                                        boolean isexist = false;

                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                User exitsUser = snapshot.toObject(User.class);
                                                if (user.getEmail().equals(exitsUser.getEmail())) {
                                                    isexist = true;
                                                }

                                                if (!isexist) {
                                                    firestore.collection("Users").add(newUser)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    Toast.makeText(LoginActivity.this, "Register Success", Toast.LENGTH_LONG);
                                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                                    finish();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }

                                                updateUI(user);

                                                break;
                                            }
                                        }
                                    });
                        }
                    }).start();

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
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
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