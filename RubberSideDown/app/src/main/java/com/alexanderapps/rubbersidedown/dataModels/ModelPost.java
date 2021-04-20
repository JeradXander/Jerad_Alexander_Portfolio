package com.alexanderapps.rubbersidedown.dataModels;

public class ModelPost {

    String pid,pTitle,pBody,pLikes,pComments,pImage,pTime,uid,uEmail,uAvatarImage, uName;

    public ModelPost(String pid, String pTitle, String pBody, String pLikes,String pCommetns,String pImage, String pTime, String uid, String uEmail, String uAvatarImage, String uName) {
        this.pid = pid;
        this.pTitle = pTitle;
        this.pBody = pBody;
        this.pLikes = pLikes;
        this.pImage = pImage;
        this.pTime = pTime;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uAvatarImage = uAvatarImage;
        this.uName = uName;
        this.pComments = pCommetns;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getpTitle() {
        return pTitle;
    }

    public void setpTitle(String pTitle) {
        this.pTitle = pTitle;
    }

    public String getpBody() {
        return pBody;
    }

    public void setpBody(String pBody) {
        this.pBody = pBody;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuAvatarImage() {
        return uAvatarImage;
    }

    public void setuAvatarImage(String uAvatarImage) {
        this.uAvatarImage = uAvatarImage;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }
}
