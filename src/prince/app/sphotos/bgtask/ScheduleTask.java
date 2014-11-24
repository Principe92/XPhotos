package prince.app.sphotos.bgtask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ScheduleTask extends RecieverX {
	final public static int REFRESH_DATA = 100;
	final public static String INTENT = "future_task";
	
	@Override
	public void onReceive(Context context, Intent intent) {
	/*	PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ScheduleTask.class.getSimpleName()); */
		
		// acquire lock
	//	wl.acquire();

		Bundle xtras = intent.getExtras();
		int key = (xtras != null) ? xtras.getInt(INTENT) : -1;
		
		switch(key){
		case REFRESH_DATA:
			Toast.makeText(context, "refresh data", Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
		
	//	wl.release();
	} 
	
	public void setSchedule(Context ct, int key, int requestCode, int flags, int mMillis){
		AlarmManager ar = (AlarmManager) ct.getSystemService(Context.ALARM_SERVICE);
		Intent it = new Intent(ct, ScheduleTask.class);
		it.putExtra(INTENT, key);
		PendingIntent pt = PendingIntent.getBroadcast(ct, requestCode, it, flags);
		ar.setRepeating(AlarmManager.RTC_WAKEUP, 
				System.currentTimeMillis(), mMillis, pt);
	}
	
	public void cancelSchedule(Context ct, int requestCode, int flags){
		Intent it = new Intent(ct, ScheduleTask.class);
		PendingIntent pt = PendingIntent.getBroadcast(ct, requestCode, it, flags);
		AlarmManager ar = (AlarmManager) ct.getSystemService(Context.ALARM_SERVICE);
		ar.cancel(pt);
	}
	
	public void oneTimeSchedule(Context ct, int key, int requestCode, int flags, int mMillis){
		AlarmManager ar = (AlarmManager) ct.getSystemService(Context.ALARM_SERVICE);
		Intent it = new Intent(ct, ScheduleTask.class);
		it.putExtra(INTENT, key);
		PendingIntent pt = PendingIntent.getBroadcast(ct, requestCode, it, flags);
		ar.setInexactRepeating(AlarmManager.RTC_WAKEUP, 
				System.currentTimeMillis(), mMillis, pt);
	}

}
