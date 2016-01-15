package com.androidapp.test.facebooktask.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidapp.test.facebooktask.R;
import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = UserProfileActivity.class.getName();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_ASK_CAMERA_PERMISSIONS = 2;

    private ImageView profilePictureView;
    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profilePictureView = (ImageView) findViewById(R.id.user_profile_picture);
        profilePictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //request camera permission in android api 23+
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_ASK_CAMERA_PERMISSIONS);
                        return;
                    }
                }
                dispatchTakePictureIntent();
            }
        });
        userName = (TextView) findViewById(R.id.user_name);

        GraphRequest userProfileRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.i(TAG, response.toString());
                JSONObject obj = response.getJSONObject();
                try {
                    //creating animation
                    AnimationDrawable spinnerAnimation;
                    ImageView rocketImage = new ImageView(getApplicationContext());
                    rocketImage.setBackgroundResource(R.drawable.loading_spinner);
                    spinnerAnimation = (AnimationDrawable) rocketImage.getBackground();
                    spinnerAnimation.start();

                    Glide.with(getApplicationContext())
                            .load(String.format("http://graph.facebook.com/%s/picture?type=large", obj.getString("id")))
                            .placeholder(spinnerAnimation)
                            .into(profilePictureView);

                    userName.setText(obj.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture");
        userProfileRequest.setParameters(parameters);
        userProfileRequest.executeAsync();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profilePictureView.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ASK_CAMERA_PERMISSIONS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Manifest.permission.CAMERA granted");
                    dispatchTakePictureIntent();
                } else {
                    Log.i(TAG, "Manifest.permission.CAMERA denied");
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.camera_perm_denied), Toast.LENGTH_LONG).show();
                }
                break;
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}
