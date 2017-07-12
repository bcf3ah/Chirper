package tech.bfitzsimmons.chirper;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseLiveQueryClient;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SubscriptionHandling;

import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserProfile extends AppCompatActivity {
    //Init UserFeed list
    List<Chirp> userChirps = new ArrayList<>();

    //Declare HomeFeed RecyclerView
    RecyclerView userChirpsRecyclerView;

    //Declare ChirpAdapter
    ChirpAdapter chirpAdapter;

    //declare FAB
    FloatingActionButton fab;

    //boolean to se if the user is already following this chirper
    boolean isFollowing = false;

    //declare ActionBar
    ActionBar actionBar;

    //Inflate top menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Add listener to top nav options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            //log the user out, go back to LoginActivity
            ParseUser.logOut();
            startActivity(new Intent(this, LoginActivity.class));
        } else if (item.getItemId() == R.id.chirpButton) {
            //build the alert dialog (doing it this way so we can dismiss it in custom layout button
            final AlertDialog chirpDialog = new AlertDialog.Builder(this).create();
            View view = getLayoutInflater().inflate(R.layout.chirp_dialog, null);

            //grab the chirp input, char count, char limit, and send button from the chirp dialog layout file
            final EditText chirp = (EditText) view.findViewById(R.id.chirp_dialog_input);
            final TextView chirpCharCount = (TextView) view.findViewById(R.id.chirp_dialog_char_count);
            final TextView chirpCharLimit = (TextView) view.findViewById(R.id.chirp_dialog_char_limit);
            ImageButton chirpButton = (ImageButton) view.findViewById(R.id.chirp_dialog_submit);

            //set click Listener for sendButton that will save chirp to Parse
            chirpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //save the chirp to Parse
                    ParseObject chirpObject = new ParseObject("Chirp");
                    chirpObject.put("chirp", chirp.getText().toString());
                    chirpObject.put("username", ParseUser.getCurrentUser().getUsername());
                    chirpObject.put("likeCount", 0);
                    chirpObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Toast.makeText(UserProfile.this, "Thanks for chirping!", Toast.LENGTH_SHORT).show();
                            chirpDialog.dismiss();
                        }
                    });
                }
            });

            //create onChangeListener for chirp EditText, to count characters
            chirp.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String currentCharCount = String.valueOf(s.length());
                    chirpCharCount.setText(currentCharCount);
                    if (s.length() > 130) {
                        chirpCharCount.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                        chirpCharLimit.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            //set the view and show the dialog
            chirpDialog.setView(view);
            chirpDialog.show();
        } else if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set up the back button for the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //capture the username from the intent
        Intent intent = getIntent();
        final String profileUsername = intent.getStringExtra("chirperUsername");

        //init the action bar
        actionBar = getSupportActionBar();
        actionBar.setTitle(profileUsername + "'s Chirps");

        //let's get the chirps for this user and subscribe to them (in case they chirp while viewing their profile)
        subscribeToUserChirps(profileUsername);

        //init UserChirpsRecyclerView and its adapter
        userChirpsRecyclerView = (RecyclerView) findViewById(R.id.user_profile_recyclerView);
        chirpAdapter = new ChirpAdapter(userChirps);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext());
        userChirpsRecyclerView.setLayoutManager(manager);
        userChirpsRecyclerView.setAdapter(chirpAdapter);

        //init the Follow fab
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followUser(profileUsername);
            }
        });

        //see if the current user is already following this chirper
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseUser user : objects) {
                            if (user.get("following") != null) {
                                //see if their following list includes this user
                                ArrayList<String> following = new ArrayList<String>();
                                following.addAll((Collection<? extends String>) user.get("following"));

                                if (following.contains(profileUsername)) {
                                    //if the array contains the profileUsername, then we are already following this user
                                    isFollowing = true;
                                    fab.setImageResource(R.drawable.unfollow);
                                } else {
                                    //not following, so set the fab to have the follow icon
                                    fab.setImageResource(R.drawable.follow);
                                }
                            } else {
                                //not following because no followings at all, so set the fab to have the follow icon
                                fab.setImageResource(R.drawable.follow);
                            }
                        }
                    }
                }
            }
        });

    }

    //convenience method to subscribe to future chirps and fill RecyclerView
    public void subscribeToUserChirps(final String username) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Chirp");
        query.whereEqualTo("username", username);
        query.orderByDescending("createdAt");

        //first let's grab all the chirps
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseObject chirp : objects) {
                            //add all the user's chirps to the userFeed

                            //format the time
                            SimpleDateFormat timeFormat = new SimpleDateFormat("MMM d, h:mm a");
                            String time = timeFormat.format(chirp.getCreatedAt());

                            //add the incoming chirp to the user feed initially
                            userChirps.add(new Chirp(username,
                                    chirp.getString("chirp"),
                                    time,
                                    chirp.getInt("likeCount")));

                            //notify the adapter
                            chirpAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        //now let's subscribe to them
        SubscriptionHandling<ParseObject> subscriptionHandling = ParseApp.parseLiveQueryClient.subscribe(query);
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
            @Override
            public void onEvent(ParseQuery<ParseObject> query, ParseObject chirp) {
                //format the time
                SimpleDateFormat timeFormat = new SimpleDateFormat("MMM d, h:mm a");
                String time = timeFormat.format(chirp.getCreatedAt());

                //add the incoming chirp to the user feed (realtime)
                userChirps.add(new Chirp(username,
                        chirp.getString("chirp"),
                        time,
                        chirp.getInt("likeCount")));

                //notify the adapter
                chirpAdapter.notifyDataSetChanged();
            }
        });

    }

    public void followUser(final String userToFollow) {
        //establish the current user
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (!isFollowing) {
            //save the userToFollow in the currentUser's 'following' array
            currentUser.add("following", userToFollow);
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(UserProfile.this, "You are now following " + userToFollow, Toast.LENGTH_SHORT).show();
                        fab.setImageResource(R.drawable.unfollow);
                    }
                }
            });

            //set isFollowing boolean to true
            isFollowing = true;
        } else {
            //remove the userToFollow from the currentUser's 'following' array
            Collection<String> removeMe = new ArrayList<>();
            removeMe.add(userToFollow);
            currentUser.removeAll("following", removeMe);
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(UserProfile.this, "You are no longer following " + userToFollow, Toast.LENGTH_SHORT).show();
                        fab.setImageResource(R.drawable.follow);
                    }
                }
            });

            //set isFollowing boolean to false
            isFollowing = false;
        }
    }
}
