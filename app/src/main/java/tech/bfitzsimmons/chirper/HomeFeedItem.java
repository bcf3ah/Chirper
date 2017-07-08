package tech.bfitzsimmons.chirper;

/**
 * Created by Brian on 7/7/2017.
 */

public class HomeFeedItem {
    private String username;
    private String chirp;
    private String time;
    private int likeCount;

    public HomeFeedItem(String username, String chirp, String time, int likeCount) {
        this.username = username;
        this.chirp = chirp;
        this.time = time;
        this.likeCount = likeCount;
    }

    public String getUsername() {
        return username;
    }

    public String getChirp() {
        return chirp;
    }

    public String getTime() {
        return time;
    }

    public int getLikeCount() {
        return likeCount;
    }
}
