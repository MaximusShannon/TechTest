package showoff.techtest;

public class RecentPost {

    private String url;
    private String caption;
    private int likeCount;

    public RecentPost(String url, String caption, int likeCount)
    {
        this.url = url;
        this.caption = caption;
        this.likeCount = likeCount;
    }

    public String getUrl() {
        return url;
    }

    public String getCaption() {
        return caption;
    }

    public int getLikeCount() {
        return likeCount;
    }
}
