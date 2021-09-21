package com.example.syncd;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.awt.font.TextAttribute;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 100;
    SignInButton signInButton;
    private LoginButton login_button;
    private ImageView imageView;
    private CallbackManager callbackManager;
    private TextView fb_name, fb_email, welcome, login_with;
    private ImageView image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        image=findViewById(R.id.image);
        welcome = findViewById(R.id.welcomeText);
        login_with = findViewById(R.id.login_with);
        imageView = findViewById(R.id.fb_pic);
        fb_name = findViewById(R.id.fb_name);
        fb_email = findViewById(R.id.fb_email);

        login_button = findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();

        login_button.setPermissions(Arrays.asList("email", "public_profile"));
        checkLoginStatus();
        login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(MainActivity.this, "Logged In successfully", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(MainActivity.this, "Login error", Toast.LENGTH_LONG).show();
                    }
                });

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }


    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            if (currentAccessToken == null) {
                image.setVisibility(View.VISIBLE);
                signInButton.setVisibility(View.VISIBLE);
                welcome.setVisibility(View.VISIBLE);
                welcome.requestLayout();
                login_with.setVisibility(View.VISIBLE);
                login_with.requestLayout();
                fb_name.setText("");
                fb_email.setText("");
                imageView.setImageResource(0);
                Toast.makeText(MainActivity.this, "User Logged Out successfully", Toast.LENGTH_SHORT).show();
            } else {
                loaduserProfile(currentAccessToken);
            }
        }
    };

    private void loaduserProfile (AccessToken newAccessToken){
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    signInButton.setVisibility(View.GONE);
                    image.setVisibility(View.GONE);
                    welcome.setVisibility(View.GONE);
                    login_with.setVisibility(View.GONE);
                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");
                    Profile profile=Profile.getCurrentProfile();
                    String image_url=profile.getProfilePictureUri(300,300).toString();
                    Picasso.get().load(image_url).into(imageView);
                    fb_email.setText(email);
                    fb_name.setText(first_name + " " + last_name);
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.dontAnimate();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void checkLoginStatus () {
        if (AccessToken.getCurrentAccessToken() != null) {
            loaduserProfile(AccessToken.getCurrentAccessToken());
            Toast.makeText(MainActivity.this, "Checking user login status", Toast.LENGTH_LONG).show();
        }
    }


    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();


    private void signIn () {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else
        {
            callbackManager.onActivityResult(requestCode,resultCode,data);
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personEmail = acct.getEmail();
                Uri personPhoto = acct.getPhotoUrl();
            }
            startActivity(new Intent(MainActivity.this, GoogleLogInActivity.class));

        } catch (ApiException e) {

        }
    }
}