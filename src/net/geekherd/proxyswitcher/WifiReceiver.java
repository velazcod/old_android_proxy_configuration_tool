/*
 * Copyright (C) 2010 Daniel Velazco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.geekherd.proxyswitcher;

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
	
	/*
	 * Receive broadcast to let us know wifi/network connection has changed
	 */
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

	/*
	 * Send broadcast intent to activate proxy & u2nl
	 */
	private void toggleActivate(Context context) 
	{	
		Intent sIntent = new Intent(context, Toggler.class);
		sIntent.setAction(Configuration.ACTION_ACTIVATE_PROXY);
		context.sendBroadcast(sIntent);
	}
	
	/*
	 * Send broadcast intent to deactivate proxy & u2nl
	 */
	private void toggleDeactivate(Context context) 
	{
		Intent sIntent = new Intent(context, Toggler.class);
		sIntent.setAction(Configuration.ACTION_DEACTIVATE_PROXY);
		context.sendBroadcast(sIntent);
	}	

}
