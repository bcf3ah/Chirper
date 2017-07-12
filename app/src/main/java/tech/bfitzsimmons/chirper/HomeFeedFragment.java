package tech.bfitzsimmons.chirper;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SubscriptionHandling;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brian on 7/12/2017.
 */

public class HomeFeedFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_feed, container, false);

        //init homeFeed list of chirps
        final List<Chirp> homeFeed = new ArrayList<>();

        //init HomeFeedRecyclerView and its adapter
        RecyclerView homeFeedRecyclerView = (RecyclerView) view.findViewById(R.id.homeFeedRecyclerView);
        final ChirpAdapter chirpAdapter = new ChirpAdapter(homeFeed);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        homeFeedRecyclerView.setLayoutManager(manager);
        homeFeedRecyclerView.setAdapter(chirpAdapter);


        //fill homeFeed with 20 chirps from Parse
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Chirp");
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseObject chirp : objects) {
                            //format the time
                            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
                            String time = timeFormat.format(chirp.getCreatedAt());
                            homeFeed.add(new Chirp(chirp.getString("username"),
                                    chirp.getString("chirp"),
                                    time,
                                    chirp.getInt("likeCount")));
                        }
                    }
                }

                //notify the recyclerView adapter that we just populated the homefeed list
                chirpAdapter.notifyDataSetChanged();
            }
        });

        // Subscribe to new chirps
        SubscriptionHandling<ParseObject> subscriptionHandling = ParseApp.parseLiveQueryClient.subscribe(query);

        //change homeFeed when new chirp is made
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
            @Override
            public void onEvent(ParseQuery<ParseObject> query, ParseObject chirp) {
                //format the time
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
                String time = timeFormat.format(chirp.getCreatedAt());

                //add the incoming chirp to the homefeed (realtime)
                homeFeed.add(new Chirp(chirp.getString("username"),
                        chirp.getString("chirp"),
                        time,
                        chirp.getInt("likeCount")));

                //notify the adapter
                chirpAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }
}
