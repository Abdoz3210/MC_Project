package com.lafamilia.app.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lafamilia.app.AppGraph
import com.lafamilia.app.data.remote.SendResult

/**
 * Runs in the background (even if the app is closed) once the phone has internet,
 * and sends one queued message. If it keeps failing it gives up and marks the message FAILED
 * so the user can tap it to retry.
 */
class SendMessageWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val messageId = inputData.getString(KEY_MESSAGE_ID) ?: return Result.failure()
        val sender = AppGraph.messageSender
        return when (sender.send(messageId)) {
            SendResult.DONE -> Result.success()
            SendResult.RETRY -> if (runAttemptCount >= MAX_ATTEMPTS) {
                sender.markFailed(messageId)
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        const val KEY_MESSAGE_ID = "message_id"
        private const val MAX_ATTEMPTS = 5
    }
}
