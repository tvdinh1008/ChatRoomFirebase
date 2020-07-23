package com.tvdinh.chatroomfirebase.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tvdinh.chatroomfirebase.Adapter.UserAdapter;
import com.tvdinh.chatroomfirebase.Model.Chat;
import com.tvdinh.chatroomfirebase.Model.User;
import com.tvdinh.chatroomfirebase.R;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    private List<String> userslist;//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView=view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userslist=new ArrayList<>();

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        reference= FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userslist.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    //lấy ra danh sách người mà mình gửi tin nhắn
                    Chat chat=snapshot.getValue(Chat.class);
                    if(chat.getSender().equals(firebaseUser.getUid()))
                    {
                        userslist.add(chat.getReceiver());
                    }
                    //lấy ra danh sách người người gửi tin nhắn cho mình
                    if(chat.getReceiver().equals(firebaseUser.getUid()))
                    {
                        userslist.add(chat.getSender());
                    }
                }
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    private  void readChats()
    {
        mUsers=new ArrayList<>();
        reference =FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                //danh sách user
                for (DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    User user=snapshot.getValue(User.class);

                    for(String id:userslist)
                    {
                        //nếu có user trùng trong list
                        if(user.getId().equals(id))
                        {
                            //ktra để không thêm 2 lần cho 1 user
                            if(mUsers.size()!=0)
                            {
                                //mUsers danh sách user đã chat với mình
                                for(User item:mUsers)
                                {
                                    if(!user.getId().equals(item.getId()))
                                    {
                                        mUsers.add(user);//array list ko cho add 2 đối tượng cùng 1 địa chỉ
                                    }
                                }

                            }
                            else
                            {
                                mUsers.add(user);
                            }
                        }
                    }
                }
                userAdapter=new UserAdapter(getContext(),mUsers,true);
                recyclerView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
