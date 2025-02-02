package org.fdroid.fdroid

import android.graphics.Bitmap
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.DisplayCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.encode.Contents
import com.google.zxing.encode.QRCodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fdroid.fdroid.Utils.debugLog
import kotlin.math.min

private const val TAG = "Utils"

/**
 * Same as the Java function Utils.generateQrBitmap, but using coroutines instead of Single and Disposable.
 */
suspend fun generateQrBitmapKt(
    activity: AppCompatActivity,
    qrData: String,
): Bitmap = withContext(Dispatchers.Default) {
    val displayMode = DisplayCompat.getMode(activity, activity.windowManager.getDefaultDisplay())
    val qrCodeDimension = min(displayMode.physicalWidth, displayMode.physicalHeight)
    debugLog(TAG, "generating QRCode Bitmap of " + qrCodeDimension + "x" + qrCodeDimension)

    val encoder = QRCodeEncoder(
        qrData,
        null,
        Contents.Type.TEXT,
        BarcodeFormat.QR_CODE.toString(),
        qrCodeDimension,
    )
    return@withContext try {
        encoder.encodeAsBitmap()
    } catch (e: WriterException) {
        Log.e(TAG, "Could not encode QR as bitmap", e)
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }
}
