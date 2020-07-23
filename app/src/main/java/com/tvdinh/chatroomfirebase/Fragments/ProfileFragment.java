package com.tvdinh.chatroomfirebase.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.tvdinh.chatroomfirebase.Model.User;
import com.tvdinh.chatroomfirebase.R;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    CircleImageView image_profile;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference reference;


    //image upload
    StorageReference storageReference;
    private static final int IMAGE_REQUEST=1111;
    private Uri imageUri;
    private StorageTask uploadTask;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_profile, container, false);

        image_profile=view.findViewById(R.id.profile_image);
        username=view.findViewById(R.id.username);


        storageReference= FirebaseStorage.getInstance().getReference("uploads");


        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user=dataSnapshot.getValue(User.class);
                if(user.getImageURL().equals("default"))
                {
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                }
                else
                {
                    Glide.with(getContext()).load(user.getImageURL()).into(image_profile);
                }
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });


        return view;
    }



    //upload ảnh từ máy của mình lên firebase vào storage(chú ý để quyền=true)
    private void openImage()
    {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }
    private String getFileExtension(Uri uri)
    {
        ContentResolver contentResolver=getContext().getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void uploadImage()
    {
        final ProgressDialog dialog=new ProgressDialog(getContext());
        dialog.setMessage("Uploading");
        dialog.show();
        if(imageUri!=null)
        {
            final StorageReference fileReference=storageReference.child(System.currentTimeMillis()
            +"."+getFileExtension(imageUri));
            uploadTask=fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        throw  task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful())
                    {
                        //cập nhật lại ảnh cho user nếu đẩy lên storage trên firebase thành công
                        Uri downloadUri= (Uri) task.getResult();
                        String mUri=downloadUri.toString();
                        reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                        HashMap<String,Object> map=new HashMap<>();
                        map.put("imageURL",mUri);
                        reference.updateChildren(map);

                        dialog.dismiss();
                    }
                    else
                    {
                        Toast.makeText(getContext(),"Failed!",Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });

        }else
        {
            Toast.makeText(getContext(),"No image selected",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==IMAGE_REQUEST && resultCode==getActivity().RESULT_OK && data!=null && data.getData()!=null)
        {
            imageUri=data.getData();
            if(uploadTask!=null&& uploadTask.isInProgress())
            {
                Toast.makeText(getContext(),"Upload in preopress",Toast.LENGTH_LONG).show();
            }
            else
            {
                uploadImage();
            }
        }


    }
}
