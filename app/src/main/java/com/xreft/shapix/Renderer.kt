package com.xreft.shapix
import android.graphics.*
import kotlin.math.*

const val W = 800

fun shapePath(t: ShapeType, r: Float) = Path().apply {
    when (t) {
        ShapeType.Circle -> addCircle(0f, 0f, r, Path.Direction.CW)
        ShapeType.Square -> addRect(-r, -r, r, r, Path.Direction.CW)
        ShapeType.Rounded -> addRoundRect(-r, -r, r, r, r * .26f, r * .26f, Path.Direction.CW)
        else -> {
            val n = when (t) { ShapeType.Triangle -> 3; ShapeType.Hexagon -> 6; else -> 10 }
            for (i in 0 until n) {
                val rr = if (t == ShapeType.Star && i % 2 == 1) r * .46f else r
                val a = (-PI / 2 + i * 2 * PI / n).toFloat()
                val px = cos(a) * rr; val py = sin(a) * rr
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
    }
}

fun fit(img: Bitmap, s: EditorState) =
    max(W / img.width.toFloat(), W / img.height.toFloat()) * s.zoom

fun clamp(s: EditorState, img: Bitmap): EditorState {
    val f = fit(img, s); val r = W * .5f * s.size
    val mx = max(0f, img.width * f / 2 - r); val my = max(0f, img.height * f / 2 - r)
    return s.copy(x = s.x.coerceIn(-mx, mx), y = s.y.coerceIn(-my, my))
}

fun Bitmap.fastBlur(r: Float): Bitmap {
    if (r <= 0f) return this
    val k = 1f / (1f + r)
    val small = Bitmap.createScaledBitmap(this, max(1, (width * k).toInt()), max(1, (height * k).toInt()), true)
    return Bitmap.createScaledBitmap(small, width, height, true)
}

/** src = الصورة بعد الضبابية، draw = طبقة الرسم 800x800 */
fun render(c: Canvas, s: EditorState, src: Bitmap, draw: Bitmap?) {
    val save = c.save()
    if (!s.transparent) c.drawColor(s.bg)
    c.translate(W / 2f, W / 2f)
    c.clipPath(shapePath(s.shape, W * .5f * s.size))
    c.translate(s.x, s.y)
    c.rotate(s.rotation)
    c.scale(if (s.flipX) -1f else 1f, if (s.flipY) -1f else 1f)
    val f = fit(src, s); val w = src.width * f; val h = src.height * f
    val p = Paint(Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(filterMatrix(s))
    }
    c.drawBitmap(src, null, RectF(-w / 2, -h / 2, w / 2, h / 2), p)
    c.restoreToCount(save)
    draw?.let { c.drawBitmap(it, 0f, 0f, null) }
}

fun export(s: EditorState, src: Bitmap, draw: Bitmap?, size: Int = W): Bitmap {
    val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val c = Canvas(out)
    val k = size / W.toFloat()
    c.scale(k, k)
    render(c, s, src, draw)
    return out
}

private fun filterMatrix(s: EditorState): ColorMatrix {
    val m = ColorMatrix()
    when (s.filter) {
        FilterType.Bw -> m.setSaturation(0f)
        FilterType.Sepia -> m.set(sepia(1f))
        FilterType.Invert -> m.set(floatArrayOf(-1f,0f,0f,0f,255f, 0f,-1f,0f,0f,255f, 0f,0f,-1f,0f,255f, 0f,0f,0f,1f,0f))
        FilterType.Vintage -> { m.set(sepia(.5f)); m.postConcat(ColorMatrix(hue(-8f))) }
        FilterType.Cool -> m.set(hue(180f))
        FilterType.Warm -> m.set(hue(-20f))
        FilterType.None -> {}
    }
    val b = s.brightness / 100f
    m.postConcat(ColorMatrix(floatArrayOf(b,0f,0f,0f,0f, 0f,b,0f,0f,0f, 0f,0f,b,0f,0f, 0f,0f,0f,1f,0f)))
    val c = s.contrast / 100f; val t = 128f * (1 - c)
    m.postConcat(ColorMatrix(floatArrayOf(c,0f,0f,0f,t, 0f,c,0f,0f,t, 0f,0f,c,0f,t, 0f,0f,0f,1f,0f)))
    m.postConcat(ColorMatrix().apply { setSaturation(s.saturate / 100f) })
    return m
}

private fun sepia(a: Float): FloatArray { val k = 1 - a; return floatArrayOf(
    .393f + .607f*k, .769f - .769f*k, .189f - .189f*k, 0f, 0f,
    .349f - .349f*k, .686f + .314f*k, .168f - .168f*k, 0f, 0f,
    .272f - .272f*k, .534f - .534f*k, .131f + .869f*k, 0f, 0f,
    0f, 0f, 0f, 1f, 0f) }

private fun hue(deg: Float): FloatArray {
    val r = Math.toRadians(deg.toDouble()); val c = cos(r).toFloat(); val s = sin(r).toFloat()
    return floatArrayOf(
        .213f + c*.787f - s*.213f, .715f - c*.715f - s*.715f, .072f - c*.072f + s*.928f, 0f, 0f,
        .213f - c*.213f + s*.143f, .715f + c*.285f + s*.140f, .072f - c*.072f - s*.283f, 0f, 0f,
        .213f - c*.213f - s*.787f, .715f - c*.715f + s*.715f, .072f + c*.928f + s*.072f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f)
}
