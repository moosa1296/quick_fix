package com.example.hp.quickfixx.Model;

public class Job {
    private String JobId;
    private String address;
    private String assignedTo;
    private String coins;
    private String jobDescription;
    private String jobTitle;
    private String latitude;
    private String longitude;
    private String maxNoDays;
    private String postedBy;
    private String sponsoredBy;
    private String status;
    private String timeStamp;
    private String visible;
    private String voteDown;
    private String voteUp;
    private String ratings;

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }

    public void setTotalBids(String totalBids) {
        this.totalBids = totalBids;
    }

    public String getTotalBids() {
        return totalBids;
    }

    private String totalBids;

    public String getJobId() {
        return JobId;
    }

    public String getAddress() {
        return address;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getCoins() {
        return coins;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getMaxNoDays() {
        return maxNoDays;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public String getSponsoredBy() {
        return sponsoredBy;
    }

    public String getStatus() {
        return status;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getVisible() {
        return visible;
    }

    public String getVoteDown() {
        return voteDown;
    }

    public String getVoteUp() {
        return voteUp;
    }

    public void setJobId(String jobId) {
        JobId = jobId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setCoins(String coins) {
        this.coins = coins;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setMaxNoDays(String maxNoDays) {
        this.maxNoDays = maxNoDays;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public void setSponsoredBy(String sponsoredBy) {
        this.sponsoredBy = sponsoredBy;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public void setVoteDown(String voteDown) {
        this.voteDown = voteDown;
    }

    public void setVoteUp(String voteUp) {
        this.voteUp = voteUp;
    }
}
