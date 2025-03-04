package com.example.hillelapps.presentation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.hillelapps.R
import java.util.Random

class MiniGameActivity : FragmentActivity() {

    private lateinit var gameView: View
    private lateinit var characterView: ImageView
    private lateinit var targetView: ImageView
    private lateinit var scoreTextView: TextView
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mini_game)

        gameView = findViewById(R.id.gameView)
        characterView = findViewById(R.id.characterView)
        targetView = findViewById(R.id.targetView)
        scoreTextView = findViewById(R.id.scoreTextView)

        updateScore(0)

        // Configurer le jeu
        setupGame()

        // Lorsque le joueur clique sur la cible
        targetView.setOnClickListener {
            // Incrémenter le score
            updateScore(score + 1)

            // Animer le personnage vers la cible
            animateCharacterToTarget()

            // Replacer la cible à un nouvel endroit aléatoire
            moveTargetToRandomPosition()
        }

        // Bouton pour quitter le jeu
        findViewById<Button>(R.id.quitGameButton).setOnClickListener {
            saveGameResults()
            finish()
        }
    }

    private fun setupGame() {
        // Positionner la cible aléatoirement
        moveTargetToRandomPosition()
    }

    private fun animateCharacterToTarget() {
        // Créer une animation pour déplacer le personnage vers la cible
        val moveX = ObjectAnimator.ofFloat(
            characterView,
            "translationX",
            characterView.translationX,
            targetView.translationX
        )

        val moveY = ObjectAnimator.ofFloat(
            characterView,
            "translationY",
            characterView.translationY,
            targetView.translationY
        )

        val animSet = AnimatorSet()
        animSet.play(moveX).with(moveY)
        animSet.duration = 500
        animSet.interpolator = AccelerateDecelerateInterpolator()

        animSet.start()
    }

    private fun moveTargetToRandomPosition() {
        // Obtenir les dimensions disponibles
        val gameWidth = gameView.width - targetView.width
        val gameHeight = gameView.height - targetView.height

        if (gameWidth <= 0 || gameHeight <= 0) {
            // Si les dimensions ne sont pas encore disponibles, attendre le prochain cycle de layout
            gameView.post { moveTargetToRandomPosition() }
            return
        }

        // Générer une position aléatoire
        val random = Random()
        val newX = random.nextInt(gameWidth).toFloat()
        val newY = random.nextInt(gameHeight).toFloat()

        // Animer la cible vers la nouvelle position
        val moveX = ObjectAnimator.ofFloat(targetView, "translationX", newX)
        val moveY = ObjectAnimator.ofFloat(targetView, "translationY", newY)

        val animSet = AnimatorSet()
        animSet.playTogether(moveX, moveY)
        animSet.duration = 300
        animSet.start()
    }

    private fun updateScore(newScore: Int) {
        score = newScore
        scoreTextView.text = "Score: $score"
    }

    private fun saveGameResults() {
        // Sauvegarder le score et donner des points
        val pointsManager = PointsManager(this)

        // Ajouter des points basés sur le score (par exemple, 1 point pour 5 points de jeu)
        val pointsToAdd = score / 5
        if (pointsToAdd > 0) {
            // Ici, on pourrait créer une méthode spécifique pour ajouter des points du jeu
            // Pour l'instant, on utilise la méthode existante
            pointsManager.addPointsForConfirmation(true)

            // Afficher un message de félicitation
            showCongratulationsAnimation()
        }
    }

    private fun showCongratulationsAnimation() {
        // Implémenter une animation de félicitation
        // Par exemple, faire apparaître des étoiles, jouer un son, etc.
    }
}

