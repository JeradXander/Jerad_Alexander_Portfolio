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
import com.alexanderapps.rubbersidedown.dataModels.ModelComment;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.content.ContentValues.TAG;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {

    final Context mContext;
    final List<ModelComment> commentList;

    public AdapterComments(Context mContext, List<ModelComment> commentList) {
        this.mContext = mContext;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.row_comments,parent,false);


        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get the data
        String name = commentList.get(position).getuName();
        String uDp = commentList.get(position).getuDp();
        String comment = commentList.get(position).getComment();
        String timeStamp = commentList.get(position).getTimeStamp();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy",calendar).toString();

        //set the data
        holder.nameTV.setText(name);
        holder.commentsTV.setText(comment);
        holder.timeTV.setText(pTime);

        try{
            Picasso.get().load(uDp).placeholder(R.drawable.add_photo)
                    .resize(120, 120)
                    .transform(new CropCircleTransformation()).rotate(90)
                    .into(holder.avatarIv);
        }catch (Exception e){
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        //set the data
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        //declare view from row_commentrs

        final ImageView avatarIv;
        final TextView nameTV;
        final TextView commentsTV;
        final TextView timeTV;

        public MyHolder(@NonNull View itemView){
            super(itemView);
            avatarIv = itemView.findViewById(R.id.cAvatarIV);
            nameTV = itemView.findViewById(R.id.uNameTV);
            commentsTV = itemView.findViewById(R.id.CommentTV);
            timeTV = itemView.findViewById(R.id.timeTV);

        }
    }
}
