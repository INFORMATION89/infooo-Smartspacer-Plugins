package com.kieronquinn.app.smartspacer.plugin.uber.repositories

import android.content.Context
import android.util.Log
import com.kieronquinn.app.smartspacer.plugin.uber.drawing.drawNotificationData
import com.kieronquinn.app.smartspacer.plugin.uber.model.NotificationData
import com.kieronquinn.app.smartspacer.plugin.uber.model.TargetData
import com.kieronquinn.app.smartspacer.plugin.uber.targets.UberTarget
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerNotificationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface NotificationRepository {

    fun setNotifications(notifications: List<NotificationData>)
    fun dismissNotification(id: String): Boolean
    fun getTargetData(): List<TargetData>
    fun getTargetDataFlow(): StateFlow<List<TargetData>>

}

class NotificationRepositoryImpl(private val context: Context): NotificationRepository {

    private val scope = MainScope()
    private val targetData = MutableStateFlow(emptyList<TargetData>())
    private val updateLock = Mutex()

    override fun setNotifications(notifications: List<NotificationData>) {
        scope.launch(Dispatchers.IO) {
            updateLock.withLock {
                updateTargetsLocked(notifications)
            }
        }
    }

    override fun dismissNotification(id: String): Boolean {
        val target = targetData.value.firstOrNull { it.notification.id.toString() == id }
            ?: return false
        SmartspacerNotificationProvider.dismissNotification(context, target.notification)
        return true
    }

    private fun updateTargetsLocked(notifications: List<NotificationData>) {
        targetData.value = notifications.map {
            it.toTargetData()
        }
        SmartspacerTargetProvider.notifyChange(context, UberTarget::class.java)
    }

    private fun NotificationData.toTargetData(): TargetData {
        val image = try {
            context.drawNotificationData(this)
        }catch (e: Exception) {
            Log.w("UberPlugin", "Failed to render Uber target image", e)
            null
        }
        return TargetData(notification, icon, title, subtitle, image)
    }

    override fun getTargetData(): List<TargetData> = targetData.value

    override fun getTargetDataFlow(): StateFlow<List<TargetData>> = targetData.asStateFlow()

}