package com.bunny.chargingmonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvWattage: TextView
    private lateinit var tvVoltage: TextView
    private lateinit var tvCurrent: TextView
    private lateinit var tvBatteryPercent: TextView
    private lateinit var tvChargingStatus: TextView
    private lateinit var tvChargingType: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvHealth: TextView
    private lateinit var tvWattLabel: TextView

    private lateinit var batteryManager: BatteryManager
    private val handler = Handler(Looper.getMainLooper())

    // Refresh every 1 second for real-time feel
    private val refreshInterval = 1000L

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateBatteryStats()
            handler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager

        tvWattage = findViewById(R.id.tvWattage)
        tvVoltage = findViewById(R.id.tvVoltage)
        tvCurrent = findViewById(R.id.tvCurrent)
        tvBatteryPercent = findViewById(R.id.tvBatteryPercent)
        tvChargingStatus = findViewById(R.id.tvChargingStatus)
        tvChargingType = findViewById(R.id.tvChargingType)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvHealth = findViewById(R.id.tvHealth)
        tvWattLabel = findViewById(R.id.tvWattLabel)
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    private fun updateBatteryStats() {
        // --- Get current (in microamps) from BatteryManager API ---
        // BATTERY_PROPERTY_CURRENT_NOW returns microamps (µA)
        // Positive = charging, Negative = discharging (varies by OEM)
        val currentMicroAmps = batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CURRENT_NOW
        )

        // --- Get battery info via sticky broadcast ---
        val batteryIntent: Intent? = registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val voltageMilliVolts = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1

        // --- Calculate values ---
        val voltageV = voltageMilliVolts / 1000.0  // millivolts → volts
        val currentA = Math.abs(currentMicroAmps) / 1_000_000.0  // microamps → amps
        val wattage = voltageV * currentA  // Power = V × I
        val batteryPercent = if (scale > 0) (level * 100 / scale) else 0
        val tempCelsius = tempTenths / 10.0

        // --- Update UI ---
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        if (isCharging) {
            tvWattage.text = String.format("%.1f", wattage)
            tvWattLabel.text = "Watts"
            tvWattage.setTextColor(ContextCompat.getColor(this, R.color.green_accent))
        } else {
            tvWattage.text = "0.0"
            tvWattLabel.text = "Watts (Not Charging)"
            tvWattage.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }

        tvVoltage.text = String.format("%.2f V", voltageV)
        tvCurrent.text = String.format("%.2f A  (%.0f mA)", currentA, currentA * 1000)
        tvBatteryPercent.text = "$batteryPercent%"
        tvTemperature.text = String.format("%.1f °C", tempCelsius)

        tvChargingStatus.text = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "⚡ Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "🔋 Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "✅ Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "⏸ Not Charging"
            else -> "Unknown"
        }

        tvChargingType.text = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "🔌 AC Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "🔌 USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "🔌 Wireless"
            else -> "Unplugged"
        }

        tvHealth.text = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good ✅"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat ⚠️"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead ❌"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage ⚠️"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold 🥶"
            else -> "Unknown"
        }
    }
}
