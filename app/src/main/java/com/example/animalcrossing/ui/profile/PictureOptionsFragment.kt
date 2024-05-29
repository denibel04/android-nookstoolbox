package com.example.animalcrossing.ui.profile

import android.app.Activity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.animalcrossing.data.repository.User
import com.example.animalcrossing.data.repository.UserRepository
import com.example.animalcrossing.databinding.FragmentPictureOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import javax.inject.Inject

class PictureOptionsFragment(private val profile: User, private val userRepository: UserRepository) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentPictureOptionsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPictureOptionsBinding.inflate(inflater, container, false)

        binding.buttonGallery.setOnClickListener {
            openGallery()
        }

        binding.buttonTakePhoto.setOnClickListener {
            takePhoto()
        }



        return binding.root
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE)
    }


    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            openCamera()
        }
    }


    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(
                requireContext(),
                "No se pudo abrir la cámara.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Permiso de cámara denegado.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            uploadAndSetProfilePicture(imageBitmap)
            Log.d(TAG, "Bitmap capturado: $imageBitmap")
        } else if (requestCode == REQUEST_GALLERY_IMAGE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let { uri ->
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val selectedImageBitmap = BitmapFactory.decodeStream(inputStream)
                    uploadAndSetProfilePicture(selectedImageBitmap)
                    this.dismiss()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error al cargar la imagen.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadAndSetProfilePicture(bitmap: Bitmap) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef: StorageReference = storageRef.child("images/${UUID.randomUUID()}.jpg")
    
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData: ByteArray = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(imageData)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imagesRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                saveProfilePicture(downloadUri.toString())
            } else {
                Toast.makeText(requireContext(), "Error al subir la imagen.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfilePicture(profilePicture: String) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(user.uid)

            userRef.update("profile_picture", profilePicture)
                .addOnSuccessListener {
                    profile.profile_picture = profilePicture
                    lifecycleScope.launch {
                        userRepository.changeProfilePicture(profile)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al actualizar la imagen de perfil.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 102
        private const val REQUEST_CAMERA_PERMISSION = 103
        private const val REQUEST_GALLERY_IMAGE = 101
        const val TAG = "ModalBottomSheet"
    }
}