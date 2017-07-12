package tech.bfitzsimmons.chirper;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Brian on 7/7/2017.
 */

public class ChirpAdapter extends RecyclerView.Adapter<ChirpAdapter.ViewHolder> {
    private List<Chirp> chirps;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chirp, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Chirp item = chirps.get(position);
        holder.username.setText(item.getUsername());
        holder.chirp.setText(item.getChirp());
        holder.time.setText(item.getTime());
        holder.likeCount.setText(String.valueOf(item.getLikeCount()));
    }

    @Override
    public int getItemCount() {
        return chirps.size();
    }


    //create ViewHolder for this class
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView username, chirp, time, likeCount;
        private ImageButton likeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.chirp_username);
            chirp = (TextView) itemView.findViewById(R.id.chirp_text);
            time = (TextView) itemView.findViewById(R.id.chirp_time);
            likeCount = (TextView) itemView.findViewById(R.id.chirp_like_count);
            likeButton = (ImageButton) itemView.findViewById(R.id.chirp_like_button);

            //set click listener for likeButton
            likeButton.setOnClickListener(this);

            //set click listener for whole view, which will take them to the details activity
            itemView.setOnClickListener(this);
        }

        //define click listener for likeButton
        @Override
        public void onClick(final View v) {

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Chirp");
            query.whereEqualTo("chirp", chirp.getText().toString());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                            for (ParseObject chirp : objects) {
                                if (v.getId() == likeButton.getId()) {
                                    //user clicked the start, so do like logic
                                    //get the current Like count and increment it
                                    int currentLikeCount = chirp.getInt("likeCount");
                                    int updatedLikeCount = currentLikeCount + 1;
                                    //update the UI first for nice UX
                                    likeCount.setText(String.valueOf(updatedLikeCount));

                                    //now udpate the database
                                    chirp.put("likeCount", updatedLikeCount);
                                    chirp.saveInBackground();
                                } else {
                                    //user clicked the chirp, so go to chirper's profile if we're still in HomeActvity
                                    if (v.getContext() instanceof DashboardActivity) {
                                        Intent intent = new Intent(v.getContext(), UserProfile.class);
                                        intent.putExtra("chirperUsername", chirp.getString("username"));
                                        v.getContext().startActivity(intent);
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    public ChirpAdapter(List<Chirp> chirps) {
        this.chirps = chirps;
    }
}
