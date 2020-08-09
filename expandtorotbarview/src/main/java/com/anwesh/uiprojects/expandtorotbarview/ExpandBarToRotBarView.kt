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

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawExpandBarToRot(scale : Float, w : Float, h : Float, paint : Paint) {
    val wSize : Float = Math.min(w, h) / wFactor
    val size : Float = Math.min(w, h) / sizeFactor
    val gap : Float = Math.min(w, h) / gapFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts + 1)
    val sf2 : Float = sf.divideScale(1, parts + 1)
    val sf3 : Float = sf.divideScale(2, parts + 1)
    val sf4 : Float = sf.divideScale(3, parts + 1)
    val dw : Float = wSize * sf1 + (size - wSize ) * sf3
    val dh : Float = wSize * sf1
    save()
    translate(w / 2, h / 2)
    rotate(rot * sf4)
    for (j in 0..1) {
        save()
        scale(1f, 1f - 2 * j)
        translate(0f, gap * sf2)
        drawRect(RectF(-dw / 2, -dh / 2, dw / 2, dh / 2), paint)
        restore()
    }
    restore()
}

fun Canvas.drawEBTRNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = Color.parseColor(colors[i])
    drawExpandBarToRot(scale, w, h, paint)
}

class ExpandBarToRotBarView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }


    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class EBTRNode(var i : Int, val state : State = State()) {

        private var next : EBTRNode? = null
        private var prev : EBTRNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = EBTRNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawEBTRNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : EBTRNode {
            var curr : EBTRNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class ExpandBarToRot(var i : Int) {

        private var curr : EBTRNode = EBTRNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : ExpandBarToRotBarView) {

        private val animator : Animator = Animator(view)
        private val ebtr : ExpandBarToRot = ExpandBarToRot(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            ebtr.draw(canvas,paint)
            animator.animate {
                ebtr.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ebtr.startUpdating {
                animator.start()
            }
        }
    }
}