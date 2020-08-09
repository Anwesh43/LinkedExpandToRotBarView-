package com.anwesh.uiprojects.expandtorotbarview

/**
 * Created by anweshmishra on 10/08/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Color

val colors : Array<String> = arrayOf("#cc3214", "#21ddee", "#4556AB", "#AB2131", "#1234CC")
val parts : Int = 4
val scGap : Float = 0.02f
val sizeFactor : Float = 10.2f
val wFactor : Float = 2f
val gapFactor : Float = 4f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val rot : Float = 90f
