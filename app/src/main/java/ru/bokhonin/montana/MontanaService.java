package ru.bokhonin.montana;

import android.app.*;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Vibrator;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MontanaService extends IntentService {

    public MontanaService() {
        super("MontanaService");
    }

    public static void setServiceAlarm(Context context, boolean isOn) {

        Intent intent = new Intent(context, MontanaService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            // В первом варианте использовалась эта функция, но тесты и последующее вдумчивое чтение документации
            // показало, что данная функция не дает точные вызовы (из-за энергосбережения)
            // alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 60000, pendingIntent);

            // Установим звуковой сигнал на срабатывание на ближайший час
            // Сделаем так, чтобы ночью сигнал не срабатывал (с 21 до 9)

            long timeStart = getNextTime();
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeStart, pendingIntent);

//            Calendar cal = Calendar.getInstance();
//            cal.setTimeInMillis(timeStart);
//
//            int h = cal.get(Calendar.HOUR_OF_DAY);
//            int m = cal.get(Calendar.MINUTE);
//            int s = cal.get(Calendar.SECOND);
//
//            String ss = String.format("%d:%02d:%02d", h, m, s);

        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context) {

        Intent intent = new Intent(context, MontanaService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);

        return pendingIntent != null;
    }

    private static long getNextTime() {

        Calendar rightNowPlusHour = Calendar.getInstance();
        rightNowPlusHour.set(Calendar.MINUTE, 0);
        rightNowPlusHour.set(Calendar.SECOND, 0);
        rightNowPlusHour.set(Calendar.MILLISECOND, 0);
        rightNowPlusHour.add(Calendar.HOUR, 1);

        int currentHour = rightNowPlusHour.get(Calendar.HOUR);
        int currentAmPm = rightNowPlusHour.get(Calendar.AM_PM);
        long currentMillis = rightNowPlusHour.getTimeInMillis();
        long rightNowMs = Calendar.getInstance().getTimeInMillis();
        long durationSeconds = currentMillis - rightNowMs;
        int hourPlus = 0;

        if (currentAmPm == 0 && currentHour <= 9) {
            hourPlus = 9 - currentHour;
        } else if (currentAmPm == 1 && currentHour >= 9) {
            hourPlus = 11 - currentHour + 10;
        }

        long timeStart = System.currentTimeMillis() + durationSeconds + hourPlus * 60 * 60 * 1000;

        return timeStart;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            SharedPreferences sharedPreferences = getSharedPreferences("montana_preferences", 0);
            boolean vibration = sharedPreferences.getBoolean("mVibrationSwitch", true);
            boolean sound = sharedPreferences.getBoolean("mSoundSwitch", false);

            String contentText;

            if (vibration) {
                contentText = "Montana. Vbr: On.";
            } else {
                contentText = "Montana. Vbr: Off.";
            }

            if (sound) {
                contentText += " Snd: On.";
            } else {
                contentText += " Snd: Off.";
            }

            long timeStart = getNextTime();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timeStart);

            SimpleDateFormat dateFormat = new SimpleDateFormat();
            String dateString = dateFormat.format(cal.getTime());
            contentText += " Next: " + dateString;

            PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

            Notification notification = new Notification.Builder(this)
                    .setTicker("Test Mont")
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle("TEST Montana")
                    .setContentText(contentText)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(0, notification);


            if (sound) {
                MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.signal);
                mPlayer.start();
            }

            if (vibration) {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                long[] pattern = new long[6];
                pattern[0] = 0;
                pattern[1] = 200;
                pattern[2] = 100;
                pattern[5] = 600;

                vibrator.vibrate(pattern, -1);
            }

            setServiceAlarm(this, true);
         }
    }
}
