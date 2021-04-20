package com.jerad_zac.solace.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.jerad_zac.solace.R;
import com.jerad_zac.solace.activities.ChatActivity;
import com.jerad_zac.solace.data_model.ChatList_Data;
import com.jerad_zac.solace.data_model.User_Data;
import com.jerad_zac.solace.listeners.BlockListener;
import com.jerad_zac.solace.listeners.SignOutListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class Chatlist_Adapter extends RecyclerView.Adapter<Chatlist_Adapter.myHolder> {
    private static final String TAG = "CustomRecycleAdapter";
    final Context mContext;
    final List<User_Data> chatList;
    private final HashMap<String, String> lastMessageMap;
    String myUid;
    BlockListener mListener;


    public Chatlist_Adapter(Context mContext, String myUID, List<User_Data> chatList, BlockListener blockListener) {
        this.mContext = mContext;
        this.myUid = myUID;
        this.chatList = chatList;
        lastMessageMap = new HashMap<>();
        this.mListener = blockListener;

    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.chatter_selection_view,parent,false);
        return new myHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final myHolder holder, final int position) {
        //setting value for and setting uo view
        final String theirUid = chatList.get(position).getUid();
        String userImage = chatList.get(position).getImage();
        String userName = chatList.get(position).getUsername();
        String lastMessageValue = lastMessageMap.get(theirUid);
        holder.nameTV.setText(userName);
        if(lastMessageValue == null || lastMessageValue.equals("default")){
            holder.lastMessageTV.setVisibility(View.GONE);
        }else {
            holder.lastMessageTV.setVisibility(View.VISIBLE);
            holder.lastMessageTV.setText(lastMessageValue);
        }
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.addphoto)
                    .transform(new CropCircleTransformation())
                    .into(holder.profileIV);
        }catch (Exception e){
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        //set online status
        if (chatList.get(position).getOnlineStatus().equals("online")){
            //online
            holder.onlineStatus.setImageResource(R.drawable.online_shape);
        }else {
            holder.onlineStatus.setImageResource(R.drawable.offline_shape);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("uid",theirUid);
                mContext.startActivity(intent);
            }
        });


        // This is the report/block logic.
        holder.optionsTVBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(mContext, holder.optionsTVBtn);
                //inflating menu from xml resource
                popup.inflate(R.menu.chatter_options_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu1) {//handle menu1 click

                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                            builder.setTitle("Are you sure you wish to permanently block this user?");

                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    blockUser(chatList.get(position).getUid());
                                }
                            });
                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            builder.setCancelable(true);
                            builder.create();
                            builder.show();

                            return true;
                        }
                        Log.d(TAG, "Chatlist_Adpater - onMenuItemClick: This should not being happening...");
                        return false;
                    }
                });
                //displaying the popup
                popup.show();

            }
        });
    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId,lastMessage );
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    private void blockUser(final String thisUid){
        Log.d(TAG, "blockUser: " + thisUid);
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(myUid).child("ChatList").child(thisUid);

        HashMap<String, Object> hasmap = new HashMap<>();
        hasmap.put("id", thisUid);
        hasmap.put("blocked", true);

        chatRef.updateChildren(hasmap);

        HashMap<String, Object> reportMap = new HashMap<>();
        reportMap.put("timesReported",ServerValue.increment(1));

        FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        DatabaseReference ref = fdb.getReference("Reports").child(thisUid);
        ref.setValue(reportMap);

    }


    static class myHolder extends RecyclerView.ViewHolder{

        final ImageView profileIV;
        final ImageView onlineStatus;
        final TextView nameTV;
        final TextView lastMessageTV;
        final TextView optionsTVBtn;
        public myHolder(@NonNull View itemView) {
            super(itemView);

            profileIV = itemView.findViewById(R.id.avatarIV);
            onlineStatus = itemView.findViewById(R.id.onlineStatusIV);
            nameTV = itemView.findViewById(R.id.nameTV);
            lastMessageTV = itemView.findViewById(R.id.lastMessageTV);
            optionsTVBtn = itemView.findViewById(R.id.textViewOptions);
        }
    }
}
