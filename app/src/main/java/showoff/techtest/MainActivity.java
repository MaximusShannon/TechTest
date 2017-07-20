package showoff.techtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    private static final String AUTHURL = "https://api.instagram.com/oauth/authorize/";
    public static String authURL = AUTHURL + "?client_id=" + Appdata.CLIENT_ID + "&redirect_uri=" + Appdata.CALLBACK_URL + "&response_type=code";

    Button loginButton;
    WebView mWebView;
    String request_token;
    static String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWidgets();
        setClickListeners();
    }

    public void getWidgets() {
        loginButton = (Button) findViewById(R.id.Login);
        mWebView = (WebView) findViewById(R.id.webview);
    }

    public void setClickListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mWebView.setVerticalScrollBarEnabled(false);
                mWebView.setHorizontalScrollBarEnabled(false);
                mWebView.setWebViewClient(new WebViewClient() {
                    public void onPageFinished(WebView view, String url) {
                        if (url.contains("code"))
                            getUrlToSplit();
                        view.clearCache(true);
                    }
                });
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.loadUrl(authURL);
                System.out.println("authURL: " + authURL);
                loginButton.setVisibility(View.GONE);
            }
        });
    }

    public void getUrlToSplit() {
        String webUrl = mWebView.getUrl();
        if (webUrl.startsWith(Appdata.CALLBACK_URL)) {
            String split[] = webUrl.split("=");
            request_token = split[1]; // request token
            System.out.println("req tok " + request_token);
            getAccessToken(request_token);
        }
    }

    public void getAccessToken(final String request_token) {
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://api.instagram.com/oauth/access_token");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write("client_id=" + Appdata.CLIENT_ID +
                            "&client_secret=" + Appdata.CLIENT_SECRET +
                            "&grant_type=authorization_code" +
                            "&redirect_uri=" + Appdata.CALLBACK_URL +
                            "&code=" + request_token);
                    writer.flush();

                    String response = streamToString(urlConnection.getInputStream());
                    System.out.println("Response is: " + response);
                    JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                    accessToken = jsonObject.getString("access_token");
                    System.out.println("ACCESS TOKEN: " + accessToken);

                    if (accessToken != null) {
                        Intent openProfile = new Intent(MainActivity.this, Profile.class);
                        openProfile.putExtra("access_token", accessToken);
                        startActivity(openProfile);
                    }
                } catch (Exception e) {
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

    public boolean hasAccessToken() {
        return (accessToken == null) ? false : true;
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

            mWebView.clearCache(true);
            mWebView.clearHistory();
            mWebView.clearFormData();
            mWebView.loadUrl("https://instagram.com/accounts/logout/");
            mWebView.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            accessToken = null;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
