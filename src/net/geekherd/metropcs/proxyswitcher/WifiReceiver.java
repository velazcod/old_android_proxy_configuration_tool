package net.geekherd.metropcs.proxyswitcher;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class WifiReceiver extends BroadcastReceiver 
{
	private Context context;
	private SharedPreferences preferences;
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		Boolean autoSwitch = preferences.getBoolean(Configuration.PREF_AUTO_SWITCH_ENABLED, 
				Configuration.PREF_AUTO_SWITCH_ENABLED_DEFAULT);
		
		if (!autoSwitch)
			return;
		
		final String action = intent.getAction();
		
		if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
		{
			NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			Log.d(Configuration.TAG, "State: " + info.getState());
			
			if (info.getState().equals(NetworkInfo.State.CONNECTED))
			{
				Log.d(Configuration.TAG, "Succesfully connected to Wi-fi");
				
				disableProxy();
				disableU2NL();
			}
			else
			{
				Log.d(Configuration.TAG, "Wi-fi connection was dropped or disconnected");
				
				enableProxy();
				enableU2NL();
			}
			
		}
		else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
		{
			if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_DISABLED)
			{
				Log.d(Configuration.TAG, "Wi-fi was disabled");
				
				enableProxy();
				enableU2NL();
			}
		}
		else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
		{
			if (!intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false))
			{
				Log.d(Configuration.TAG, "Wi-fi connection was dropped or disconnected");
				
				enableProxy();
				enableU2NL();
			}
		}
	}
	
	private void enableProxy()
	{
		Log.d(Configuration.TAG, "Enabling proxy");
		
		ContentResolver res = context.getContentResolver();
		
		Boolean useCustomProxy = preferences.getBoolean(Configuration.PREF_USE_CUSTOM_PROXY, false);
		String customProxy = preferences.getString(Configuration.PREF_PROXY, Configuration.PREF_PROXY_DEFAULT);
		String customProxyPort = preferences.getString(Configuration.PREF_PROXY_PORT, Configuration.PREF_PROXY_PORT_DEFAULT);
		
		String hostname;
		
		if (useCustomProxy)
			hostname = customProxy + ':' + customProxyPort;
		else
			hostname = Configuration.DEFAULT_PROXY 
						+ ':' + Configuration.DEFAULT_PROXY_PORT;
		
		
		Settings.Secure.putString(res, Settings.Secure.HTTP_PROXY, hostname);
		context.sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
	}
	
	private void disableProxy()
	{
		Log.d(Configuration.TAG, "Disabling proxy");
		
		ContentResolver res = context.getContentResolver();
		String hostname = "";
		
		Settings.Secure.putString(res, Settings.Secure.HTTP_PROXY, hostname);
		context.sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
	}
	
	private void enableU2NL()
	{
		Boolean useU2NL = preferences.getBoolean(Configuration.PREF_USE_U2NL, 
				Configuration.PREF_USE_U2NL_DEFAULT);
		
		if (!useU2NL)
			return;
		
		Log.d(Configuration.TAG, "Enabling U2NL");
	}
	
	private void disableU2NL()
	{
		Boolean useU2NL = preferences.getBoolean(Configuration.PREF_USE_U2NL, 
				Configuration.PREF_USE_U2NL_DEFAULT);
		
		if (!useU2NL)
			return;
		
		Log.d(Configuration.TAG, "Enabling U2NL");
	}

}
