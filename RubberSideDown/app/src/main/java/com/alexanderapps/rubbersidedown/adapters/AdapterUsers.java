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

import com.alexanderapps.rubbersidedown.activities.ChatActivity;
import com.alexanderapps.rubbersidedown.dataModels.ModelUser;
import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.listeners.ProfileSelectedListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    private static final String TAG = "sdf";
    final Context mContext;
    final List<ModelUser> userList;
    final ProfileSelectedListener profileSelectedListener;

    public AdapterUsers(Context mContext, List<ModelUser> userList, ProfileSelectedListener listener) {
        this.mContext = mContext;
        this.userList = userList;
        this.profileSelectedListener = listener;
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_user, parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final String selectedUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        final String userName = userList.get(position).getName();
        String userLocation = userList.get(position).getLocation();

        holder.mName.setText(userName);
        holder.mLocation.setText(userLocation);

        try{
            Picasso.get().load(userImage).placeholder(R.drawable.profile_place)
                    .resize(50, 50)
                    .transform(new CropCircleTransformation()).rotate(90)
                    .into(holder.avatarIm);
        }catch (Exception e){
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        holder.avatarIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileSelectedListener.theirProfileSelected(selectedUid);
            }
        });
        //handle holder click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(mContext, ChatActivity.class);
                chatIntent.putExtra("uid", selectedUid);
                mContext.startActivity(chatIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    static class MyHolder extends RecyclerView.ViewHolder{
        final ImageView avatarIm;
        final TextView mName;
        final TextView mLocation;



        public MyHolder(@NonNull View itemView){
            super(itemView);
            avatarIm = itemView.findViewById(R.id.user_avatar);
            mName = itemView.findViewById(R.id.card_name);
            mLocation = itemView.findViewById(R.id.user_location);
        }
    }
}
