package com.bhumaji.wordbuddy.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.media.FaceDetector
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * The child's name and up to 4 photos used for her animated buddy and stickers.
 * Photos are face-cropped on the phone and saved only in the app's private storage.
 */
class ProfileStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("word_buddy_profile", Context.MODE_PRIVATE)
    private val bitmaps = mutableListOf<Bitmap>()

    var name by mutableStateOf(prefs.getString(KEY_NAME, "") ?: "")
        private set

    /** Observable copies for the UI. */
    val photos = mutableStateListOf<ImageBitmap>()

    init {
        for (i in 0 until MAX_PHOTOS) {
            val f = file(i)
            if (f.exists()) BitmapFactory.decodeFile(f.path)?.let { bitmaps.add(it) }
        }
        sync()
    }

    val canAddMore: Boolean get() = bitmaps.size < MAX_PHOTOS

    /** Photo for slot [i], cycling through the ones available; null if none added. */
    fun photo(i: Int): ImageBitmap? = if (photos.isEmpty()) null else photos[i.mod(photos.size)]

    fun changeName(newName: String) {
        name = newName.take(20)
        prefs.edit().putString(KEY_NAME, name.trim()).apply()
    }

    suspend fun addPhotos(uris: List<Uri>): Int {
        val room = MAX_PHOTOS - bitmaps.size
        val fresh = withContext(Dispatchers.IO) {
            uris.take(room).mapNotNull { runCatching { prepare(it) }.getOrNull() }
        }
        bitmaps.addAll(fresh)
        persist()
        return fresh.size
    }

    suspend fun rotate(index: Int) {
        if (index !in bitmaps.indices) return
        bitmaps[index] = withContext(Dispatchers.Default) { rotated(bitmaps[index], 90f) }
        persist()
    }

    suspend fun remove(index: Int) {
        if (index !in bitmaps.indices) return
        bitmaps.removeAt(index)
        persist()
    }

    private fun file(i: Int) = File(context.filesDir, "buddy_$i.jpg")

    private fun sync() {
        photos.clear()
        photos.addAll(bitmaps.map { it.asImageBitmap() })
    }

    private suspend fun persist() {
        withContext(Dispatchers.IO) {
            for (i in 0 until MAX_PHOTOS) {
                val f = file(i)
                if (i < bitmaps.size) {
                    FileOutputStream(f).use { bitmaps[i].compress(Bitmap.CompressFormat.JPEG, 90, it) }
                } else if (f.exists()) {
                    f.delete()
                }
            }
        }
        sync()
    }

    /** Decode a picked photo, fix its rotation, and crop a square around her face. */
    private fun prepare(uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= 900 && bounds.outHeight / (sample * 2) >= 900) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return null

        val orientation = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val upright = if (degrees != 0f) rotated(decoded, degrees) else decoded
        val square = faceSquare(upright)
        return Bitmap.createScaledBitmap(square, OUT_SIZE, OUT_SIZE, true)
    }

    private fun rotated(src: Bitmap, degrees: Float): Bitmap {
        val m = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }

    /** Uses Android's built-in (offline) face detector; falls back to a centre crop. */
    private fun faceSquare(src: Bitmap): Bitmap {
        val w = src.width and 1.inv() // detector needs an even width
        val h = src.height
        val shortest = minOf(w, h)
        try {
            val rgb = Bitmap.createBitmap(src, 0, 0, w, h).copy(Bitmap.Config.RGB_565, false)
            val faces = arrayOfNulls<FaceDetector.Face>(1)
            val found = FaceDetector(w, h, 1).findFaces(rgb, faces)
            val face = faces[0]
            if (found > 0 && face != null) {
                val mid = PointF()
                face.getMidPoint(mid)
                val eyes = face.eyesDistance()
                val side = (eyes * 4.5f).toInt().coerceIn(shortest / 4, shortest)
                val cx = mid.x.toInt()
                val cy = (mid.y + eyes * 0.4f).toInt()
                val left = (cx - side / 2).coerceIn(0, w - side)
                val top = (cy - side / 2).coerceIn(0, h - side)
                return Bitmap.createBitmap(src, left, top, side, side)
            }
        } catch (_: Exception) {
            // fall through to centre crop
        }
        return Bitmap.createBitmap(src, (src.width - shortest) / 2, (src.height - shortest) / 2, shortest, shortest)
    }

    companion object {
        const val MAX_PHOTOS = 4
        private const val OUT_SIZE = 512
        private const val KEY_NAME = "name"
    }
}
