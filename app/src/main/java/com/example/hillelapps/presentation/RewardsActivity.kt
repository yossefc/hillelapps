package com.example.hillelapps.presentation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.hillelapps.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Activité pour le mini-jeu et les récompenses
 */
class RewardsActivity : FragmentActivity() {

    private lateinit var pointsManager: PointsManager
    private lateinit var pointsTextView: TextView
    private lateinit var streakTextView: TextView
    private lateinit var badgeImageView: ImageView
    private lateinit var playGameButton: Button
    private lateinit var returnButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewards)

        pointsManager = PointsManager(this)

        pointsTextView = findViewById(R.id.pointsTextView)
        streakTextView = findViewById(R.id.streakTextView)
        badgeImageView = findViewById(R.id.badgeImageView)
        playGameButton = findViewById(R.id.playGameButton)
        returnButton = findViewById(R.id.returnButton)

        updateUI()

        playGameButton.setOnClickListener {
            startGame()
        }

        returnButton.setOnClickListener {
            finish()
        }
    }

    private fun updateUI() {
        pointsTextView.text = "${pointsManager.getCurrentPoints()} points"
        streakTextView.text = "Série: ${pointsManager.getCurrentStreak()} jours"

        // Afficher les badges débloqués
        val unlockedRewards = pointsManager.getAvailableRewards().filter {
            pointsManager.isRewardUnlocked(it.id)
        }

        if (unlockedRewards.isNotEmpty()) {
            // Afficher le dernier badge débloqué
            // (ici on mettrait l'image du badge dans badgeImageView)
            badgeImageView.visibility = View.VISIBLE
        } else {
            badgeImageView.visibility = View.INVISIBLE
        }
    }

    private fun startGame() {
        val intent = Intent(this, MiniGameActivity::class.java)
        startActivity(intent)
    }
}
