package com.droidbubble.app.tasks

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class TasksRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("droidtools_tasks_prefs", Context.MODE_PRIVATE)

    private val _shortcuts = MutableStateFlow<List<TaskShortcut>>(emptyList())
    val shortcuts: StateFlow<List<TaskShortcut>> = _shortcuts.asStateFlow()

    init {
        loadShortcuts()
    }

    private fun loadShortcuts() {
        val json = prefs.getString(KEY_SHORTCUTS, null)
        if (json.isNullOrBlank()) {
            _shortcuts.value = emptyList()
        } else {
            _shortcuts.value = deserialize(json)
        }
    }

    fun saveShortcut(shortcut: TaskShortcut) {
        val current = _shortcuts.value.toMutableList()
        val index = current.indexOfFirst { it.id == shortcut.id }
        if (index >= 0) {
            current[index] = shortcut
        } else {
            current.add(shortcut)
        }
        persist(current)
    }

    fun deleteShortcut(id: String) {
        val current = _shortcuts.value.filter { it.id != id }
        persist(current)
    }

    fun reorderShortcuts(list: List<TaskShortcut>) {
        persist(list)
    }

    private fun persist(list: List<TaskShortcut>) {
        _shortcuts.value = list
        prefs.edit().putString(KEY_SHORTCUTS, serialize(list)).apply()
    }

    private fun serialize(shortcuts: List<TaskShortcut>): String {
        val array = JSONArray()
        for (s in shortcuts) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("iconName", s.iconName)
            obj.put("showInBubble", s.showInBubble)
            val actionsArray = JSONArray()
            for (action in s.actions) {
                val aObj = JSONObject()
                when (action) {
                    is TaskAction.OpenApp -> {
                        aObj.put("type", "open_app")
                        aObj.put("packageName", action.packageName)
                        aObj.put("appLabel", action.appLabel)
                    }
                    is TaskAction.Wifi -> {
                        aObj.put("type", "wifi")
                        aObj.put("op", action.type.name)
                    }
                    is TaskAction.MobileData -> {
                        aObj.put("type", "mobile_data")
                        aObj.put("op", action.type.name)
                    }
                    is TaskAction.Flashlight -> {
                        aObj.put("type", "flashlight")
                        aObj.put("op", action.type.name)
                    }
                    is TaskAction.LowPower -> {
                        aObj.put("type", "low_power")
                        aObj.put("op", action.type.name)
                    }
                    is TaskAction.AirplaneMode -> {
                        aObj.put("type", "airplane_mode")
                        aObj.put("op", action.type.name)
                    }
                    is TaskAction.Bluetooth -> {
                        aObj.put("type", "bluetooth")
                        aObj.put("op", action.type.name)
                    }
                    is TaskAction.RingerMode -> {
                        aObj.put("type", "ringer_mode")
                        aObj.put("op", action.type.name)
                    }
                    is TaskAction.SetBrightness -> {
                        aObj.put("type", "brightness")
                        aObj.put("level", action.level)
                    }
                    is TaskAction.SetVolume -> {
                        aObj.put("type", "volume")
                        aObj.put("level", action.level)
                    }
                    is TaskAction.TakeScreenshot -> {
                        aObj.put("type", "screenshot")
                    }
                    is TaskAction.GoHome -> {
                        aObj.put("type", "go_home")
                    }
                    is TaskAction.MediaControls -> {
                        aObj.put("type", "media_controls")
                        aObj.put("op", action.type.name)
                    }
                    is TaskAction.SendNotification -> {
                        aObj.put("type", "notification")
                        aObj.put("title", action.title)
                        aObj.put("text", action.text)
                    }
                }
                actionsArray.put(aObj)
            }
            obj.put("actions", actionsArray)
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserialize(json: String): List<TaskShortcut> {
        val list = mutableListOf<TaskShortcut>()
        runCatching {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                val name = obj.optString("name", "Untitled")
                val iconName = obj.optString("iconName", "Bolt")
                val showInBubble = obj.optBoolean("showInBubble", true)
                val actionsList = mutableListOf<TaskAction>()
                val actionsArray = obj.optJSONArray("actions")
                if (actionsArray != null) {
                    for (j in 0 until actionsArray.length()) {
                        val aObj = actionsArray.getJSONObject(j)
                        val type = aObj.optString("type")
                        val op = aObj.optString("op")
                        val action = when (type) {
                            "open_app" -> TaskAction.OpenApp(
                                packageName = aObj.optString("packageName"),
                                appLabel = aObj.optString("appLabel")
                            )
                            "wifi" -> TaskAction.Wifi(runCatching { ToggleActionType.valueOf(op) }.getOrDefault(ToggleActionType.TOGGLE))
                            "mobile_data" -> TaskAction.MobileData(runCatching { ToggleActionType.valueOf(op) }.getOrDefault(ToggleActionType.TOGGLE))
                            "flashlight" -> TaskAction.Flashlight(runCatching { ToggleActionType.valueOf(op) }.getOrDefault(ToggleActionType.TOGGLE))
                            "low_power" -> TaskAction.LowPower(runCatching { ToggleActionType.valueOf(op) }.getOrDefault(ToggleActionType.TOGGLE))
                            "airplane_mode" -> TaskAction.AirplaneMode(runCatching { ToggleActionType.valueOf(op) }.getOrDefault(ToggleActionType.TOGGLE))
                            "bluetooth" -> TaskAction.Bluetooth(runCatching { ToggleActionType.valueOf(op) }.getOrDefault(ToggleActionType.TOGGLE))
                            "ringer_mode" -> TaskAction.RingerMode(runCatching { RingerActionType.valueOf(op) }.getOrDefault(RingerActionType.TOGGLE))
                            "brightness" -> TaskAction.SetBrightness(aObj.optInt("level", 50))
                            "volume" -> TaskAction.SetVolume(aObj.optInt("level", 50))
                            "screenshot" -> TaskAction.TakeScreenshot
                            "go_home" -> TaskAction.GoHome
                            "media_controls" -> TaskAction.MediaControls(runCatching { MediaActionType.valueOf(op) }.getOrDefault(MediaActionType.TOGGLE_PLAYBACK))
                            "notification" -> TaskAction.SendNotification(
                                title = aObj.optString("title", "Task Notification"),
                                text = aObj.optString("text", "Shortcut executed")
                            )
                            else -> null
                        }
                        if (action != null) actionsList.add(action)
                    }
                }
                list.add(TaskShortcut(id, name, iconName, showInBubble, actionsList))
            }
        }
        return list
    }

    companion object {
        private const val KEY_SHORTCUTS = "tasks_shortcuts_list"

        @Volatile
        private var instance: TasksRepository? = null

        fun getInstance(context: Context): TasksRepository {
            return instance ?: synchronized(this) {
                instance ?: TasksRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
