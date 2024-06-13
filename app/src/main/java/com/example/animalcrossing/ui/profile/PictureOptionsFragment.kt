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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.User
import com.example.animalcrossing.data.repository.UserRepository
import com.example.animalcrossing.databinding.FragmentPictureOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

/**
 * A bottom sheet dialog fragment for selecting and capturing profile pictures.
 *
 * @property profile The user profile for which the picture is being managed.
 * @property userRepository The repository for user-related operations.
 */
class PictureOptionsFragment(
    private val profile: User,
    private val userRepository: UserRepository
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentPictureOptionsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPictureOptionsBinding.inflate(inflater, container, false)

        binding.buttonGallery.setOnClickListener {
            openGallery()
        }

        binding.buttonTakePhoto.setOnClickListener {
            takePhoto()
        }

        return binding.root
    }

    /**
     * Opens the device's gallery to select an image.
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE)
    }

    /**
     * Initiates the process to take a photo using the device's camera.
     * Requests camera permission if not granted.
     */
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

    /**
     * Opens the device's camera to capture an image.
     */
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.open_camera_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Handles the result of permission requests for camera access.
     */
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
                        getString(R.string.permission_camera_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Handles the result of activities for capturing or selecting an image.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            uploadAndSetProfilePicture(imageBitmap)
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
                    Toast.makeText(requireContext(), getString(R.string.load_image_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Uploads the selected or captured image to Firebase Storage and updates the user's profile picture.
     *
     * @param bitmap The Bitmap image to upload.
     */
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
                Toast.makeText(requireContext(), getString(R.string.upload_image_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Updates the user's profile picture URL in Firestore and locally in the app.
     *
     * @param profilePicture The URL of the new profile picture.
     */
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
                    Toast.makeText(requireContext(), getString(R.string.update_image_error), Toast.LENGTH_SHORT).show()
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