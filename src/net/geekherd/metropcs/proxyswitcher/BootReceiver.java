package net.geekherd.metropcs.proxyswitcher;

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