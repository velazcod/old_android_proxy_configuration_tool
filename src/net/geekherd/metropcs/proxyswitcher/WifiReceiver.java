package net.geekherd.metropcs.proxyswitcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class WifiReceiver extends BroadcastReceiver 
{	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		Boolean autoSwitch = preferences.getBoolean(Configuration.PREF_AUTO_SWITCH_ENABLED, 
				Configuration.PREF_AUTO_SWITCH_ENABLED_DEFAULT);
		
		if (autoSwitch)
		{
			final String action = intent.getAction();
			
			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
			{
				NetworkInfo info = (NetworkInfo)intent.
						getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				
				Log.d(Configuration.TAG, "State: " + info.getState());
				
				if (info.getState().equals(NetworkInfo.State.CONNECTED))
				{
					Log.d(Configuration.TAG, 
							"Succesfully connected to Wi-fi");
					
					toggleDeactivate(context);
				}
				else
				{
					Log.d(Configuration.TAG, 
							"Wi-fi connection was dropped or disconnected");
					
					toggleActivate(context);
				}
				
			}
			else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
			{
				if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 
						WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_DISABLED)
				{
					Log.d(Configuration.TAG, 
							"Wi-fi was disabled");
					
					toggleActivate(context);
				}
			}
			else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
			{
				if (!intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false))
				{
					Log.d(Configuration.TAG, 
							"Wi-fi connection was dropped or disconnected");
					
					toggleActivate(context);
				}
			}
		}
	}

	private void toggleActivate(Context context) 
	{	
		Intent sIntent = new Intent(context, Toggler.class);
		sIntent.setAction(Configuration.ACTION_ACTIVATE_PROXY);
		context.sendBroadcast(sIntent);
	}
	
	private void toggleDeactivate(Context context) 
	{
		Intent sIntent = new Intent(context, Toggler.class);
		sIntent.setAction(Configuration.ACTION_DEACTIVATE_PROXY);
		context.sendBroadcast(sIntent);
	}	

}
