package com.example.chatapp.repositories

import android.R.attr.bitmap
import android.graphics.Bitmap
import android.util.Log
import com.example.chatapp.data.Resource
import com.example.chatapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject


class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun signIn(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
        profileImage: Bitmap
    ): Resource<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val storageRef = addProfileImageToStorage(profileImage)
            val profileImageUri = storageRef.downloadUrl.await()
            result.user?.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(name).setPhotoUri(profileImageUri).build()
            )?.await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    private suspend fun addProfileImageToStorage(profileImage: Bitmap): StorageReference {
        val filename = auth.currentUser?.uid
        val storageRef = storage.getReference("${Constants.KEY_PROFILE_IMAGES}$filename")
        val baos = ByteArrayOutputStream()
        profileImage.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data: ByteArray = baos.toByteArray()
        storageRef.putBytes(data).await()
        return storageRef
    }

    override fun logout() {
        auth.signOut()
    }

    override suspend fun sendEmailVerification() {
        currentUser?.sendEmailVerification()
    }
}