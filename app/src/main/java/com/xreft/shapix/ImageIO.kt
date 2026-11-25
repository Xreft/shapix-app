package com.xreft.shapix

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import kotlin.math.max

enum class ExportFormat(val mime: String, val ext: String) {
    Png("image/png", "png"),
    Jpeg("image/jpeg", "jpg")
}

object ImageIO {
    sealed class Load {
        class Ok(val bmp: Bitmap, val name: String) : Load()
        class Err(val key: String) : Load()
    }

    private const val MAX_BYTES = 20L * 1024 * 1024

    fun load(ctx: Context, uri: Uri): Load {
        try {
            val cr = ctx.contentResolver
            if (cr.getType(uri)?.startsWith("image/") != true) return Load.Err("msgInvalidFile")

            var size = -1L
            var name = "image"
            cr.query(uri, arrayOf(OpenableColumns.SIZE, OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val si = c.getColumnIndex(OpenableColumns.SIZE)
                    val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (si >= 0 && !c.isNull(si)) size = c.getLong(si)
                    if (ni >= 0) c.getString(ni)?.let { name = it.substringBeforeLast('.') }
                }
            }
            if (size > MAX_BYTES) return Load.Err("msgTooBig")

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0) return Load.Err("msgLoadFail")

            var sample = 1
            while (max(bounds.outWidth, bounds.outHeight) / sample > 2400) sample *= 2
            var bmp = cr.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return Load.Err("msgLoadFail")

            val orient = try {
                cr.openInputStream(uri)?.use {
                    ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } ?: 1
            } catch (e: Exception) { 1 }
            val deg = when (orient) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (deg != 0f) {
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(deg) }, true)
            }
            return Load.Ok(bmp, name)
        } catch (e: Exception) {
            return Load.Err("msgLoadFail")
        } catch (e: OutOfMemoryError) {
            return Load.Err("msgLoadFail")
        }
    }

    fun save(ctx: Context, bmp: Bitmap, name: String, fmt: ExportFormat): Uri? {
        return try {
            val cr = ctx.contentResolver
            val v = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name-shapix-${System.currentTimeMillis()}.${fmt.ext}")
                put(MediaStore.Images.Media.MIME_TYPE, fmt.mime)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Shapix")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v)!!
            cr.openOutputStream(uri)!!.use {
                if (fmt == ExportFormat.Png) bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
                else bmp.compress(Bitmap.CompressFormat.JPEG, 92, it)
            }
            v.clear()
            v.put(MediaStore.Images.Media.IS_PENDING, 0)
            cr.update(uri, v, null, null)
            uri
        } catch (e: Exception) {
            null
        }
    }

    fun share(ctx: Context, uri: Uri, mime: String) {
        try {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            ctx.startActivity(Intent.createChooser(send, null))
        } catch (e: Exception) {
        }
    }
}
