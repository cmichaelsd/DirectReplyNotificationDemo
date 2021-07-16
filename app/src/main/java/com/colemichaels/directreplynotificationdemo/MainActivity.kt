package com.colemichaels.directreplynotificationdemo

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.colemichaels.directreplynotificationdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),
    View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationManager: NotificationManager
    private lateinit var icon: Icon


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        icon = Icon.createWithResource(this@MainActivity, android.R.drawable.ic_dialog_info)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotification(
            CHANNEL_ID,
            "DirectReply News",
            "Example News Channel"
        )

        binding.button.setOnClickListener(this)

        handleIntent()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.button.id -> sendNotification()
        }
    }

    private fun createNotification(id: String, name: String, contentDescription: String) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(id, name, importance).apply {
            description = contentDescription
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
            vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun sendNotification() {
        val replyLabel = "Enter your reply here"
        // RemoteInput class allows a request for user input to be included from the resulting intent.
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(replyLabel)
            .build()
        // PendingIntent allows other applications or services the ability to launch that intent from outside the original application.
        val resultIntent = Intent(this, MainActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val replyAction = Notification.Action.Builder(
            icon,
            "Reply",
            resultPendingIntent)
            .addRemoteInput(remoteInput)
            .build()

        val newMessageNotification = Notification.Builder(this, CHANNEL_ID)
            .setColor(ContextCompat.getColor(this, R.color.design_default_color_primary))
            .setSmallIcon(icon)
            .setContentTitle("My Notification")
            .setContentText("this is a test message")
            .addAction(replyAction)
            .build()

        notificationManager.notify(NOTIFICATION_ID, newMessageNotification)
    }

    private fun handleIntent() {
        val intent = intent // Kotlin getter syntax which supplies intent which launched activity.
        val remoteInput = RemoteInput.getResultsFromIntent(intent) ?: return // Pass in intent value so its clear where intent is coming from.

        remoteInput. let {
            val inputString = it.getCharSequence(KEY_TEXT_REPLY).toString()
            binding.textView.text = inputString
        }

        // Reply to the received notification, appears to function without this code but is best practice to handle this situation.
        // It informs the users that their response was handled safely.
        val repliedNotification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentText("Reply received")
            .build()

        notificationManager.notify(NOTIFICATION_ID, repliedNotification)
    }

    companion object {
        const val CHANNEL_ID = "com.colemichaels.directreplynotificationdemo.news"
        const val NOTIFICATION_ID = 101
        const val KEY_TEXT_REPLY = "key_text_reply"
    }
}