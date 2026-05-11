package com.example.mediremind.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

object MedicationPhotoMatcher {

    private const val MATCH_THRESHOLD = 78
    private const val WARNING_THRESHOLD = 58
    private const val CENTER_CROP_FRACTION = 0.65f

    private const val WEIGHT_HUE = 0.45f
    private const val WEIGHT_SATURATION = 0.35f
    private const val WEIGHT_EDGE = 0.20f

    data class MatchResult(
        val score: Int,
        val zone: MatchZone,
        val message: String,
        val debugDetail: String
    )

    enum class MatchZone {
        MATCH,
        WARNING,
        NO_MATCH
    }

    suspend fun compare(
        context: Context,
        referenceUri: String,
        capturedUri: String
    ): MatchResult {
        val refBitmap = loadBitmap(context, referenceUri)
            ?: return failResult("Could not read the saved reference photo. Retake it in Medication Setup.")
        val capBitmap = loadBitmap(context, capturedUri)
            ?: return failResult("Could not read the live photo. Try again.")

        val refCropped = detectAndCrop(refBitmap)
        val capCropped = detectAndCrop(capBitmap)

        val hueScore = compareHueHistogram(refCropped, capCropped)
        val satScore = compareSaturationHistogram(refCropped, capCropped)
        val edgeScore = compareEdgeDensity(refCropped, capCropped)

        val finalScore = (
            hueScore * WEIGHT_HUE +
                satScore * WEIGHT_SATURATION +
                edgeScore * WEIGHT_EDGE
            ).toInt().coerceIn(0, 100)

        val debug = "Hue ${hueScore.toInt()}% | Sat ${satScore.toInt()}% | Edge ${edgeScore.toInt()}%"

        return when {
            finalScore >= MATCH_THRESHOLD -> MatchResult(
                score = finalScore,
                zone = MatchZone.MATCH,
                message = "Live photo matches the saved medicine.",
                debugDetail = debug
            )

            finalScore >= WARNING_THRESHOLD -> MatchResult(
                score = finalScore,
                zone = MatchZone.WARNING,
                message = "Photo looks similar but not quite the same. Check the medicine and confirm if correct.",
                debugDetail = debug
            )

            else -> MatchResult(
                score = finalScore,
                zone = MatchZone.NO_MATCH,
                message = "This does not look like the saved medicine. Please retake the photo of the actual bottle or blister pack.",
                debugDetail = debug
            )
        }
    }

    private suspend fun detectAndCrop(bitmap: Bitmap): Bitmap {
        return try {
            val detected = runObjectDetection(bitmap)
            if (detected != null) {
                val padX = (detected.width() * 0.15f).toInt()
                val padY = (detected.height() * 0.15f).toInt()
                val left = (detected.left - padX).coerceAtLeast(0)
                val top = (detected.top - padY).coerceAtLeast(0)
                val right = (detected.right + padX).coerceAtMost(bitmap.width)
                val bottom = (detected.bottom + padY).coerceAtMost(bitmap.height)
                val width = (right - left).coerceAtLeast(1)
                val height = (bottom - top).coerceAtLeast(1)
                Bitmap.createBitmap(bitmap, left, top, width, height)
            } else {
                centerCrop(bitmap)
            }
        } catch (_: Exception) {
            centerCrop(bitmap)
        }
    }

    private suspend fun runObjectDetection(bitmap: Bitmap): Rect? =
        suspendCancellableCoroutine { continuation ->
            val options = ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .build()
            val detector = ObjectDetection.getClient(options)
            val image = InputImage.fromBitmap(bitmap, 0)

            detector.process(image)
                .addOnSuccessListener { objects ->
                    val best = objects.maxByOrNull { obj ->
                        obj.boundingBox.width() * obj.boundingBox.height()
                    }
                    continuation.resume(best?.boundingBox)
                    detector.close()
                }
                .addOnFailureListener {
                    continuation.resume(null)
                    detector.close()
                }
        }

    private fun centerCrop(bitmap: Bitmap): Bitmap {
        val cropW = (bitmap.width * CENTER_CROP_FRACTION).toInt()
        val cropH = (bitmap.height * CENTER_CROP_FRACTION).toInt()
        val startX = (bitmap.width - cropW) / 2
        val startY = (bitmap.height - cropH) / 2
        return Bitmap.createBitmap(bitmap, startX, startY, cropW, cropH)
    }

    private fun compareHueHistogram(a: Bitmap, b: Bitmap): Float {
        val histA = buildHueHistogram(a)
        val histB = buildHueHistogram(b)
        return histogramIntersection(histA, histB) * 100f
    }

    private fun compareSaturationHistogram(a: Bitmap, b: Bitmap): Float {
        val histA = buildSaturationHistogram(a)
        val histB = buildSaturationHistogram(b)
        return histogramIntersection(histA, histB) * 100f
    }

    private fun compareEdgeDensity(a: Bitmap, b: Bitmap): Float {
        val densityA = edgeDensity(a)
        val densityB = edgeDensity(b)
        val diff = abs(densityA - densityB)
        val maxVal = maxOf(densityA, densityB).coerceAtLeast(0.001f)
        val similarity = (1f - (diff / maxVal)).coerceIn(0f, 1f)
        return similarity * 100f
    }

    private fun buildHueHistogram(bitmap: Bitmap): FloatArray {
        val bins = 32
        val small = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        val hist = FloatArray(bins)
        val pixels = IntArray(64 * 64)
        small.getPixels(pixels, 0, 64, 0, 0, 64, 64)

        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF) / 255f
            val g = ((pixel shr 8) and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f
            val hue = rgbToHue(r, g, b)
            val bin = ((hue / 360f) * bins).toInt().coerceIn(0, bins - 1)
            hist[bin]++
        }

        val total = hist.sum().coerceAtLeast(1f)
        return FloatArray(bins) { hist[it] / total }
    }

    private fun buildSaturationHistogram(bitmap: Bitmap): FloatArray {
        val bins = 16
        val small = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        val hist = FloatArray(bins)
        val pixels = IntArray(64 * 64)
        small.getPixels(pixels, 0, 64, 0, 0, 64, 64)

        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF) / 255f
            val g = ((pixel shr 8) and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f
            val sat = rgbToSaturation(r, g, b)
            val bin = (sat * bins).toInt().coerceIn(0, bins - 1)
            hist[bin]++
        }

        val total = hist.sum().coerceAtLeast(1f)
        return FloatArray(bins) { hist[it] / total }
    }

    private fun edgeDensity(bitmap: Bitmap): Float {
        val small = Bitmap.createScaledBitmap(bitmap, 48, 48, true)
        val gray = Array(48) { y ->
            IntArray(48) { x ->
                val pixel = small.getPixel(x, y)
                ((pixel shr 16 and 0xFF) + (pixel shr 8 and 0xFF) + (pixel and 0xFF)) / 3
            }
        }

        var edgeCount = 0
        for (y in 1 until 47) {
            for (x in 1 until 47) {
                val gx = -gray[y - 1][x - 1] + gray[y - 1][x + 1] +
                    -2 * gray[y][x - 1] + 2 * gray[y][x + 1] +
                    -gray[y + 1][x - 1] + gray[y + 1][x + 1]
                val gy = -gray[y - 1][x - 1] - 2 * gray[y - 1][x] - gray[y - 1][x + 1] +
                    gray[y + 1][x - 1] + 2 * gray[y + 1][x] + gray[y + 1][x + 1]
                val magnitude = sqrt((gx * gx + gy * gy).toDouble())
                if (magnitude > 40.0) edgeCount++
            }
        }
        return edgeCount.toFloat() / (46 * 46)
    }

    private fun histogramIntersection(a: FloatArray, b: FloatArray): Float {
        var intersection = 0f
        val maxIndex = min(a.size, b.size)
        for (i in 0 until maxIndex) {
            intersection += min(a[i], b[i])
        }
        return intersection.coerceIn(0f, 1f)
    }

    private fun rgbToHue(r: Float, g: Float, b: Float): Float {
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        if (delta < 0.0001f) return 0f
        val hue = when (max) {
            r -> 60f * (((g - b) / delta) % 6)
            g -> 60f * (((b - r) / delta) + 2)
            else -> 60f * (((r - g) / delta) + 4)
        }
        return if (hue < 0f) hue + 360f else hue
    }

    private fun rgbToSaturation(r: Float, g: Float, b: Float): Float {
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val l = (max + min) / 2f
        val delta = max - min
        if (delta < 0.0001f) return 0f
        return delta / (1f - abs(2f * l - 1f))
    }

    private fun loadBitmap(context: Context, uriString: String): Bitmap? = runCatching {
        context.contentResolver.openInputStream(Uri.parse(uriString))?.use {
            BitmapFactory.decodeStream(it)
        }
    }.getOrNull()

    private fun failResult(message: String) = MatchResult(
        score = 0,
        zone = MatchZone.NO_MATCH,
        message = message,
        debugDetail = "Load failed"
    )
}
