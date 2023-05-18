package org.thoughtcrime.securesms.notifications

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import org.session.libsession.messaging.jobs.BatchMessageReceiveJob
import org.session.libsession.messaging.jobs.JobQueue
import org.session.libsession.messaging.jobs.MessageReceiveParameters
import org.session.libsession.messaging.utilities.MessageWrapper
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsignal.utilities.Base64
import org.session.libsignal.utilities.Log
import javax.inject.Inject

@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {

    @Inject lateinit var pushManager: FirebasePushManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("Loki", "New FCM token: $token.")
        TextSecurePreferences.getLocalNumber(this) ?: return
        pushManager.register(true)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("Loki", "Received a push notification.")
        val data: ByteArray? = if (message.data.containsKey("spns")) {
            // this is a v2 push notification
            try {
                pushManager.decrypt(Base64.decode(message.data["enc_payload"]))
            } catch(e: Exception) {
                Log.e("Loki", "Invalid push notification: ${e.message}")
                return
            }
        } else {
            // old v1 push notification; we still need this for receiving legacy closed group notifications
            val base64EncodedData = message.data?.get("ENCRYPTED_DATA")
            base64EncodedData?.let { Base64.decode(it) }
        }
        if (data != null) {
            try {
                val envelopeAsData = MessageWrapper.unwrap(data).toByteArray()
                val job = BatchMessageReceiveJob(listOf(MessageReceiveParameters(envelopeAsData)), null)
                JobQueue.shared.add(job)
            } catch (e: Exception) {
                Log.d("Loki", "Failed to unwrap data for message due to error: $e.")
            }
        } else {
            Log.d("Loki", "Failed to decode data for message.")
            val builder = NotificationCompat.Builder(this, NotificationChannels.OTHER)
                .setSmallIcon(network.loki.messenger.R.drawable.ic_notification)
                .setColor(this.getResources().getColor(network.loki.messenger.R.color.textsecure_primary))
                .setContentTitle("Session")
                .setContentText("You've got a new message.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
            with(NotificationManagerCompat.from(this)) {
                notify(11111, builder.build())
            }
        }
    }

    override fun onDeletedMessages() {
        Log.d("Loki", "Called onDeletedMessages.")
        super.onDeletedMessages()
        val token = TextSecurePreferences.getFCMToken(this)!!
        val userPublicKey = TextSecurePreferences.getLocalNumber(this) ?: return
        PushNotificationManager.register(token, userPublicKey, this, true)
    }
}