package com.shubham.self.model;

import java.util.List;
import java.util.Map;

public class ThoughtAnalysisModel {

    private String sentiment;
    private String emotion;
    public List<String> parts_of_speech;
    private String entity;

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public List<String> getParts_of_speech() {
        return parts_of_speech;
    }

    public void setParts_of_speech(List<String> parts_of_speech) {
        this.parts_of_speech = parts_of_speech;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public ThoughtAnalysisModel(String sentiment, String emotion, List<String> parts_of_speech, String entity) {
        this.sentiment = sentiment;
        this.emotion = emotion;
        this.parts_of_speech = parts_of_speech;
        this.entity = entity;
    }
}
