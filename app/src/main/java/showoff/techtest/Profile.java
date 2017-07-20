package showoff.techtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Profile extends AppCompatActivity {

    TextView userName, userId, fullName, media, follows, followedBy, recentCaptionOne, recentCaptionTwo, captionLikesOne, captionLikesTwo;
    ImageView profPicture, recentPictureOne, recentPictureTwo;
    ArrayList<RecentPost> recentPosts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        String acToken = bundle.getString("access_token");
        tieWidgets();
        getUserData(acToken);
        getRecentPosts(acToken);
    }

    public void tieWidgets() {
        userName = (TextView) findViewById(R.id.userName);
        userId = (TextView) findViewById(R.id.userId);
        fullName = (TextView) findViewById(R.id.fullName);
        media = (TextView) findViewById(R.id.media);
        follows = (TextView) findViewById(R.id.follows);
        followedBy = (TextView) findViewById(R.id.followedBy);
        profPicture = (ImageView) findViewById(R.id.profilePicture);
        recentCaptionOne = (TextView) findViewById(R.id.captionTextOne);
        captionLikesOne = (TextView) findViewById(R.id.captionLikesOne);
        recentPictureOne = (ImageView) findViewById(R.id.recentOne);
        recentCaptionTwo = (TextView) findViewById(R.id.captionTextTwo);
        captionLikesTwo = (TextView) findViewById(R.id.captionLikesTwo);
        recentPictureTwo = (ImageView) findViewById(R.id.recentTwo);
    }

    public void getUserData(final String accessToken) {
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://api.instagram.com/v1/users/self/?access_token=" + accessToken);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();

                    String response = streamToString(urlConnection.getInputStream());
                    System.out.println("object response " + response);
                    final JSONObject profileObject = (JSONObject) new JSONTokener(response).nextValue();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setUpDisplay(profileObject);
                        }
                    });
                    urlConnection.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private String streamToString(InputStream is) throws IOException {
        String str = "";
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
            } finally {
                is.close();
            }
            str = sb.toString();
        }
        return str;
    }

    private void setUpDisplay(final JSONObject profileObject) {
        try {
            userName.setText(profileObject.getJSONObject("data").getString("username"));
            userId.setText(profileObject.getJSONObject("data").getString("id"));
            fullName.setText(profileObject.getJSONObject("data").getString("full_name"));
            media.setText(profileObject.getJSONObject("data").getJSONObject("counts").getString("media"));
            follows.setText(profileObject.getJSONObject("data").getJSONObject("counts").getString("follows"));
            followedBy.setText(profileObject.getJSONObject("data").getJSONObject("counts").getString("followed_by"));
            Glide.with(this)
                    .load(profileObject.getJSONObject("data").getString("profile_picture"))
                    .into(profPicture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getRecentPosts(final String accessToken) {
        new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("we are here");
                    URL url = new URL("https://api.instagram.com/v1/users/self/media/recent/?access_token=" + accessToken);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();

                    String response = streamToString(urlConnection.getInputStream());
                    System.out.println("Recent Activity " + response);
                    final JSONObject recentActivity = (JSONObject) new JSONTokener(response).nextValue();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            retrieveRecentDataFromJSON(recentActivity);
                            displayRetrievedRecentPosts();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    private void retrieveRecentDataFromJSON(final JSONObject recentActivity) {
        try {

            JSONArray msg = (JSONArray) recentActivity.get("data");
            for (int i = 0; i < msg.length(); i++) {
                JSONObject recent = msg.getJSONObject(i);
                String thumbNailUrl = recent.getJSONObject("images").getJSONObject("thumbnail").getString("url");
                String captions = recent.getJSONObject("caption").getString("text");
                int likeCount = recent.getJSONObject("likes").getInt("count");
                RecentPost rPost = new RecentPost(thumbNailUrl, captions, likeCount);
                recentPosts.add(i, rPost);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayRetrievedRecentPosts()
    {
        if(recentPosts != null){
            Glide.with(this)
                    .load(recentPosts.get(0).getUrl())
                    .into(recentPictureOne);
            recentCaptionOne.setText(recentPosts.get(0).getCaption());
            captionLikesOne.setText(Integer.toString(recentPosts.get(0).getLikeCount()));
            Glide.with(this)
                    .load(recentPosts.get(1).getUrl())
                    .into(recentPictureTwo);
            recentCaptionTwo.setText(recentPosts.get(1).getCaption());
            captionLikesTwo.setText(Integer.toString(recentPosts.get(1).getLikeCount()));

        }else
            Toast.makeText(this, "NOTHING TO DISPLAY", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            finish();
            Intent login = new Intent(Profile.this, MainActivity.class);
            startActivity(login);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

