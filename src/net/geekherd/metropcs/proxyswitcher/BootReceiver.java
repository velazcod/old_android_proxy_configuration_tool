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
		NetworkInfo info = (NetworkInfo)intent.
			getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		
		if (info != null)
		{
			if (info.getState().equals(NetworkInfo.State.CONNECTED))
			{
				Intent sIntent = new Intent(context, Toggler.class);
				sIntent.setAction(Configuration.ACTION_DEACTIVATE_PROXY);
				context.sendBroadcast(sIntent);
			}
			else
			{
				Intent sIntent = new Intent(context, Toggler.class);
				sIntent.setAction(Configuration.ACTION_ACTIVATE_PROXY);
				context.sendBroadcast(sIntent);
			}
		}
		else
		{
			Intent sIntent = new Intent(context, Toggler.class);
			sIntent.setAction(Configuration.ACTION_ACTIVATE_PROXY);
			context.sendBroadcast(sIntent);
		}
	}
}
