package com.taehokimmm.hapticvboard_android.layout.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class MultiTouchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
    onTap: (() -> Unit)? = null, onDoubleTap: (() -> Unit)? = null,
    onRightSwipe: (() -> Unit)? = null, onLeftSwipe: (() -> Unit)? = null
) : View(context, attrs, defStyleAttr) {

    private val touchPoints = mutableMapOf<Int, Pair<Float, Float>>()
    var onMultiTouchEvent: ((MotionEvent) -> Unit)? = null

    private val outOfBound = 1027
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Handle double-tap event
            if (onDoubleTap != null && e.y < outOfBound) {
                onDoubleTap()
            }
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // Handle double-tap event

            if (onTap != null && e.y < outOfBound) {
                onTap()
            }
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val SWIPE_THRESHOLD = 100
            val SWIPE_VELOCITY_THRESHOLD = 100
            if (e1 != null && e2 != null) {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            if (onRightSwipe != null) onRightSwipe()
                        } else {
                            if (onLeftSwipe != null) onLeftSwipe()
                        }
                        return true
                    }
                }
            }
            return false
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