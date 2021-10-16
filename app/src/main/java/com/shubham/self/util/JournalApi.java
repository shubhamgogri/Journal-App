package com.shubham.self.util;

import android.app.Application;

import com.shubham.self.model.Journal;
import com.shubham.self.model.ThoughtAnalysisModel;

public class JournalApi extends Application {
    private String username;
    private String userId;
    private static JournalApi instance;
    private Journal j;

    public static JournalApi getInstance() {
        if (instance == null)
            instance = new JournalApi();
        return instance;

    }

    public Journal getJ() {
        return j;
    }

    public void setJ(Journal j) {
        this.j = j;
    }

    public JournalApi(){}


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
