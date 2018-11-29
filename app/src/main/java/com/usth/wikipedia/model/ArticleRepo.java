package com.usth.wikipedia.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ArticleRepo {
    private String articleName;
    private String articleDescription;
    private Bitmap articleImage;

    public ArticleRepo(String articleName) {
        this.articleName = articleName;
    }

    public ArticleRepo(String articleName, String articleDescription, Bitmap articleImage) {
        this.articleName = articleName;
        this.articleDescription = articleDescription;
        this.articleImage = articleImage;
    }

    public String getName() {
        return this.articleName;
    }

    public String getDescription() {
        return this.articleDescription;
    }

    public Bitmap getImage() {
        return this.articleImage;
    }
}

