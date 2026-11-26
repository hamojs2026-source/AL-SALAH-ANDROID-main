package com.mas.alsalah.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mas.alsalah.MainActivity
import com.mas.alsalah.R
import com.mas.alsalah.core.PrayerCalculator
import com.mas.alsalah.core.PrayerMethod
import com.mas.alsalah.data.SavedSettings
import com.mas.alsalah.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

private const val EXTRA_PRAYER = "prayer_name"
private const val EXTRA_PRE_ALERT = "is_pre_alert"
private const val EXTRA_REPEAT = "is_repeat"
private const val NOTIFICATION_ID = 5102

object PrayerNotificationScheduler {
    private const val CHANNEL_PREFIX = "prayer_alert_v2_"
    private const val REQUEST_CODE = 5102

    private data class ScheduledEvent(
        val prayer: String,
        val triggerAt: ZonedDateTime,
        val isPreAlert: Boolean,
        val isRepeat: Boolean = false
    )

    fun createChannel(context: Context) {
        createChannel(context, "default", true)
    }

    private fun createChannel(context: Context, channelId: String, vibration: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val id = CHANNEL_PREFIX + channelId
        if (manager.getNotificationChannel(id) != null) return
        val channel = NotificationChannel(id, "تنبيهات الصلاة", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "إشعارات صامتة بمواعيد الصلاة دون تشغيل أي صوت"
            setSound(null, null)
            enableVibration(vibration)
            if (vibration) vibrationPattern = longArrayOf(0, 180, 100, 180)
        }
        manager.createNotificationChannel(channel)
    }

    fun scheduleNext(context: Context) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val settings = SettingsRepository(context).settings.first()
            val latitude = settings.latitude
            val longitude = settings.longitude
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val oldPending = PendingIntent.getBroadcast(
                context, REQUEST_CODE, Intent(context, PrayerAlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (oldPending != null) alarm.cancel(oldPending)
            if (latitude == null || longitude == null) return@launch

            val zone = ZoneId.systemDefault()
            val now = ZonedDateTime.now(zone)
            val method = PrayerMethod.values().firstOrNull { it.id == settings.calculationMethod } ?: PrayerMethod.EGYPT
            val candidates = mutableListOf<ScheduledEvent>()

            for (dayOffset in 0..2) {
                val date = LocalDate.now(zone).plusDays(dayOffset.toLong())
                val times = runCatching {
                    PrayerCalculator.calculate(date, latitude, longitude, zone, method.calculation())
                }.getOrNull() ?: continue
                listOf(
                    "الفجر" to times.fajr, "الظهر" to times.dhuhr, "العصر" to times.asr,
                    "المغرب" to times.maghrib, "العشاء" to times.isha
                ).forEach { (name, localTime) ->
                    val alert = settings.alertFor(name)
                    if (!alert.enabled) return@forEach
                    val prayerAt = date.atTime(localTime).atZone(zone).plusMinutes(settings.offsetFor(name).toLong())
                    if (alert.preAlertMinutes > 0) {
                        val preAt = prayerAt.minusMinutes(alert.preAlertMinutes.toLong())
                        if (preAt.isAfter(now)) candidates += ScheduledEvent(name, preAt, true)
                    }
                    if (alert.atPrayerTime && prayerAt.isAfter(now)) candidates += ScheduledEvent(name, prayerAt, false)
                    val repeatAt = prayerAt.plusMinutes(5)
                    if (alert.atPrayerTime && alert.repeat && repeatAt.isAfter(now)) candidates += ScheduledEvent(name, repeatAt, false, true)
                }
            }

            val next = candidates.minByOrNull { it.triggerAt.toInstant() } ?: return@launch
            val pending = PendingIntent.getBroadcast(
                context, REQUEST_CODE,
                Intent(context, PrayerAlarmReceiver::class.java)
                    .putExtra(EXTRA_PRAYER, next.prayer)
                    .putExtra(EXTRA_PRE_ALERT, next.isPreAlert)
                    .putExtra(EXTRA_REPEAT, next.isRepeat),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerMillis = next.triggerAt.toInstant().toEpochMilli()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarm.canScheduleExactAlarms()) {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
            } else alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
        }
    }

    internal fun ensureChannel(context: Context, settings: SavedSettings, prayer: String): String {
        val vibration = settings.alertFor(prayer).vibration
        val id = if (vibration) "vibrate" else "silent"
        createChannel(context, id, vibration)
        return CHANNEL_PREFIX + id
    }
}

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val prayer = intent.getStringExtra(EXTRA_PRAYER) ?: "الصلاة"
                val isPreAlert = intent.getBooleanExtra(EXTRA_PRE_ALERT, false)
                val isRepeat = intent.getBooleanExtra(EXTRA_REPEAT, false)
                val settings = SettingsRepository(context).settings.first()
                val alert = settings.alertFor(prayer)
                if (!alert.enabled) {
                    PrayerNotificationScheduler.scheduleNext(context)
                    return@launch
                }

                val channelId = PrayerNotificationScheduler.ensureChannel(context, settings, prayer)
                val openApp = PendingIntent.getActivity(
                    context, 0, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val title = when {
                    isRepeat -> "تذكير: صلاة $prayer"
                    isPreAlert -> "اقترب موعد صلاة $prayer"
                    else -> "حان وقت صلاة $prayer"
                }
                val message = when {
                    isRepeat -> "مرّت خمس دقائق على موعد الصلاة."
                    isPreAlert -> "باقي ${alert.preAlertMinutes} دقيقة على الصلاة."
                    else -> "حان الآن موعد صلاة $prayer."
                }
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_stat_prayer)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(openApp)
                    .setSound(null)
                    .setDefaults(0)
                    .setVibrate(if (alert.vibration) longArrayOf(0, 180, 100, 180) else null)
                    .build()
                runCatching {
                    val id = NOTIFICATION_ID + (prayer.hashCode() and 0x3FFF) + if (isPreAlert) 0x4000 else 0
                    NotificationManagerCompat.from(context).notify(id, notification)
                }
                PrayerNotificationScheduler.scheduleNext(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in setOf(
                Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_DATE_CHANGED, Intent.ACTION_MY_PACKAGE_REPLACED
            )
        ) PrayerNotificationScheduler.scheduleNext(context)
    }
}
