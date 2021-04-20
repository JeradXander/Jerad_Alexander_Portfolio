package com.alexanderapps.rubbersidedown.dataModels;

public class ModelUser {

    private String uid;
    private String Fir;
    private String email;
   private String motorcyle;
    private String search;
  private  String location;
   private String likes;
    private String posts;
   private  String image;
   private String cover;
   private String years;
    private String onlineStatus;
   private String typingTo;

   public ModelUser(){

   }

    public ModelUser(String uid, String name, String email, String motorcyle, String search, String location, String likes, String posts, String image, String cover, String years, String onlineStatus, String typingTo) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.motorcyle = motorcyle;
        this.search = search;
        this.location = location;
        this.likes = likes;
        this.posts = posts;
        this.image = image;
        this.cover = cover;
        this.years = years;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotorcyle() {
        return motorcyle;
    }

    public void setMotorcyle(String motorcyle) {
        this.motorcyle = motorcyle;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getPosts() {
        return posts;
    }

    public void setPosts(String posts) {
        this.posts = posts;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getYears() {
        return years;
    }

    public void setYears(String years) {
        this.years = years;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }
}
