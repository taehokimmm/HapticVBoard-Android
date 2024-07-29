package com.taehokimmm.hapticvboard_android.layout.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class MultiTouchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val touchPoints = mutableMapOf<Int, Pair<Float, Float>>()
    var onMultiTouchEvent: ((MotionEvent) -> Unit)? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        onMultiTouchEvent?.invoke(event)
        val pointerCount = event.pointerCount
        for (i in 0 until pointerCount) {
            val pointerId = event.getPointerId(i)
            val x = event.getX(i)
            val y = event.getY(i)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    touchPoints[pointerId] = Pair(x, y)
                }
                MotionEvent.ACTION_MOVE -> {
                    touchPoints[pointerId] = Pair(x, y)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    touchPoints.remove(pointerId)
                }
            }
        }
        invalidate()
        return true
    }
}