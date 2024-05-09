package com.example.panicalert;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MediaController;

public class CustomMediaController extends MediaController {

    public CustomMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAnchorView(android.view.View view) {
        // Do nothing, to prevent MediaController from trying to access the anchor view
    }

    @Override
    public void show(int timeout) {
        // Do nothing, to prevent showing controls
    }

    @Override
    public void show() {
        // Do nothing, to prevent showing controls
    }

    @Override
    public void hide() {
        // Do nothing, to prevent hiding controls
    }
}
