package tech.bfitzsimmons.chirper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class DashboardActivity extends AppCompatActivity {
//Init HomeFeed list
//    List<Chirp> homeFeed = new ArrayList<>();
    //Declare HomeFeed RecyclerView
//    RecyclerView homeFeedRecyclerView;

    //Declare ChirpAdapter
//    ChirpAdapter chirpAdapter;

    //Bottom navbar and fragments
    BottomNavigationView navigation;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final Fragment homeFeedFragment = new HomeFeedFragment();
    final Fragment myProfileFragment = new MyProfileFragment();


    //Inflate top menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_top_nav, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Add listener to top nav options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //if they press the chirp button, show dialog to write chirp!
        if (item.getItemId() == R.id.chirpButton) {
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
                            Toast.makeText(DashboardActivity.this, "Thanks for chirping!", Toast.LENGTH_LONG).show();
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
        } else if (item.getItemId() == R.id.logout) {
            //log the user out, go back to LoginActivity
            ParseUser.logOut();
            startActivity(new Intent(this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragmentManager.beginTransaction().replace(R.id.fragment_holder, homeFeedFragment, homeFeedFragment.getTag()).commit();
                    return true;
                case R.id.navigation_dashboard:
                    fragmentManager.beginTransaction().replace(R.id.fragment_holder, myProfileFragment, myProfileFragment.getTag()).commit();
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //set up bottom nav
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //default to HomeFeedFragment
        fragmentManager.beginTransaction().replace(R.id.fragment_holder, homeFeedFragment, homeFeedFragment.getTag()).commit();

//        //fill homeFeed with 20 chirps from Parse
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("Chirp");
//        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
//        query.setLimit(20);
//        query.findInBackground(new FindCallback<ParseObject>() {
//            @Override
//            public void done(List<ParseObject> objects, ParseException e) {
//                if(e == null){
//                    if(objects.size() > 0){
//                        for(ParseObject chirp: objects){
//                            //format the time
//                            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
//                            String time = timeFormat.format(chirp.getCreatedAt());
//                            homeFeed.add(new Chirp(chirp.getString("username"),
//                                    chirp.getString("chirp"),
//                                    time,
//                                    chirp.getInt("likeCount")));
//                        }
//                    }
//                }
//
//                //notify the recyclerView adapter that we just populated the homefeed list
//                chirpAdapter.notifyDataSetChanged();
//            }
//        });
//
//        // Subscribe to new chirps
//        SubscriptionHandling<ParseObject> subscriptionHandling = ParseApp.parseLiveQueryClient.subscribe(query);
//
//        //change homeFeed when new chirp is made
//        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
//            @Override
//            public void onEvent(ParseQuery<ParseObject> query, ParseObject chirp) {
//                //format the time
//                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
//                String time = timeFormat.format(chirp.getCreatedAt());
//
//                //add the incoming chirp to the homefeed (realtime)
//                homeFeed.add(new Chirp(chirp.getString("username"),
//                        chirp.getString("chirp"),
//                        time,
//                        chirp.getInt("likeCount")));
//
//                //notify the adapter
//                chirpAdapter.notifyDataSetChanged();
//            }
//        });

//        //init HomeFeedRecyclerView and its adapter
//        homeFeedRecyclerView = (RecyclerView) findViewById(R.id.homeFeedRecyclerView);
//        chirpAdapter = new ChirpAdapter(homeFeed);
//        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext());
//        homeFeedRecyclerView.setLayoutManager(manager);
//        homeFeedRecyclerView.setAdapter(chirpAdapter);

    }
}
