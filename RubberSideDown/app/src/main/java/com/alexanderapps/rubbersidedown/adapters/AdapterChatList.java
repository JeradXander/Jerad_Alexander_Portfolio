package com.alexanderapps.rubbersidedown.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.activities.ChatActivity;
import com.alexanderapps.rubbersidedown.dataModels.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.content.ContentValues.TAG;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.myHolder>{
    final Context mContext;
    final List<ModelUser> chatList;
    private final HashMap<String, String> lastMessageMap;


    public AdapterChatList(Context mContext, List<ModelUser> chatList) {
        this.mContext = mContext;
        this.chatList = chatList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.row_chatlist,parent,false);
            return new myHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
        //setting value for and setting uo view
        final String theirUid = chatList.get(position).getUid();
        String userImage = chatList.get(position).getImage();
        String userName = chatList.get(position).getName();
        String lastMessageValue = lastMessageMap.get(theirUid);
        holder.nameTV.setText(userName);
        if(lastMessageValue == null || lastMessageValue.equals("default")){
            holder.lastMessageTV.setVisibility(View.GONE);
        }else {
            holder.lastMessageTV.setVisibility(View.VISIBLE);
            holder.lastMessageTV.setText(lastMessageValue);
        }
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.add_photo)
                    .transform(new CropCircleTransformation()).rotate(90)
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
    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId,lastMessage );
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }



    static class myHolder extends RecyclerView.ViewHolder{

        final ImageView profileIV;
        final ImageView onlineStatus;
        final TextView nameTV;
        final TextView lastMessageTV;

        public myHolder(@NonNull View itemView) {
            super(itemView);

            profileIV = itemView.findViewById(R.id.profile_avatar);
            onlineStatus = itemView.findViewById(R.id.user_online_status);
            nameTV = itemView.findViewById(R.id.uNameTV);
            lastMessageTV = itemView.findViewById(R.id.last_messagetV);
        }
    }
}
