package com.example.smsforwarder

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.provider.Telephony
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import java.util.Properties
import android.app.Service
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.pm.ServiceInfo


class SMSForwarderService : android.app.Service() {
    private lateinit var prefs: SharedPreferences
    private lateinit var smsReceiver: BroadcastReceiver
    private val CHANNEL_ID = "SMSForwarderService"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("SMSForwarder", Context.MODE_PRIVATE)
        createNotificationChannel()
        setupSmsReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        return Service.START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SMS Forwarder Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Running SMS Forwarder"
                lightColor = Color.BLUE
                enableLights(true)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SMS Forwarder Active")
            .setContentText("Forwarding SMS messages to email")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun setupSmsReceiver() {
        smsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    messages.forEach { message ->
                        val sender = message.originatingAddress
                        val body = message.messageBody
                        sendEmail(sender, body)
                    }
                }
            }
        }

        registerReceiver(smsReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
    }

    private fun sendEmail(sender: String?, body: String) {
        val username = prefs.getString("senderEmail", "") ?: return
        val password = prefs.getString("senderPassword", "") ?: return
        val destinationEmail = prefs.getString("targetEmail", "") ?: return

        // Truncate message to 100 chars for subject line
        val truncatedMessage = if (body.length > 100) "${body.take(97)}..." else body

        Thread {
            try {
                val props = Properties()
                props["mail.smtp.host"] = "smtp.gmail.com"
                props["mail.smtp.socketFactory.port"] = "465"
                props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                props["mail.smtp.auth"] = "true"
                props["mail.smtp.port"] = "465"

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(username, password)
                    }
                })

                val message = MimeMessage(session)
                message.setFrom(InternetAddress(username))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinationEmail))
                message.subject = "SMS from $sender: $truncatedMessage"
                message.setText("""
                    From: $sender
                    Message:
                    $body
                    
                    --
                    Sent via SMS Forwarder
                """.trimIndent())

                Transport.send(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(smsReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}