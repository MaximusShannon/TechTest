package showoff.techtest;

/**
 * Created by Max on 12/07/2017.
 */

public class Appdata {
    public static final String CLIENT_ID = "879f440a63da4593a4a16913167a6eac";
    public static final String CLIENT_SECRET = "941cda30b6414865b90d4f8cfa77e7a4";
    public static final String CALLBACK_URL = "https://www.instagram.com";
    public static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token";
    public static String tokenURL = TOKEN_URL + "?client_id=" + Appdata.CLIENT_ID + "&client_secret=" + Appdata.CLIENT_SECRET + "&redirect_uri=" + Appdata.CALLBACK_URL + "&grant_type=authorization_code";
}
