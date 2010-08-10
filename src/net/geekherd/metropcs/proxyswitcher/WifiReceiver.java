package net.geekherd.metropcs.proxyswitcher;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

public class WifiReceiver extends BroadcastReceiver 
{
	private Context context;
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		final String action = intent.getAction();
		this.context = context;
		
		if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
		{
			NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			Log.d(Configuration.TAG, "State: " + info.getState());
			
			if (info.getState().equals(NetworkInfo.State.CONNECTED))
			{
				Log.d(Configuration.TAG, "Succesfully connected to Wi-fi");
				
				disableProxy();
			}
			else
			{
				Log.d(Configuration.TAG, "Wi-fi connection was dropped or disconnected");
				
				enableProxy();
			}
			
		}
		else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
		{
			if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_DISABLED)
			{
				Log.d(Configuration.TAG, "Wi-fi was disabled");
				
				enableProxy();
			}
		}
		else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
		{
			if (!intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false))
			{
				Log.d(Configuration.TAG, "Wi-fi connection was dropped or disconnected");
				
				enableProxy();
			}
		}
	}
	
	private void enableProxy()
	{
		ContentResolver res = context.getContentResolver();
		String hostname = Configuration.DEFAULT_PROXY + ':' + Configuration.DEFAULT_PROXY_PORT;
		
		Settings.Secure.putString(res, Settings.Secure.HTTP_PROXY, hostname);
		context.sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
	}
	
	private void disableProxy()
	{
		ContentResolver res = context.getContentResolver();
		String hostname = "";
		
		Settings.Secure.putString(res, Settings.Secure.HTTP_PROXY, hostname);
		context.sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
	}

}
