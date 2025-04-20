package com.sarrawi.mytranslate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.mlkit.vision.text.Text

class TextOverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val textBlocks = mutableListOf<Pair<Rect, String>>()
    private var onTextClickListener: ((String) -> Unit)? = null

    fun setBlocks(blocks: List<Text.TextBlock>) {
        textBlocks.clear()
        for (block in blocks) {
            block.boundingBox?.let { textBlocks.add(it to block.text) }
        }
        invalidate()
    }

    fun setOnTextClickListener(listener: (String) -> Unit) {
        onTextClickListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((rect, _) in textBlocks) {
            canvas.drawRect(rect, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            for ((rect, text) in textBlocks) {
                if (rect.contains(x, y)) {
                    onTextClickListener?.invoke(text)
                    break
                }
            }
        }
        return true
    }
}
