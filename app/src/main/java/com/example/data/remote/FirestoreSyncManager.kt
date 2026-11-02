package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.HirafiDao
import com.example.data.model.CraftsmanEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirestoreSyncManager(
    private val context: Context,
    private val dao: HirafiDao,
    private val scope: CoroutineScope
) {
    private val TAG = "FirestoreSyncManager"
    private var listenerRegistration: ListenerRegistration? = null

    private val firestore: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank()) {
                val app = FirebaseApp.getInstance()
                FirebaseFirestore.getInstance(app, dbId)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firestore with custom DB ID", e)
            null
        }
    }

    fun startRealtimeSync() {
        val db = firestore ?: return
        try {
            listenerRegistration = db.collection("craftsmen")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore sync listen error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            val remoteCraftsmen = snapshot.documents.mapNotNull { doc ->
                                try {
                                    val id = doc.getLong("localId") ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it })
                                    CraftsmanEntity(
                                        id = id,
                                        name = doc.getString("name") ?: "",
                                        craft = doc.getString("craft") ?: "",
                                        experienceYears = (doc.getLong("experienceYears") ?: 5).toInt(),
                                        phone = doc.getString("phone") ?: "",
                                        whatsapp = doc.getString("whatsapp") ?: "",
                                        city = doc.getString("city") ?: "طرابلس",
                                        district = doc.getString("district") ?: "",
                                        latitude = doc.getDouble("latitude") ?: 32.8872,
                                        longitude = doc.getDouble("longitude") ?: 13.1913,
                                        bio = doc.getString("bio") ?: "",
                                        priceEstimate = doc.getString("priceEstimate") ?: "",
                                        isAvailable = doc.getBoolean("isAvailable") ?: true,
                                        hasEmergencyService = doc.getBoolean("hasEmergencyService") ?: false,
                                        isRegisteredByMe = false,
                                        isVipFeatured = doc.getBoolean("isVipFeatured") ?: false,
                                        specializations = doc.getString("specializations") ?: "",
                                        avatarSeed = doc.getString("avatarSeed") ?: "",
                                        createdAt = (doc.getTimestamp("createdAt")?.seconds ?: (System.currentTimeMillis() / 1000)) * 1000
                                    )
                                } catch (ex: Exception) {
                                    null
                                }
                            }
                            if (remoteCraftsmen.isNotEmpty()) {
                                dao.insertAllCraftsmen(remoteCraftsmen)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register snapshot listener", e)
        }
    }

    suspend fun pushCraftsmanToFirestore(craftsman: CraftsmanEntity, ownerId: String? = null) {
        val db = firestore ?: return
        val currentUserId = ownerId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
        val docId = "craftsman_${craftsman.id.coerceAtLeast(1)}"

        val data = hashMapOf(
            "ownerId" to currentUserId,
            "localId" to craftsman.id,
            "name" to craftsman.name,
            "craft" to craftsman.craft,
            "experienceYears" to craftsman.experienceYears,
            "phone" to craftsman.phone,
            "whatsapp" to craftsman.whatsapp,
            "city" to craftsman.city,
            "district" to craftsman.district,
            "latitude" to craftsman.latitude,
            "longitude" to craftsman.longitude,
            "bio" to craftsman.bio,
            "priceEstimate" to craftsman.priceEstimate,
            "isAvailable" to craftsman.isAvailable,
            "hasEmergencyService" to craftsman.hasEmergencyService,
            "isVipFeatured" to craftsman.isVipFeatured,
            "specializations" to craftsman.specializations,
            "avatarSeed" to craftsman.avatarSeed,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            db.collection("craftsmen").document(docId)
                .set(data, SetOptions.merge())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push craftsman to Firestore", e)
        }
    }

    fun stopRealtimeSync() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}
