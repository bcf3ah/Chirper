package tech.bfitzsimmons.chirper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    //Declare views
    ProgressBar progressBar;
    ImageView icon;
    EditText username;
    EditText password;
    LinearLayout authForm;
    Switch authSwitch;

    //boolean for showing progressBar or not
    boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //init views
        progressBar = (ProgressBar) findViewById(R.id.loginProgressBar);
        icon = (ImageView) findViewById(R.id.loginIcon);
        username = (EditText) findViewById(R.id.usernameForm);
        password = (EditText) findViewById(R.id.passwordForm);
        authForm = (LinearLayout) findViewById(R.id.authForm);
        authSwitch = (Switch) findViewById(R.id.authSwitch);

        //hide action bar for full screen
        getSupportActionBar().hide();

        //see if user is logged in from last time
        if (ParseUser.getCurrentUser() != null) {
            //go to next activity
            goToHome();
        }
    }

    //Login or register to Parse
    public void authenticate(View view) {
        //Set isLoading to true so we can then hide the auth form, show the progress bar until appropriate callback is done
        isLoading = true;
        updateUI();

        //if switch is on, register
        if (authSwitch.isChecked()) {

            final ParseUser user = new ParseUser();

            //use email as username
            user.setUsername(username.getText().toString());

            //get password too
            user.setPassword(password.getText().toString());

            //register the user
            user.signUpInBackground(new SignUpCallback() {

                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        isLoading = false;
                        //let user know sign up was successful
                        Toast.makeText(LoginActivity.this, "Successfully registered", Toast.LENGTH_SHORT).show();
                        //go to Dashboard
                        goToHome();
                    } else {
                        //register was unsuccessful, so tell the user and show the form again
                        isLoading = false;
                        updateUI();
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            ParseUser.logInInBackground(username.getText().toString(), password.getText().toString(), new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        //show success message
                        Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                        //go to home activity
                        goToHome();
                    } else {
                        //login unsuccessful. Show form again and give error message
                        isLoading = false;
                        updateUI();
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }

    }

    //method for handling login UI progress bar
    public void updateUI() {
        if (isLoading) {
            authForm.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            icon.setVisibility(View.INVISIBLE);
        } else {
            authForm.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            icon.setVisibility(View.VISIBLE);
        }
    }

    //method to go to Dashboard
    public void goToHome() {
        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
        startActivity(intent);
    }
}

