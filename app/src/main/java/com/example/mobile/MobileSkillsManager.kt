package com.example.mobile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import com.example.models.MobileSkill

class MobileSkillsManager(private val context: Context) {

    private var isTorchOn = false

    val availableSkills = listOf(
        MobileSkill("app_launcher", "App Search & Launch", "System", "Open any installed application", "ic_app_launch", "ACTION_APP_LAUNCH"),
        MobileSkill("flashlight", "Flashlight Control", "Hardware", "Toggle LED torch flashlight", "ic_flashlight", "ACTION_FLASHLIGHT"),
        MobileSkill("wifi", "Wi-Fi Settings", "Connectivity", "Open Wi-Fi network configuration", "ic_wifi", "ACTION_WIFI"),
        MobileSkill("bluetooth", "Bluetooth Manager", "Connectivity", "Configure Bluetooth devices", "ic_bluetooth", "ACTION_BLUETOOTH"),
        MobileSkill("hotspot", "Hotspot Control", "Connectivity", "Tethering & Portable Hotspot settings", "ic_hotspot", "ACTION_HOTSPOT"),
        MobileSkill("volume", "Sound & Volume", "Audio", "Adjust device volume levels", "ic_volume", "ACTION_VOLUME"),
        MobileSkill("brightness", "Display Brightness", "Display", "Adjust screen brightness settings", "ic_brightness", "ACTION_BRIGHTNESS"),
        MobileSkill("phone", "Phone Call", "Communication", "Dial phone numbers or contacts", "ic_phone", "ACTION_CALL"),
        MobileSkill("sms", "Send SMS", "Communication", "Compose text messages", "ic_sms", "ACTION_SMS"),
        MobileSkill("email", "Send Email", "Communication", "Draft and send emails", "ic_email", "ACTION_EMAIL"),
        MobileSkill("alarm", "Set Alarm", "Time", "Configure alarms & timers", "ic_alarm", "ACTION_ALARM"),
        MobileSkill("calendar", "Calendar Events", "Time", "View or add calendar appointments", "ic_calendar", "ACTION_CALENDAR"),
        MobileSkill("calculator", "Calculator", "Tools", "Perform quick calculations", "ic_calculator", "ACTION_CALCULATOR"),
        MobileSkill("maps", "GPS Navigation", "Location", "Open Google Maps navigation", "ic_maps", "ACTION_MAPS"),
        MobileSkill("gallery", "Gallery & Photos", "Media", "View system photo gallery", "ic_gallery", "ACTION_GALLERY"),
        MobileSkill("files", "File Manager", "Storage", "Browse system files and downloads", "ic_files", "ACTION_FILES"),
        MobileSkill("clipboard", "Clipboard Manager", "Tools", "Copy or read clipboard buffer", "ic_clipboard", "ACTION_CLIPBOARD")
    )

    fun executeSkill(skillKey: String, parameter: String = ""): String {
        return try {
            when (skillKey) {
                "ACTION_FLASHLIGHT", "flashlight" -> toggleFlashlight()
                "ACTION_WIFI", "wifi" -> openWifiSettings()
                "ACTION_BLUETOOTH", "bluetooth" -> openBluetoothSettings()
                "ACTION_HOTSPOT", "hotspot" -> openHotspotSettings()
                "ACTION_VOLUME", "volume" -> openVolumeSettings()
                "ACTION_BRIGHTNESS", "brightness" -> openDisplaySettings()
                "ACTION_CALL", "phone" -> makePhoneCall(parameter)
                "ACTION_SMS", "sms" -> sendSms(parameter)
                "ACTION_EMAIL", "email" -> sendEmail(parameter)
                "ACTION_ALARM", "alarm" -> setAlarm("JARVIS Alarm", 8, 0)
                "ACTION_CALENDAR", "calendar" -> openCalendar()
                "ACTION_CALCULATOR", "calculator" -> openCalculator()
                "ACTION_MAPS", "maps" -> openMaps(parameter)
                "ACTION_GALLERY", "gallery" -> openGallery()
                "ACTION_FILES", "files" -> openFileManager()
                "ACTION_CLIPBOARD", "clipboard" -> copyToClipboard(parameter.ifEmpty { "JARVIS System Active" })
                "ACTION_APP_LAUNCH", "app_launcher" -> launchApp(parameter)
                else -> {
                    if (parameter.isNotEmpty()) launchApp(parameter)
                    else "JARVIS Action: Skill '$skillKey' initialized."
                }
            }
        } catch (e: Exception) {
            "JARVIS Mobile Control Error: ${e.localizedMessage}"
        }
    }

    private fun toggleFlashlight(): String {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        val cameraId = cameraManager?.cameraIdList?.firstOrNull()
        if (cameraManager != null && cameraId != null) {
            isTorchOn = !isTorchOn
            cameraManager.setTorchMode(cameraId, isTorchOn)
            return "JARVIS: Flashlight turned ${if (isTorchOn) "ON" else "OFF"}."
        }
        return "JARVIS: Flashlight hardware unavailable."
    }

    private fun openWifiSettings(): String {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        context.startActivity(intent)
        return "JARVIS: Opening Wi-Fi Control Panel..."
    }

    private fun openBluetoothSettings(): String {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        context.startActivity(intent)
        return "JARVIS: Opening Bluetooth Control Panel..."
    }

    private fun openHotspotSettings(): String {
        val intent = Intent().apply {
            action = Settings.ACTION_WIRELESS_SETTINGS
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return "JARVIS: Opening Wireless & Hotspot Settings..."
    }

    private fun openVolumeSettings(): String {
        val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        context.startActivity(intent)
        return "JARVIS: Opening Audio Volume Console..."
    }

    private fun openDisplaySettings(): String {
        val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        context.startActivity(intent)
        return "JARVIS: Opening Screen & Brightness Panel..."
    }

    private fun makePhoneCall(number: String): String {
        val cleanNumber = number.ifEmpty { "911" }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return "JARVIS: Dialing $cleanNumber..."
    }

    private fun sendSms(message: String): String {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:")).apply {
            putExtra("sms_body", message.ifEmpty { "Sent via JARVIS AI Operating System" })
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return "JARVIS: Opening SMS Composer..."
    }

    private fun sendEmail(subject: String): String {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject.ifEmpty { "JARVIS Automated Briefing" })
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return "JARVIS: Opening Mail Client..."
    }

    private fun setAlarm(label: String, hour: Int, minute: Int): String {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return "JARVIS: Setting Alarm for $hour:$minute..."
    }

    private fun openCalendar(): String {
        val intent = Intent(Intent.ACTION_VIEW, CalendarContract.CONTENT_URI).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return "JARVIS: Opening Calendar Console..."
    }

    private fun openCalculator(): String {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALCULATOR)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(fallback)
        }
        return "JARVIS: Opening Calculator Subsystem..."
    }

    private fun openMaps(query: String): String {
        val uri = if (query.isNotEmpty()) Uri.parse("geo:0,0?q=$query") else Uri.parse("geo:0,0")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
        return "JARVIS: Launching GPS Navigation Grid..."
    }

    private fun openGallery(): String {
        val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return "JARVIS: Accessing Visual Media Vault..."
    }

    private fun openFileManager(): String {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return "JARVIS: Accessing System Files..."
    }

    private fun copyToClipboard(text: String): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("JARVIS", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to JARVIS Clipboard", Toast.LENGTH_SHORT).show()
        return "JARVIS: Text buffered into clipboard memory."
    }

    private fun launchApp(appName: String): String {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(0)
        val target = packages.firstOrNull {
            val label = pm.getApplicationLabel(it).toString().lowercase()
            label.contains(appName.lowercase())
        }
        if (target != null) {
            val launchIntent = pm.getLaunchIntentForPackage(target.packageName)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                return "JARVIS: Launching ${pm.getApplicationLabel(target)}..."
            }
        }
        return "JARVIS Search: Unable to locate package matching '$appName'. Opening System Search..."
    }
}
