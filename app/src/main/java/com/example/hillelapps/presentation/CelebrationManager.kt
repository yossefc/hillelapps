package com.example.hillelapps.presentation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.hillelapps.R

/**
 * Gestionnaire d'animations pour les célébrations
 */
class CelebrationManager(private val context: Context) {

    // Afficher une animation de célébration quand l'enfant a pris son médicament
    fun showMedicationTakenCelebration(view: View) {
        if (view !is ViewGroup) return

        // Animation de base: faire tourner et grossir une étoile
        val star = ImageView(context).apply {
            setImageResource(R.drawable.ic_star)
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f
        }

        // Positionner l'étoile au centre de la vue
        star.x = (view.width / 2 - 50).toFloat()
        star.y = (view.height / 2 - 50).toFloat()

        view.addView(star)

        val fadeIn = ObjectAnimator.ofFloat(star, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(star, "scaleX", 0.5f, 1.5f)
        val scaleY = ObjectAnimator.ofFloat(star, "scaleY", 0.5f, 1.5f)
        val rotation = ObjectAnimator.ofFloat(star, "rotation", 0f, 360f)

        val animSet = AnimatorSet()
        animSet.playTogether(fadeIn, scaleX, scaleY, rotation)
        animSet.duration = 1000

        // Faire disparaître l'étoile après l'animation
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                val fadeOut = ObjectAnimator.ofFloat(star, "alpha", 1f, 0f)
                fadeOut.duration = 500
                fadeOut.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        view.removeView(star)
                    }
                })
                fadeOut.start()
            }
        })

        animSet.start()
    }

    // Afficher une animation quand l'enfant débloque une récompense
    fun showRewardUnlockedCelebration(view: View, rewardName: String) {
        if (view !is ViewGroup) return

        // Créer une vue de texte pour afficher le nom de la récompense
        val congratsText = TextView(context).apply {
            text = "Bravo ! Tu as débloqué: $rewardName"
            textSize = 16f
            setTextColor(context.getColor(android.R.color.black))
            alpha = 0f
        }

        // Positionner le texte
        congratsText.x = (view.width / 2 - 150).toFloat()
        congratsText.y = (view.height / 2).toFloat()

        view.addView(congratsText)

        // Animer le texte
        val fadeIn = ObjectAnimator.ofFloat(congratsText, "alpha", 0f, 1f)
        fadeIn.duration = 1000

        // Déplacer le texte vers le haut
        val moveUp = ObjectAnimator.ofFloat(
            congratsText,
            "translationY",
            50f,
            -50f
        )
        moveUp.duration = 2000

        val animSet = AnimatorSet()
        animSet.playTogether(fadeIn, moveUp)

        // Faire disparaître le texte après l'animation
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                val fadeOut = ObjectAnimator.ofFloat(congratsText, "alpha", 1f, 0f)
                fadeOut.duration = 500
                fadeOut.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        view.removeView(congratsText)
                    }
                })
                fadeOut.start()
            }
        })

        animSet.start()
    }

    // Afficher une animation de confettis lorsque l'enfant atteint un jalon important
    fun showConfettiCelebration(view: View) {
        if (view !is ViewGroup) return

        // Créer plusieurs confettis
        for (i in 0 until 20) {
            val confetti = View(context).apply {
                setBackgroundResource(getRandomConfettiColor())
                alpha = 0.8f
                scaleX = 0.2f
                scaleY = 0.2f
                // Taille du confetti
                layoutParams = ViewGroup.LayoutParams(20, 20)
            }

            // Position aléatoire en haut de l'écran
            confetti.x = (Math.random() * view.width).toFloat()
            confetti.y = -50f

            view.addView(confetti)

            // Animation de chute
            val fallDown = ObjectAnimator.ofFloat(
                confetti,
                "translationY",
                -50f,
                view.height + 50f
            )

            // Animation de balancement
            val swingLeft = ObjectAnimator.ofFloat(
                confetti,
                "translationX",
                confetti.x,
                confetti.x - 50 + (Math.random() * 100).toFloat()
            )

            // Animation de rotation
            val rotate = ObjectAnimator.ofFloat(
                confetti,
                "rotation",
                0f,
                360f * (Math.random() * 2).toFloat()
            )

            val animSet = AnimatorSet()
            animSet.playTogether(fallDown, swingLeft, rotate)
            animSet.duration = 1500 + (Math.random() * 1000).toLong()

            animSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    view.removeView(confetti)
                }
            })

            animSet.start()
        }
    }

    // Obtenir une couleur aléatoire pour les confettis
    private fun getRandomConfettiColor(): Int {
        val colors = intArrayOf(
            android.R.color.holo_red_light,
            android.R.color.holo_blue_light,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_purple
        )
        return colors[(Math.random() * colors.size).toInt()]
    }
}

// La classe AnimatorListenerAdapter
abstract class AnimatorListenerAdapter : android.animation.Animator.AnimatorListener {
    override fun onAnimationStart(animation: android.animation.Animator) {}
    override fun onAnimationEnd(animation: android.animation.Animator) {}
    override fun onAnimationCancel(animation: android.animation.Animator) {}
    override fun onAnimationRepeat(animation: android.animation.Animator) {}
}