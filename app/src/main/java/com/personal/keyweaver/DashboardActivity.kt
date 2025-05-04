package com.personal.keyweaver

import android.content.*
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.SecureRandom
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvGeneratedPassword: TextView
    private lateinit var seekBarLength: SeekBar
    private lateinit var tvPasswordLength: TextView
    private lateinit var checkboxUppercase: CheckBox
    private lateinit var checkboxLowercase: CheckBox
    private lateinit var checkboxNumbers: CheckBox
    private lateinit var checkboxSymbols: CheckBox
    private lateinit var btnGeneratePassword: Button
    private lateinit var btnSavePassword: Button
    private lateinit var btnCopyPassword: ImageButton
    private lateinit var btnViewSavedPasswords: Button
    private lateinit var tvSettings: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val MIN_PASSWORD_LENGTH = 6

    companion object {
        private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
        private const val NUMBERS = "0123456789"
        private const val SYMBOLS = "!@#$%^&*()-_=+{}[]|:;<>,.?/~`"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.dashboard)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize Views
        tvGeneratedPassword = findViewById(R.id.tvGeneratedPassword)
        seekBarLength = findViewById(R.id.seekBarLength)
        tvPasswordLength = findViewById(R.id.tvPasswordLength)
        checkboxUppercase = findViewById(R.id.checkboxUppercase)
        checkboxLowercase = findViewById(R.id.checkboxLowercase)
        checkboxNumbers = findViewById(R.id.checkboxNumbers)
        checkboxSymbols = findViewById(R.id.checkboxSymbols)
        btnGeneratePassword = findViewById(R.id.btnGeneratePassword)
        btnSavePassword = findViewById(R.id.btnSavePassword)
        btnCopyPassword = findViewById(R.id.btnCopyPassword)
        btnViewSavedPasswords = findViewById(R.id.btnViewPasswords)

        btnGeneratePassword.setOnClickListener { generatePassword() }
        btnSavePassword.setOnClickListener { savePassword() }
        btnCopyPassword.setOnClickListener { copyPasswordToClipboard() }

        seekBarLength.progress = MIN_PASSWORD_LENGTH
        tvPasswordLength.text = MIN_PASSWORD_LENGTH.toString()

        seekBarLength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val actualProgress = if (progress < MIN_PASSWORD_LENGTH) MIN_PASSWORD_LENGTH else progress
                seekBar?.progress = actualProgress
                tvPasswordLength.text = actualProgress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val tvUserEmail = findViewById<TextView>(R.id.tvUsername)
        val currentUser = auth.currentUser
        tvUserEmail.text = currentUser?.email ?: "No Email"
        tvUserEmail.setOnClickListener { view -> showLogoutMenu(view) }

        tvSettings = findViewById(R.id.tvSettings)
        tvSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun showLogoutMenu(anchor: View) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.menu_user_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.menu_logout) {
                showLogoutConfirmationDialog()
                true
            } else false
        }
        popupMenu.show()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun generatePassword() {
        val length = seekBarLength.progress
        val characterPool = buildString {
            if (checkboxUppercase.isChecked) append(UPPERCASE)
            if (checkboxLowercase.isChecked) append(LOWERCASE)
            if (checkboxNumbers.isChecked) append(NUMBERS)
            if (checkboxSymbols.isChecked) append(SYMBOLS)
        }

        if (characterPool.isEmpty()) {
            Toast.makeText(this, "Please select at least one option", Toast.LENGTH_SHORT).show()
            return
        }

        val random = SecureRandom()
        val password = buildString {
            repeat(length) {
                append(characterPool[random.nextInt(characterPool.length)])
            }
        }

        tvGeneratedPassword.text = password
    }

    private fun savePassword() {
        val password = tvGeneratedPassword.text.toString()
        val currentUser = auth.currentUser

        if (password.isEmpty()) {
            Toast.makeText(this, "Please generate a password first", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val passwordData = hashMapOf(
            "userId" to currentUser.uid,
            "email" to currentUser.email,
            "password" to password,
            "timestamp" to Date()
        )

        db.collection("users")
            .document(currentUser.uid)
            .collection("passwords")
            .add(passwordData)
            .addOnSuccessListener {
                Snackbar.make(findViewById(android.R.id.content), "Password saved successfully!", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Snackbar.make(findViewById(android.R.id.content), "Failed: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun copyPasswordToClipboard() {
        val password = tvGeneratedPassword.text.toString()

        if (password.isEmpty()) {
            Toast.makeText(this, "No password to copy", Toast.LENGTH_SHORT).show()
            return
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Password", password)
        clipboard.setPrimaryClip(clip)

        Snackbar.make(findViewById(android.R.id.content), "Password copied to clipboard!", Snackbar.LENGTH_SHORT).show()
    }
}
