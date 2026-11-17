package com.xreft.shapix
import android.graphics.Color

enum class ShapeType { Circle, Triangle, Square, Rounded, Star, Hexagon }
enum class FilterType { None, Bw, Sepia, Invert, Vintage, Cool, Warm }

data class EditorState(
    val shape: ShapeType = ShapeType.Circle,
    val zoom: Float = 1f, val rotation: Float = 0f, val size: Float = .9f,
    val x: Float = 0f, val y: Float = 0f,
    val flipX: Boolean = false, val flipY: Boolean = false,
    val filter: FilterType = FilterType.None,
    val brightness: Float = 100f, val contrast: Float = 100f,
    val saturate: Float = 100f, val blur: Float = 0f,
    val transparent: Boolean = true, val bg: Int = Color.WHITE
)
