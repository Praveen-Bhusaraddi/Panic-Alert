package com.example.panicalert;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class splashmain extends AppCompatActivity {

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashmain);

        mAuth = FirebaseAuth.getInstance();


        // Get a reference to the VideoView
        VideoView videoView = findViewById(R.id.videoView);

        // Set the path of the video file
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sp_vid2;

        // Parse the video URI
        Uri uri = Uri.parse(videoPath);

        // Set the URI to the VideoView
        videoView.setVideoURI(uri);

        // Create a MediaController
        CustomMediaController mediaController = new CustomMediaController(this, null);
        mediaController.setAnchorView(videoView);

        // Set CustomMediaController to VideoView
        videoView.setMediaController(mediaController);

        // Start playing the video
        videoView.start();

        // Set a listener to go to the next activity once the video finishes
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Start your next activity here
                checkCurrentUser();
                finish();
            }
        });
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(splashmain.this, MainActivity.class));
        } else {
            startActivity(new Intent(splashmain.this, LoginMain.class));
        }
        finish();
    }

}
