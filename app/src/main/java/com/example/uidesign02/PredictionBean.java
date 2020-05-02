package com.example.uidesign02;

import java.util.Date;

public class PredictionBean {

    private int id;
    private String dangerlevel;
    private String suggestiontext;
    private String predictedTime;
    private byte[] image;

    public PredictionBean(int id, String dangerlevel, String suggestiontext, String  predictedTime, byte[] image){
        this.id = id;
        this.dangerlevel = dangerlevel;
        this.suggestiontext = suggestiontext;
        this.predictedTime = predictedTime;
        this.image = image;
    }

/*
    public PredictionBean(int id, String dangerlevel, String suggestiontext, byte[] image){
        this.id = id;
        this.dangerlevel = dangerlevel;
        this.suggestiontext = suggestiontext;
        this.image = image;
    }

 */
    public int getId(){
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getDangerlevel() {
        return dangerlevel;
    }

    public void setDangerlevel(String dangerlevel) {
        this.dangerlevel = dangerlevel;
    }

    public String getSuggestiontext() {
        return suggestiontext;
    }

    public void setSuggestiontext(String suggestiontext) {
        this.suggestiontext = suggestiontext;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }



    public String getPredictedTime() {
        return predictedTime;
    }

    public void setPredictedTime(String predictedTime) {
        this.predictedTime = predictedTime;
    }



}
