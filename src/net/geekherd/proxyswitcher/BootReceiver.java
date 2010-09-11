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
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class BootReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		/*
		 * Startup U2NL binary no matter what
		 */
		Intent mU2NLIntent = new Intent(context, Toggler.class);
		mU2NLIntent.setAction(Configuration.ACTION_ACTIVATE_U2NL);
		context.sendBroadcast(mU2NLIntent);
		
		
		/*
		 * Activate proxy only if we are not connected to wifi, otherwise deactivate
		 */
		NetworkInfo info = (NetworkInfo)intent.
			getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		
		if (info != null)
		{
			if (info.getState().equals(NetworkInfo.State.CONNECTED))
			{
				Intent mProxyIntent = new Intent(context, Toggler.class);
				mProxyIntent.setAction(Configuration.ACTION_DEACTIVATE_PROXY);
				context.sendBroadcast(mProxyIntent);
			}
			else
			{
				Intent mProxyIntent = new Intent(context, Toggler.class);
				mProxyIntent.setAction(Configuration.ACTION_ACTIVATE_PROXY);
				context.sendBroadcast(mProxyIntent);
			}
		}
		else
		{
			Intent mProxyIntent = new Intent(context, Toggler.class);
			mProxyIntent.setAction(Configuration.ACTION_ACTIVATE_PROXY);
			context.sendBroadcast(mProxyIntent);
		}
	}
}
