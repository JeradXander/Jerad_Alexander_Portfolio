package com.alexanderapps.rubbersidedown.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.adapters.AdapterChatList;
import com.alexanderapps.rubbersidedown.dataModels.ModelChat;
import com.alexanderapps.rubbersidedown.dataModels.ModelChatlList;
import com.alexanderapps.rubbersidedown.dataModels.ModelUser;
import com.alexanderapps.rubbersidedown.listeners.SignOutListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatListFrag extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    Button signoutBut;

    SignOutListener mListener;
    String mUid;
    RecyclerView recyclerView;
    List<ModelUser> userList;
    List<ModelChatlList> chatlListList;
    DatabaseReference reference;
    FirebaseUser fUser;
    AdapterChatList adapterChatList;

    public static ChatListFrag newInstance() {

        Bundle args = new Bundle();

        ChatListFrag fragment = new ChatListFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chatlist_layout, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        //attaching listeners
        if (context instanceof SignOutListener) {
            mListener = (SignOutListener) context;
        } else {
            Log.e(TAG, "onAttach: " + context.toString() + " must implement MainFragment.Listener");
        }

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        fUser = mAuth.getCurrentUser();

        recyclerView = Objects.requireNonNull(getView()).findViewById(R.id.chatlist_recyclce);

        chatlListList = new ArrayList<>();
        userList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(fUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlListList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChatlList chatlList = ds.getValue(ModelChatlList.class);
                    chatlListList.add(chatlList);

                }
                loadchats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadchats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUser user = ds.getValue(ModelUser.class);

                    for (ModelChatlList chatlList : chatlListList) {
                        if (Objects.requireNonNull(user).getUid() != null && user.getUid().equals(chatlList.getId())) {
                            userList.add(user);
                            break;
                        }
                    }
                    adapterChatList = new AdapterChatList(getContext(), userList);
                    //setadapter
                    recyclerView.setAdapter(adapterChatList);
                    //get last message
                    for (int i = 0; i < userList.size(); i++) {
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat == null) {
                        continue;
                    } else {
                        String sender = chat.getSender();
                        String receiver = chat.getReceiver();

                        if (sender == null || receiver == null) {
                            continue;
                        }

                        if (chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userId) ||
                                chat.getReceiver().equals(userId) &&
                                        chat.getSender().equals(fUser.getUid())) {
                            theLastMessage = chat.getMessage();

                        }
                    }
                    adapterChatList.setLastMessageMap(userId, "Last Message: " + theLastMessage);
                    adapterChatList.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == signoutBut.getId()) {
            Toast.makeText(getContext(), "signout selected", Toast.LENGTH_SHORT).show();
            mListener.SignOutPressed();
        } else {
            Log.d(TAG, "shouldn't happen");
        }
    }
}
