package net.sorablue.shogo82148.yuire;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class OAuthActivity extends Activity {
	public final static String EXTRA_CALLBACK = "callback";
	public final static String EXTRA_CONSUMER_KEY = "consumer_key";
	public final static String EXTRA_CONSUMER_SECRET = "consumer_secret";
	public final static String EXTRA_ACCESS_TOKEN = "access_token";
	public final static String EXTRA_ACCESS_TOKEN_SECRET = "access_token_secret";
	public final static String EXTRA_PACKAGE_NAME = "package";
	public final static String EXTRA_CLASS_NAME = "class_name";

	private final Handler mHandler = new Handler(); 
	private RequestToken mRequestToken;
    final AsyncTwitterFactory factory = new AsyncTwitterFactory();
    final AsyncTwitter twitter = factory.getInstance();
    
	private final TwitterListener listener = new TwitterAdapter() {
		@Override
		public void gotOAuthRequestToken(RequestToken token) {
			mRequestToken = token;
			final Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(mRequestToken.getAuthorizationURL()));
			startActivity(intent);
		}
		
		@Override
		public void gotOAuthAccessToken(AccessToken token) {
			final Intent ointent = getIntent();
			final String package_name = ointent.getStringExtra(EXTRA_PACKAGE_NAME);
			final String class_name = ointent.getStringExtra(EXTRA_CLASS_NAME);
			final String stoken = token.getToken();
			final String token_secret = token.getTokenSecret();
			mHandler.post(new Runnable() {
				public void run() {
					final Intent intent = new Intent();
					intent.setClassName(package_name, class_name);
					intent.putExtra(EXTRA_ACCESS_TOKEN, stoken);
					intent.putExtra(EXTRA_ACCESS_TOKEN_SECRET, token_secret);
					startActivity(intent);
				}
			});
			finish();
		}
		
		@Override
		public void onException(TwitterException e, TwitterMethod method) {
			Log.e("Twitter", e.toString());
			finish();
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        
        // Request Request Token
        final Intent intent = getIntent();
        final String consumer_key = intent.getStringExtra(EXTRA_CONSUMER_KEY);
        final String consumer_secret = intent.getStringExtra(EXTRA_CONSUMER_SECRET);
        final String callback = intent.getStringExtra(EXTRA_CALLBACK);
        twitter.addListener(listener);
        twitter.setOAuthConsumer(consumer_key, consumer_secret);
        twitter.getOAuthRequestTokenAsync(callback);
    }
    
    @Override
    public void onNewIntent(Intent intent) {
    	final Uri uri = intent.getData();
    	if(uri == null) return ;
    	final String verifier = uri.getQueryParameter("oauth_verifier");
    	twitter.getOAuthAccessTokenAsync(mRequestToken, verifier);
    }
}
