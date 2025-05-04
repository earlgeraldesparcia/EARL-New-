package com.personal.keyweaver

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.personal.keyweaver.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupClickListeners() {
        binding.switchAutoSave.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this,
                if (isChecked) "Auto-save enabled" else "Auto-save disabled",
                Toast.LENGTH_SHORT).show()
        }

        binding.switchAutoCopy.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this,
                if (isChecked) "Auto-copy enabled" else "Auto-copy disabled",
                Toast.LENGTH_SHORT).show()
        }

        binding.btnAbout.setOnClickListener {
            // Handle about click
            Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    // Handle logout
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}