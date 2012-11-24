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
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class OAuthActivity extends Activity implements OnClickListener {
	public final static String EXTRA_CONSUMER_KEY = "consumer_key";
	public final static String EXTRA_CONSUMER_SECRET = "consumer_secret";
	public final static String EXTRA_ACCESS_TOKEN = "access_token";
	public final static String EXTRA_ACCESS_TOKEN_SECRET = "access_token_secret";

	private final Handler mHandler = new Handler(); 
	private RequestToken mRequestToken;
    final AsyncTwitterFactory factory = new AsyncTwitterFactory();
    final AsyncTwitter twitter = factory.getInstance();
	
	private final TwitterListener listener = new TwitterAdapter() {
		@Override
		public void gotOAuthRequestToken(RequestToken token) {
			mHandler.post(new Runnable() {
				public void run() {
					final View startLogin = findViewById(R.id.button_start_login);
					final View progress = findViewById(R.id.progressBar1);
					startLogin.setEnabled(true);
					progress.setVisibility(View.INVISIBLE);
				}
			});
			mRequestToken = token;
		}
		
		@Override
		public void gotOAuthAccessToken(AccessToken token) {
			final Intent intent = new Intent();
			intent.putExtra(EXTRA_ACCESS_TOKEN, token.getToken());
			intent.putExtra(EXTRA_ACCESS_TOKEN_SECRET, token.getTokenSecret());
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
		
		@Override
		public void onException(TwitterException e, TwitterMethod method) {
			setResult(Activity.RESULT_CANCELED);
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
        twitter.addListener(listener);
        twitter.setOAuthConsumer(consumer_key, consumer_secret);
        twitter.getOAuthRequestTokenAsync();
        
        // AddEventListener
        final View start_login = findViewById(R.id.button_start_login);
        start_login.setOnClickListener(this);
        final View login = findViewById(R.id.button_login);
        login.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_oauth, menu);
        return true;
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_start_login:
			{
				final View startLogin = findViewById(R.id.button_start_login);
				startLogin.setEnabled(false);
				final Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(mRequestToken.getAuthorizationURL()));
				startActivity(intent);
			}
			break;
		case R.id.button_login:
			{
				final EditText editPin = (EditText)findViewById(R.id.edit_pin_code);
				final View progress = findViewById(R.id.progressBar1);
				final View login = findViewById(R.id.button_login);
				login.setEnabled(false);
				progress.setVisibility(View.VISIBLE);
	
				final String pin = editPin.getText().toString();
				twitter.getOAuthAccessTokenAsync(mRequestToken, pin);
			}
			break;
		}
	}
}
