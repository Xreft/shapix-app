@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.xreft.shapix

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Canvas as AndroidCanvas
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

val BG = Color(0xFF0B1020)
val PANEL = Color(0xFF151E31)
val TEAL = Color(0xFF66DFC7)
val MUTED = Color(0xFF9CAEC5)
val TEXT = Color(0xFFF3F4F6)

private val PALETTE = listOf(
    0xFFFF3B3B, 0xFFFFB020, 0xFFFFE14D, 0xFF4DD67A,
    0xFF39C7FF, 0xFF8C6BFF, 0xFFFFFFFF, 0xFF111111
)
private val SHAPES = listOf(
    Triple(ShapeType.Circle, "●", "shapeCircle"), Triple(ShapeType.Triangle, "▲", "shapeTriangle"),
    Triple(ShapeType.Square, "■", "shapeSquare"), Triple(ShapeType.Rounded, "▢", "shapeRounded"),
    Triple(ShapeType.Star, "★", "shapeStar"), Triple(ShapeType.Hexagon, "⬡", "shapeHexagon")
)
private val FILTERS = listOf(
    FilterType.None to "filterNone", FilterType.Bw to "filterBw", FilterType.Sepia to "filterSepia",
    FilterType.Invert to "filterInvert", FilterType.Vintage to "filterVintage",
    FilterType.Cool to "filterCool", FilterType.Warm to "filterWarm"
)

@Composable
fun EditorScreen(lang: Lang, onLang: (Lang) -> Unit) {
    val ctx = LocalContext.current
    fun t(k: String) = tr(lang, k)

    var s by remember { mutableStateOf(EditorState()) }
    var original by remember { mutableStateOf<Bitmap?>(null) }
    var fileName by remember { mutableStateOf("image") }
    var statusKey by remember { mutableStateOf("") }
    var drawMode by remember { mutableStateOf(false) }
    var drawColor by remember { mutableStateOf(PALETTE[0].toInt()) }
    var brushSize by remember { mutableFloatStateOf(8f) }
    var tick by remember { mutableIntStateOf(0) }
    var exportSize by remember { mutableIntStateOf(800) }
    var exportFmt by remember { mutableStateOf(ExportFormat.Png) }
    var saveCount by remember { mutableIntStateOf(0) }

    val drawBmp = remember { Bitmap.createBitmap(W, W, Bitmap.Config.ARGB_8888) }
    val drawCanvas = remember { AndroidCanvas(drawBmp) }
    val undo = remember { ArrayDeque<Bitmap>() }
    val brush = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
        }
    }
    val src = remember(original, s.blur) { original?.fastBlur(s.blur) }

    fun say(key: String) {
        statusKey = key
        Toast.makeText(ctx, tr(lang, key), Toast.LENGTH_SHORT).show()
    }
    fun upd(n: EditorState) { s = original?.let { clamp(n, it) } ?: n }
    fun pushUndo() {
        undo.addLast(drawBmp.copy(Bitmap.Config.ARGB_8888, false))
        if (undo.size > 20) undo.removeFirst()
    }
    fun doUndo() {
        undo.removeLastOrNull()?.let {
            drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR)
            drawCanvas.drawBitmap(it, 0f, 0f, null)
            tick++
        }
    }
    fun clearDraw() { drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR); undo.clear(); tick++ }
    fun resetAll() {
        s = s.copy(
            zoom = 1f, rotation = 0f, size = .9f, x = 0f, y = 0f, flipX = false, flipY = false,
            filter = FilterType.None, brightness = 100f, contrast = 100f, saturate = 100f, blur = 0f
        )
        clearDraw(); say("msgReset")
    }
    fun doSave(share: Boolean) {
        val b = src
        if (b == null) { say("msgExportFail"); return }
        val st = if (exportFmt == ExportFormat.Jpeg && s.transparent)
            s.copy(transparent = false, bg = 0xFFFFFFFF.toInt()) else s
        val uri = ImageIO.save(ctx, export(st, b, drawBmp, exportSize), fileName, exportFmt)
        if (uri == null) { say("msgExportFail"); return }
        if (share) {
            ImageIO.share(ctx, uri, exportFmt.mime)
        } else {
            say("msgDownloaded")
            saveCount++
            if (saveCount % 2 == 0) Ads.showInterstitialIfReady()
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) when (val r = ImageIO.load(ctx, uri)) {
            is ImageIO.Load.Ok -> {
                original = r.bmp; fileName = r.name
                s = EditorState(shape = s.shape, transparent = s.transparent, bg = s.bg)
                clearDraw(); say("msgUploaded")
            }
            is ImageIO.Load.Err -> say(r.key)
        }
    }

    Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("✂️ ${t("appName")} | ${t("titleSub")}", fontSize = 18.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            LangMenu(lang, onLang)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier.align(Alignment.CenterHorizontally).size(280.dp)
                .clip(RoundedCornerShape(16.dp)).background(Color(0xFF202A3D)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                Modifier.fillMaxSize().pointerInput(drawMode, original) {
                    if (original == null) return@pointerInput
                    val k = W / size.width.toFloat()
                    if (!drawMode) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val cx = centroid.x * k - W / 2f
                            val cy = centroid.y * k - W / 2f
                            val nz = (s.zoom * zoom).coerceIn(0.5f, 3f)
                            val r = nz / s.zoom
                            upd(s.copy(
                                zoom = nz,
                                x = cx + (s.x - cx) * r + pan.x * k,
                                y = cy + (s.y - cy) * r + pan.y * k
                            ))
                        }
                    } else {
                        var last = Offset.Zero
                        detectDragGestures(
                            onDragStart = { o ->
                                pushUndo()
                                last = o * k
                                brush.color = drawColor; brush.strokeWidth = brushSize
                                drawCanvas.drawPoint(last.x, last.y, brush)
                                tick++
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val p = change.position * k
                                brush.color = drawColor; brush.strokeWidth = brushSize
                                drawCanvas.drawLine(last.x, last.y, p.x, p.y, brush)
                                last = p; tick++
                            }
                        )
                    }
                }
            ) {
                val redraw = tick
                val b = src
                if (b != null) drawIntoCanvas { c ->
                    val nc = c.nativeCanvas
                    val k = size.width / W
                    nc.save(); nc.scale(k, k)
                    render(nc, s, b, drawBmp)
                    nc.restore()
                }
            }
            if (original == null) Text(t("emptyText"), color = MUTED, modifier = Modifier.padding(20.dp))
        }
        Text(t("previewHint"), color = MUTED, fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp))
        Spacer(Modifier.height(6.dp))
        Button(
            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("🖼️ ${t("dropTitle")}") }
        if (statusKey.isNotEmpty()) Text(t(statusKey), color = TEAL, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Panel {
                Text(t("toolsHeading"), fontWeight = FontWeight.Bold)
                Section(t("shapeHeading")) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SHAPES.forEach { (sh, g, key) -> Chip(s.shape == sh, "$g ${t(key)}") { upd(s.copy(shape = sh)) } }
                    }
                }
                Sl(t("rotationLabel"), "${s.rotation.roundToInt()}°", s.rotation, -180f..180f) { upd(s.copy(rotation = it)) }
                Sl(t("sizeLabel"), "${(s.size * 100).roundToInt()}%", s.size * 100, 20f..100f) { upd(s.copy(size = it / 100f)) }

                Section(t("flipHeading")) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { upd(s.copy(flipX = !s.flipX)) }, modifier = Modifier.weight(1f)) { Text("⇋ ${t("flipH")}") }
                        OutlinedButton(onClick = { upd(s.copy(flipY = !s.flipY)) }, modifier = Modifier.weight(1f)) { Text("⇵ ${t("flipV")}") }
                    }
                }
                Section(t("filtersHeading")) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FILTERS.forEach { (f, key) -> Chip(s.filter == f, t(key)) { upd(s.copy(filter = f)) } }
                    }
                }
                Sl(t("brightnessLabel"), "${s.brightness.roundToInt()}%", s.brightness, 40f..180f) { upd(s.copy(brightness = it)) }
                Sl(t("contrastLabel"), "${s.contrast.roundToInt()}%", s.contrast, 40f..180f) { upd(s.copy(contrast = it)) }
                Sl(t("saturateLabel"), "${s.saturate.roundToInt()}%", s.saturate, 0f..220f) { upd(s.copy(saturate = it)) }
                Sl(t("blurLabel"), "${s.blur}px", s.blur, 0f..10f) { upd(s.copy(blur = (it * 2).roundToInt() / 2f)) }

                Section(t("modeHeading")) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip(!drawMode, t("modeMove")) { drawMode = false }
                        Chip(drawMode, t("modeDraw")) { drawMode = true }
                    }
                    if (drawMode) {
                        Spacer(Modifier.height(10.dp))
                        Text(t("colorLabel"), color = MUTED, fontSize = 13.sp)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PALETTE.forEach { c -> Swatch(c, drawColor == c.toInt()) { drawColor = c.toInt() } }
                        }
                        Sl(t("brushLabel"), "${brushSize.roundToInt()}", brushSize, 2f..40f) { brushSize = it }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { doUndo() }, modifier = Modifier.weight(1f)) { Text(t("undoBtn")) }
                            OutlinedButton(
                                onClick = { clearDraw(); say("msgDrawCleared") },
                                modifier = Modifier.weight(1f)
                            ) { Text(t("clearBtn")) }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = s.transparent, onCheckedChange = { s = s.copy(transparent = it) })
                    Text(t("transparentLabel"), fontSize = 14.sp)
                }
                if (!s.transparent) {
                    Text(t("bgColorLabel"), color = MUTED, fontSize = 13.sp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PALETTE.forEach { c -> Swatch(c, s.bg == c.toInt()) { s = s.copy(bg = c.toInt()) } }
                    }
                }

                Section(t("exportHeading")) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(512, 800, 1080).forEach { n ->
                            Chip(exportSize == n, "$n×$n") { exportSize = n }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip(exportFmt == ExportFormat.Png, "PNG") { exportFmt = ExportFormat.Png }
                        Chip(exportFmt == ExportFormat.Jpeg, "JPG") { exportFmt = ExportFormat.Jpeg }
                    }
                    Text(t("exportHint"), color = MUTED, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                    if (exportFmt == ExportFormat.Jpeg && s.transparent) {
                        Text(t("jpgNote"), color = MUTED, fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { doSave(false) },
                        enabled = original != null, modifier = Modifier.weight(1f)
                    ) { Text(t("downloadBtn")) }
                    OutlinedButton(
                        onClick = { doSave(true) },
                        enabled = original != null, modifier = Modifier.weight(1f)
                    ) { Text(t("shareBtn")) }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { resetAll() },
                    enabled = original != null, modifier = Modifier.fillMaxWidth()
                ) { Text(t("resetBtn")) }
            }

            Text(t("bottomText"), color = MUTED, fontSize = 12.sp, lineHeight = 20.sp)
            Text(t("footerText"), color = MUTED, fontSize = 11.sp, modifier = Modifier.fillMaxWidth())
        }

        BannerAd(Ads.BANNER_ID)
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(PANEL).padding(16.dp), content = content)

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 14.dp)) {
        Text(title, color = MUTED, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun Chip(selected: Boolean, label: String, onClick: () -> Unit) = FilterChip(
    selected = selected, onClick = onClick, label = { Text(label, fontSize = 12.sp) },
    colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = Color(0xFF17534E), selectedLabelColor = Color(0xFFBFFFEE)
    )
)

@Composable
private fun Swatch(color: Long, selected: Boolean, onClick: () -> Unit) = Box(
    Modifier.size(30.dp).clip(CircleShape).background(Color(color))
        .border(if (selected) 3.dp else 1.dp, if (selected) Color.White else Color(0xFF30415B), CircleShape)
        .clickable(onClick = onClick)
)

@Composable
private fun Sl(label: String, text: String, v: Float, r: ClosedFloatingPointRange<Float>, on: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 14.sp)
            Text(text, color = TEAL, fontSize = 14.sp, style = TextStyle(textDirection = TextDirection.Ltr))
        }
        Slider(
            value = v, onValueChange = on, valueRange = r,
            colors = SliderDefaults.colors(thumbColor = TEAL, activeTrackColor = TEAL)
        )
    }
}

@Composable
private fun LangMenu(lang: Lang, onLang: (Lang) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) { Text("🌐 ${lang.label}") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            Lang.values().forEach { l ->
                DropdownMenuItem(text = { Text(l.label) }, onClick = { onLang(l); open = false })
            }
        }
    }
}
