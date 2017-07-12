package tech.bfitzsimmons.chirper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brian on 7/12/2017.
 */

public class FollowingFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.following, container, false);

        //init homeFeed list of chirps
        final ArrayList<String> followingList = new ArrayList<>();

        //init ListView and its adapter
        final ListView listView = (ListView) view.findViewById(R.id.my_profile_following_listView);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(container.getContext(), android.R.layout.simple_list_item_1, followingList);
        listView.setAdapter(adapter);

        //get the following list of this user from Parse
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        ParseUser thisUser = objects.get(0);
                        if (thisUser.get("following") != null) {
                            for (Object chirper : thisUser.getList("following")) {
                                followingList.add(chirper.toString());
                            }
                        }

                        //notify the adapter
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        //set click listener for each followingList item, which should take the user to the appropriate user profile via chirperUsername in the intent
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String chirperUsername = followingList.get(position);
                Intent intent = new Intent(view.getContext(), UserProfile.class);
                intent.putExtra("chirperUsername", chirperUsername);
                startActivity(intent);
            }
        });


        return view;
    }
}
