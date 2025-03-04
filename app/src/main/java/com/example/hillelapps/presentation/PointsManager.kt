package com.example.hillelapps.presentation

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Classe pour gérer le système de points et récompenses
 */
class PointsManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "points_prefs", Context.MODE_PRIVATE
    )
    private var databaseRef: DatabaseReference? = null

    init {
        try {
            databaseRef = FirebaseDatabase.getInstance().getReference("points")
        } catch (e: Exception) {
            // Gérer l'erreur si Firebase n'est pas disponible
        }
    }

    // Points actuels
    fun getCurrentPoints(): Int {
        return sharedPreferences.getInt("current_points", 0)
    }

    // Total des points gagnés
    fun getTotalPoints(): Int {
        return sharedPreferences.getInt("total_points", 0)
    }

    // Streak actuel (jours consécutifs)
    fun getCurrentStreak(): Int {
        return sharedPreferences.getInt("current_streak", 0)
    }

    // Meilleur streak
    fun getBestStreak(): Int {
        return sharedPreferences.getInt("best_streak", 0)
    }

    // Ajouter des points pour une confirmation à temps
    fun addPointsForConfirmation(onTime: Boolean) {
        val pointsToAdd = if (onTime) 10 else 5
        val currentPoints = getCurrentPoints() + pointsToAdd
        val totalPoints = getTotalPoints() + pointsToAdd

        sharedPreferences.edit().apply {
            putInt("current_points", currentPoints)
            putInt("total_points", totalPoints)
            apply()
        }

        if (onTime) {
            incrementStreak()
        } else {
            resetStreak()
        }

        // Enregistrer dans Firebase
        savePointsToFirebase(PointsRecord(
            timestamp = System.currentTimeMillis(),
            pointsEarned = pointsToAdd,
            currentTotal = currentPoints,
            onTime = onTime
        ))

        // Enregistrer dans Room
        savePointsToRoom(PointsRecord(
            timestamp = System.currentTimeMillis(),
            pointsEarned = pointsToAdd,
            currentTotal = currentPoints,
            onTime = onTime
        ))
    }

    // Dépenser des points (pour débloquer des récompenses)
    fun spendPoints(amount: Int): Boolean {
        val currentPoints = getCurrentPoints()
        if (currentPoints >= amount) {
            sharedPreferences.edit().putInt("current_points", currentPoints - amount).apply()
            return true
        }
        return false
    }

    // Incrémenter le streak
    private fun incrementStreak() {
        val currentStreak = getCurrentStreak() + 1
        val bestStreak = getBestStreak()

        sharedPreferences.edit().apply {
            putInt("current_streak", currentStreak)
            if (currentStreak > bestStreak) {
                putInt("best_streak", currentStreak)
            }
            apply()
        }
    }

    // Réinitialiser le streak
    private fun resetStreak() {
        sharedPreferences.edit().putInt("current_streak", 0).apply()
    }

    // Enregistrer dans Firebase
    private fun savePointsToFirebase(pointsRecord: PointsRecord) {
        databaseRef?.push()?.setValue(pointsRecord)
    }

    // Enregistrer dans Room
    private fun savePointsToRoom(pointsRecord: PointsRecord) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getDatabase(context).pointsDao().insert(
                    PointsEntity(
                        timestamp = pointsRecord.timestamp,
                        pointsEarned = pointsRecord.pointsEarned,
                        currentTotal = pointsRecord.currentTotal,
                        onTime = pointsRecord.onTime
                    )
                )
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }

    // Obtenir les niveaux débloqués
    fun getUnlockedLevels(): List<Int> {
        val totalPoints = getTotalPoints()
        val levels = mutableListOf<Int>()

        // Chaque niveau est débloqué tous les 50 points
        for (i in 1..(totalPoints / 50) + 1) {
            levels.add(i)
        }

        return levels
    }

    // Vérifier si l'utilisateur a suffisamment de points pour débloquer un niveau
    fun canUnlockLevel(level: Int): Boolean {
        val requiredPoints = level * 50
        return getTotalPoints() >= requiredPoints
    }

    // Les différentes récompenses disponibles
    fun getAvailableRewards(): List<Reward> {
        return listOf(
            Reward(1, "Badge étoile", 50, RewardType.BADGE),
            Reward(2, "Fond d'écran spécial", 100, RewardType.THEME),
            Reward(3, "Animation de victoire", 150, RewardType.ANIMATION),
            Reward(4, "Nouveau personnage", 200, RewardType.CHARACTER)
        )
    }

    // Vérifier si une récompense est déverrouillée
    fun isRewardUnlocked(rewardId: Int): Boolean {
        return sharedPreferences.getBoolean("reward_$rewardId", false)
    }

    // Débloquer une récompense
    fun unlockReward(rewardId: Int) {
        sharedPreferences.edit().putBoolean("reward_$rewardId", true).apply()
    }
}

// Classe pour stocker les enregistrements de points
data class PointsRecord(
    val timestamp: Long,
    val pointsEarned: Int,
    val currentTotal: Int,
    val onTime: Boolean
)

// Entité Room pour les points
@Entity(tableName = "points")
data class PointsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val pointsEarned: Int,
    val currentTotal: Int,
    val onTime: Boolean,
    val isSynced: Boolean = false
)

// DAO pour les points
@Dao
interface PointsDao {
    @Insert
    suspend fun insert(points: PointsEntity)

    @Query("SELECT * FROM points ORDER BY timestamp DESC")
    suspend fun getAllPoints(): List<PointsEntity>

    @Query("SELECT * FROM points WHERE isSynced = 0")
    suspend fun getUnsyncedPoints(): List<PointsEntity>

    @Query("UPDATE points SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)
}

// Types de récompenses
enum class RewardType {
    BADGE, THEME, ANIMATION, CHARACTER
}

// Classe pour les récompenses
data class Reward(
    val id: Int,
    val name: String,
    val cost: Int,
    val type: RewardType
)