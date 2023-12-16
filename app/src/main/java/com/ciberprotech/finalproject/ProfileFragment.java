package com.ciberprotech.finalproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ciberprotech.finalproject.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProfileFragment extends Fragment {
    View view;
    public static final String TAG = ProfileFragment.class.getName();
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private FirebaseAuth firebaseAuth;
    private User user;
    private Uri imagePath;
    private ImageButton imageButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        EditText profileFNameEditText = view.findViewById(R.id.editTextUFirstName);
        EditText profileLNameEditText = view.findViewById(R.id.editTextULastName);
        TextView profileEmailText = view.findViewById(R.id.editTextUEmail);
        EditText profileMobileText = view.findViewById(R.id.editTextUMobile);
        EditText profileAddressLine1EditText = view.findViewById(R.id.editTextUAddress1);
        EditText profileAddressLine2EditText = view.findViewById(R.id.editTextUAddress2);
        EditText profileAddressCityEditText = view.findViewById(R.id.editTextUCity);

        view.findViewById(R.id.logoutBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(), "log Out", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
                startActivity(new Intent(getActivity().getApplicationContext(), LoginActivity.class));
            }
        });

        //Load Data
        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {
            firestore.collection("Users")
                    .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                user = documentSnapshot.toObject(User.class);

                                profileEmailText.setText(user.getEmail());
                                if (user.getFirstName() != null) {
                                    profileFNameEditText.setText(user.getFirstName());
                                }
                                if (user.getLastName() != null) {
                                    profileLNameEditText.setText(user.getLastName());
                                }
                                if (user.getMobile() != null) {
                                    profileMobileText.setText(user.getMobile());
                                }
                                if(user.getAddress1()!=null){
                                    profileAddressLine1EditText.setText(user.getAddress1());
                                }
                                if(user.getAddress2()!=null){
                                    profileAddressLine2EditText.setText(user.getAddress2());
                                }
                                if(user.getCity()!=null){
                                    profileAddressCityEditText.setText(user.getCity());
                                }
                                if (user.getImagePath()!=null){
                                    storage.getReference("UserImage/" + user.getImagePath())
                                            .getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    Transformation transformation = new RoundedTransformationBuilder()
                                                            .borderColor(Color.WHITE)
                                                            .borderWidthDp(3)
                                                            .cornerRadiusDp(20)
                                                            .oval(false)
                                                            .build();

                                                    Picasso.get()
                                                            .load(uri)
                                                            .fit()
                                                            .transform(transformation)
                                                            .into(imageButton);
                                                }
                                            });
                                }

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Please Login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity().getApplicationContext(), LoginActivity.class));
        }

        //Update Profile
        imageButton = view.findViewById(R.id.uImageBtn);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                activityResultLauncher1.launch(Intent.createChooser(intent,"Select Image"));
            }
        });
        view.findViewById(R.id.updateProfileBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText fNameView = view.findViewById(R.id.editTextUFirstName);
                EditText lNameView = view.findViewById(R.id.editTextULastName);
                TextView emailView = view.findViewById(R.id.editTextUEmail);
                EditText mobileView = view.findViewById(R.id.editTextUMobile);
                EditText address1View = view.findViewById(R.id.editTextUAddress1);
                EditText address2View = view.findViewById(R.id.editTextUAddress2);
                EditText cityView = view.findViewById(R.id.editTextUCity);

                String fName = fNameView.getText().toString();
                String lName = lNameView.getText().toString();
                String email = emailView.getText().toString();
                String mobile = mobileView.getText().toString();
                String address1 = address1View.getText().toString();
                String address2 = address2View.getText().toString();
                String city = cityView.getText().toString();

                if (fName.isEmpty()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter First Name", Toast.LENGTH_SHORT).show();
                } else if (lName.isEmpty()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter Last Name", Toast.LENGTH_SHORT).show();
                } else if (email.isEmpty()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter EMail", Toast.LENGTH_SHORT).show();
                } else if (mobile.isEmpty()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter Mobile", Toast.LENGTH_SHORT).show();
                }  else if (address1.isEmpty()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter Address line1", Toast.LENGTH_SHORT).show();
                } else if (address2.isEmpty()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter Address line2", Toast.LENGTH_SHORT).show();
                } else if (city.isEmpty()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter City", Toast.LENGTH_SHORT).show();
                } else {

                    firestore.collection("Users")
                            .whereNotEqualTo("id", user.getId()).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                boolean existsMobile = false;

                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                        User exitsItem = snapshot.toObject(User.class);
                                        if (mobile.equals(exitsItem.getMobile())) {
                                            existsMobile = true;
                                        }
                                    }
                                    if (existsMobile) {
                                        Toast.makeText(getActivity().getApplicationContext(), "Mobile number already exists", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String ref = String.valueOf(System.currentTimeMillis());

                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("firstName", fName);
                                        updates.put("lastName", lName);
                                        updates.put("email", email);
                                        updates.put("mobile", mobile);
                                        updates.put("address1", address1);
                                        updates.put("address2", address2);
                                        updates.put("city", city);

                                        String image1Id = UUID.randomUUID().toString();

                                        if (imagePath == null) {
                                            updates.put("imagePath", user.getImagePath());
                                        } else {
                                            updates.put("imagePath", image1Id);
                                        }

                                        firestore.collection("Users")
                                                .whereEqualTo("id", user.getId()).get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            List<String> documentIds = new ArrayList<>();

                                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                                String documentId = document.getId();

                                                                ProgressDialog dialog = new ProgressDialog(requireContext());
                                                                dialog.setMessage("Update Profile...");
                                                                dialog.setCancelable(false);
                                                                dialog.show();

                                                                firestore.collection("Users").document(documentId).update(updates)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                dialog.dismiss();

                                                                                StorageReference storageRef = storage.getReference();

                                                                                if (imagePath != null) {

                                                                                    ProgressDialog dialog = new ProgressDialog(requireContext());
                                                                                    dialog.setMessage("Uploading");
                                                                                    dialog.setCancelable(false);
                                                                                    dialog.show();

                                                                                    if (user.getImagePath() != null) {
                                                                                        StorageReference desertRef = storageRef.child("UserImage/" + user.getImagePath());

                                                                                        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                            }
                                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception exception) {
                                                                                            }
                                                                                        });
                                                                                    }

                                                                                    StorageReference reference = storage.getReference("UserImage")
                                                                                            .child(image1Id);
                                                                                    reference.putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                        @Override
                                                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                            dialog.dismiss();
                                                                                            Toast.makeText(getActivity().getApplicationContext(), "Your profile has been updated successfully", Toast.LENGTH_LONG).show();
                                                                                        }
                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            dialog.dismiss();
                                                                                            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                                                        }
                                                                                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                                                        @Override
                                                                                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                                                                            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                                                                            dialog.setMessage("Image Uploading " + (int) progress + "%");
                                                                                        }
                                                                                    });

                                                                                } else {
                                                                                    Toast.makeText(getActivity().getApplicationContext(), "Your profile has been updated successfully", Toast.LENGTH_LONG).show();
                                                                                }

                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                dialog.dismiss();
                                                                                Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                            }

                                                        } else {

                                                        }
                                                    }
                                                });
                                    }

                                }
                            });
                }
            }
        });

        view.findViewById(R.id.resetPasswordBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.sendPasswordResetEmail(firebaseAuth.getCurrentUser().getEmail())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity().getApplicationContext(), "Your Password has been updated successfully", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "Please try again later", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        return view;

    }
    ActivityResultLauncher<Intent> activityResultLauncher1 = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        imagePath = result.getData().getData();
                        Log.i(TAG,"Image Path: "+imagePath.getPath());
                        Picasso.get()
                                .load(imagePath)
                                .fit()
                                .centerCrop()
                                .into(imageButton);
                    }
                }
            }
    );
}