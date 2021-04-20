package com.alexanderapps.rubbersidedown.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.dataModels.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.content.ContentValues.TAG;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.myHolder>{
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    final Context mContext;
    final List<ModelChat> chatList;
    final String imageUri;

    FirebaseUser fUser;

    public AdapterChat(Context mContext, List<ModelChat> chatList, String imageUri) {
        this.mContext = mContext;
        this.chatList = chatList;
        this.imageUri = imageUri;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.row_chat_right,parent,false);
            return new myHolder(view);
        }else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.row_chat_left,parent,false);
            return new myHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
        //get data and set up view
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("hh:mm aa",cal).toString();

        holder.messafeTC.setText(message);
        holder.timeTV.setText(dateTime+ "              ");

        try{
            Picasso.get().load(imageUri).placeholder(R.drawable.add_photo)
                    .transform(new CropCircleTransformation()).rotate(90)
                    .into(holder.profileIV);
        }catch (Exception e){
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        if(position ==  chatList.size() - 1){
            holder.isSeenTV.setVisibility(View.VISIBLE);
            if(chatList.get(position).isSeen()){
                holder.isSeenTV.setText(R.string.seesbool);
            }
            else {
                holder.isSeenTV.setText(R.string.delivered);
            }
        }else {
            holder.isSeenTV.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //checking for view type
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

    static class myHolder extends RecyclerView.ViewHolder{

        //init views
        final ImageView profileIV;
        final TextView messafeTC;
        final TextView timeTV;
        final TextView isSeenTV;

        public myHolder(@NonNull View itemView) {
            super(itemView);

            //setting views
            profileIV = itemView.findViewById(R.id.profile_IV);
            messafeTC = itemView.findViewById(R.id.messageTV);
            timeTV = itemView.findViewById(R.id.message_time);
            isSeenTV = itemView.findViewById(R.id.is_seen);
        }
    }
}
