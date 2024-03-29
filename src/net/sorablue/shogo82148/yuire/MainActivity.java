package net.sorablue.shogo82148.yuire;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.auth.AccessToken;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private static final String PACKAGE_NAME = "net.sorablue.shogo82148.yuire";
	private static final String CLASS_NAME = "MainActivity";
	private final static String KEY_ACCESS_TOKEN = "access_token";
	private final static String KEY_ACCESS_TOKEN_SECRET = "access_token_secret";
	private final static String KEY_SCREEN_NAME = "screen_name";
	private final static String CALLBACK_URL = "yuirecallback://callback/";
	private static final String CONSUMER_KEY = "qfPYZnAz4lOrz0VOdo16Pg";
	private static final String CONSUMER_SECRET = "StNpMyza8VG6kMu7xFDwveAk4Kn8hplBq3bZPV4sY";
	
	private final Handler mHandler = new Handler();
	private final Activity context = this;
    final AsyncTwitterFactory factory = new AsyncTwitterFactory();
    final AsyncTwitter twitter = factory.getInstance();
    private boolean logged_in;
    
	private final TwitterListener listener = new TwitterAdapter() {
		@Override
		public void updatedStatus(Status status) {
			mHandler.post(new Runnable() {
				public void run() {
					Toast.makeText(context, R.string.message_tweeted, Toast.LENGTH_LONG).show();
				}
			});
			Log.d("Twitter", status.getText());
		}
		
		@Override
		public void onException(TwitterException e, TwitterMethod method) {
			mHandler.post(new Runnable() {
				public void run() {
					Toast.makeText(context, R.string.message_fail, Toast.LENGTH_LONG).show();
				}
			});
			Log.d("Twitter", e.toString());
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        logged_in = false;
        twitter.addListener(listener);
        twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        login();
        
        final View yu = findViewById(R.id.button_hot_water);
        yu.setOnClickListener(this);
    }
    
    @Override
    public void onNewIntent(Intent intent) {
    	final int status = intent.getIntExtra(OAuthActivity.EXTRA_STATUS, OAuthActivity.STATUS_NONE);
        if(status == OAuthActivity.STATUS_OK) {
        	final String token = intent.getStringExtra(OAuthActivity.EXTRA_ACCESS_TOKEN);
			final String token_secret = intent.getStringExtra(OAuthActivity.EXTRA_ACCESS_TOKEN_SECRET);
			final String screen_name = intent.getStringExtra(OAuthActivity.EXTRA_SCREEN_NAME);
			if(token != null && token_secret != null) {
				Log.d("Twitter", "gotAccessToken from OAuthAcrivity");
				final AccessToken access_token = new AccessToken(token, token_secret);
				saveAccessToken(access_token, screen_name);
	        	twitter.setOAuthAccessToken(access_token);
				Toast.makeText(this, R.string.message_success_login, Toast.LENGTH_LONG).show();
				final TextView text_screen_name = (TextView)findViewById(R.id.textScreenName);
				text_screen_name.setText(getResources().getString(R.string.text_screen_name, screen_name));
				logged_in = true;
			}
        } else if(status == OAuthActivity.STATUS_ERROR) {
        	Toast.makeText(this, R.string.message_fail_login, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.menu_login:
    		start_login();
    		break;
    	case R.id.menu_logout:
    		saveAccessToken(null, null);
			final TextView text_screen_name = (TextView)findViewById(R.id.textScreenName);
			text_screen_name.setText(R.string.text_not_login);
			logged_in = false;
    		break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    private void saveAccessToken(AccessToken access_token, String screen_name) {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final Editor e = pref.edit();
		if(access_token != null) {
	    	final String token = access_token.getToken();
	    	final String token_secret = access_token.getTokenSecret();
			e.putString(KEY_ACCESS_TOKEN, token);
			e.putString(KEY_ACCESS_TOKEN_SECRET, token_secret);
			e.putString(KEY_SCREEN_NAME, screen_name);
		} else {
			e.putString(KEY_ACCESS_TOKEN, "");
			e.putString(KEY_ACCESS_TOKEN_SECRET, "");
			e.putString(KEY_SCREEN_NAME, "");
			twitter.setOAuthAccessToken(null);
		}
		e.commit();
    }
    
    private void login() {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final String token = pref.getString(KEY_ACCESS_TOKEN, "");
		final String token_secret = pref.getString(KEY_ACCESS_TOKEN_SECRET, "");
		final String screen_name = pref.getString(KEY_SCREEN_NAME, "");
		if(!token.equals("") && !token_secret.equals("")) {
			twitter.setOAuthAccessToken(new AccessToken(token, token_secret));
			final TextView text_screen_name = (TextView)findViewById(R.id.textScreenName);
			text_screen_name.setText(getResources().getString(R.string.text_screen_name, screen_name));
			logged_in = true;
		}
    }
    
    private void start_login() {
		// Start OAuthActivity to login
		Intent intent = new Intent(this, OAuthActivity.class);
		intent.putExtra(OAuthActivity.EXTRA_CONSUMER_KEY, CONSUMER_KEY);
		intent.putExtra(OAuthActivity.EXTRA_CONSUMER_SECRET, CONSUMER_SECRET);
		intent.putExtra(OAuthActivity.EXTRA_CALLBACK, CALLBACK_URL);
		intent.putExtra(OAuthActivity.EXTRA_PACKAGE_NAME, PACKAGE_NAME);
		intent.putExtra(OAuthActivity.EXTRA_CLASS_NAME, PACKAGE_NAME + "." + CLASS_NAME);
		startActivity(intent);
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_hot_water:
			if(logged_in){
				twitter.updateStatus("@JO_RI_bot �������ꂽ");
			} else {
				final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setTitle(R.string.app_name);
				alertDialogBuilder.setMessage(R.string.message_must_login);
				alertDialogBuilder.setPositiveButton(R.string.menu_login, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						start_login();
					}
				});
		        alertDialogBuilder.setCancelable(true);
		        AlertDialog alertDialog = alertDialogBuilder.create();
		        alertDialog.show();
			}
			break;
		}
	}
}
