package net.geekherd.metropcs.proxyswitcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		Intent sIntent = new Intent(context, Toggler.class);
		sIntent.setAction(Configuration.ACTION_ACTIVATE_PROXY);
		context.sendBroadcast(sIntent);
	}
}
