package com.alexanderapps.rubbersidedown.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alexanderapps.rubbersidedown.R;
import com.alexanderapps.rubbersidedown.activities.PostDetailActivity;
import com.alexanderapps.rubbersidedown.dataModels.ModelPost;
import com.alexanderapps.rubbersidedown.listeners.ProfileSelectedListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.content.ContentValues.TAG;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.myHolder> {
    final Context mContext;
    final List<ModelPost> postList;
    final ProfileSelectedListener profileSelectedListener;
    final String mUid;
    Dialog progressDialog;

    final FirebaseAuth mAuth;
    private final DatabaseReference likeRef;
    private final DatabaseReference postRef;

    boolean mProcessLike = false;


    public AdapterPosts(Context mContext, List<ModelPost> postList, ProfileSelectedListener listener) {
        this.mContext = mContext;
        this.postList = postList;
        this.profileSelectedListener = listener;
        mAuth = FirebaseAuth.getInstance();
        mUid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");


    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


            View view = LayoutInflater.from(mContext).inflate(R.layout.row_posts,parent,false);
            return new myHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final myHolder holder, final int position) {

        setProgress();
        final String uid = postList.get(position).getUid();
        String uName = postList.get(position).getuName();
        String uAvatar = postList.get(position).getuAvatarImage();
        final String pId = postList.get(position).getPid();
        String pTitle = postList.get(position).getpTitle();
        String pBody = postList.get(position).getpBody();
        final String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes();
        String pComments = postList.get(position).getpComments();

        //contatins total number of likes

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.uNameTV.setText(uName);
        holder.pTimeTV.setText(pTime);
        holder.pTitleTV.setText(pTitle);
        holder.pBodyTV.setText(pBody);
        holder.uNameTV.setText(uName);
        holder.pLikesTV.setText(pLikes+ " Likes");
        holder.pCommentsTV.setText(pComments+ " Comments");
        setLikes(holder,pId);


        try{
            Picasso.get().load(uAvatar).placeholder(R.drawable.back)
                    .resize(100, 100)
                    .transform(new CropCircleTransformation()).rotate(90)
                    .into(holder.profileIV);
        }catch (Exception e){
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        try{
            Picasso.get().load(pImage).placeholder(R.drawable.add_photo)
                    .resize(380, 380)
                    .rotate(90)
                    .into(holder.postIV);
        }catch (Exception e){
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        if(uid.equals(mUid)){
            holder.moreBT.setVisibility(View.VISIBLE);
            holder.moreBT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"more", Toast.LENGTH_SHORT).show();
                    showMoreOptions(holder.moreBT, uid, mUid,pId,pImage);
                }
            });
        }else{
            holder.moreBT.setVisibility(View.GONE);
        }


        holder.likeBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;

                final String postIds = postList.get(position).getPid();
                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(mProcessLike){
                            if(snapshot.child(postIds).hasChild(mUid)){
                                //ALREADYLIKED SO REMOVE LIKE
                                postRef.child(postIds).child("pLikes").setValue(""+(pLikes -1));
                                likeRef.child(postIds).child(mUid).removeValue();
                                mProcessLike =false;
                            }
                            else{
                                //not liked so like it
                                postRef.child(postIds).child("pLikes").setValue(""+(pLikes + 1 ));
                                likeRef.child(postIds).child(mUid).setValue("Liked");
                                mProcessLike = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        holder.commentBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start post detail
                Intent intent = new Intent(mContext, PostDetailActivity.class);
                intent.putExtra("postId",pId);
                mContext.startActivity(intent);


            }
        });



        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileSelectedListener.theirProfileSelected(uid);
            }
        });
    }

    private void setLikes(final myHolder holder, final String postKey) {
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postKey).hasChild(mUid)){
                    //user has Liked this post
                    holder.likeBT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked_24,0,0,0);
                    holder.likeBT.setText("Liked");
                }
                else{
                    //user has not Liked this post
                    holder.likeBT.setCompoundDrawablesWithIntrinsicBounds(R.drawable.thumbs,0,0,0);
                    holder.likeBT.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    private void showMoreOptions(ImageButton moreBT, String uid, String mUid, final String pId, final String pImage){
        final PopupMenu popupMenu = new PopupMenu(mContext,moreBT, Gravity.END);

        if(uid.equals(mUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == 0) {
                        deletePost(pId,pImage);
                    }
                    return false;
                }
            });
            //show menu
            popupMenu.show();
        }

    }

    private void deletePost(final String pId, String pImage) {
        progressDialog.show();

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                //image deleted
                Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            ds.getRef().removeValue();//remove values from firebase where pid matches
                            Toast.makeText(mContext,"Deleted successfully",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(mContext,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //registration_progress bar builder method
    private void setProgress() {
        if("".equals("login")){
            //setting up dialog box to hold progressbar and text view
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setView(R.layout.login_progress);
            progressDialog = builder.create();
        }else if("".equals("recover")){
            //setting up dialog box to hold progressbar and text view
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setView(R.layout.recovering_progress);
            progressDialog = builder.create();
        }else {
            //setting up dialog box to hold progressbar and text view
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setView(R.layout.updating_progress);
            progressDialog = builder.create();
        }
    }


    static class myHolder extends RecyclerView.ViewHolder{

        final ImageView profileIV;
        final ImageView postIV;
        final TextView uNameTV;
        final TextView pTimeTV;
        final TextView pTitleTV;
        final TextView pBodyTV;
        final TextView pLikesTV;
        final TextView pCommentsTV;
        final ImageButton moreBT;
        final Button likeBT;
        final Button commentBT;
        final LinearLayout profileLayout;

        public myHolder(@NonNull View itemView) {
            super(itemView);

            profileIV = itemView.findViewById(R.id.post_avatar);
            postIV = itemView.findViewById(R.id.post_pImageIV);
            uNameTV = itemView.findViewById(R.id.uNameTV);
            pTimeTV = itemView.findViewById(R.id.pTimeTV);
            pTitleTV = itemView.findViewById(R.id.pTitleTV);
            pBodyTV = itemView.findViewById(R.id.pBodyTV);
            pLikesTV = itemView.findViewById(R.id.pLikesTV);
            pCommentsTV = itemView.findViewById(R.id.pCommentTV);
            moreBT = itemView.findViewById(R.id.post_more);
            likeBT = itemView.findViewById(R.id.pLikeBT);
            commentBT = itemView.findViewById(R.id.pCommentBT);

            profileLayout = itemView.findViewById(R.id.profile_layout);



        }
    }
}
