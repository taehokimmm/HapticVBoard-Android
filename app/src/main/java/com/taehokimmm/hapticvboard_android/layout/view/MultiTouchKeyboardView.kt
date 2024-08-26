package com.taehokimmm.hapticvboard_android.layout.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class MultiTouchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
    onTap: (() -> Unit)? = null, onDoubleTap: (() -> Unit)? = null
) : View(context, attrs, defStyleAttr) {

    private val touchPoints = mutableMapOf<Int, Pair<Float, Float>>()
    var onMultiTouchEvent: ((MotionEvent) -> Unit)? = null

    private val outOfBound = 1533
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Handle double-tap event
            Log.d("touchevent", "on double tap" + e.y)
            if (onDoubleTap != null && e.y < outOfBound) {
                onDoubleTap()
            }
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // Handle double-tap event
            Log.d("touchevent", "on tap" + e.y)

            if (onTap != null && e.y < outOfBound) {
                onTap()
            }
            return true
        }

        // You can override other gesture events here if needed
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
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