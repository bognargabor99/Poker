package hu.bme.aut.onlab.poker.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import hu.bme.aut.onlab.poker.R

class PokerCardView : View {
    var value: Int = 2
    var symbol: Int = SYMBOL_HEART
    private var paintText = Paint()
    private val paintBg = Paint()
    private lateinit var attributes: TypedArray

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (attrs == null) {
            return
        }

        paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        attributes = context.obtainStyledAttributes(attrs, R.styleable.PokerCardView)
        try {
            symbol = attributes.getInt(R.styleable.PokerCardView_symbol, SYMBOL_HEART)
            value = attributes.getInt(R.styleable.PokerCardView_value, 2)
        } finally {
            attributes.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        /*try {
            symbol = attributes.getInt(R.styleable.PokerCardView_symbol, SYMBOL_HEART)
            value = attributes.getInt(R.styleable.PokerCardView_value, 2)
        } finally {
            attributes.recycle()
        }*/
        paintBg.color = Color.WHITE
        paintBg.style = Paint.Style.FILL
        canvas.drawRoundRect(0F, 0F, width.toFloat(), height.toFloat(), 10.0F, 10.0F, paintBg)
        paintBg.color = Color.GRAY
        paintBg.strokeWidth = 2F
        paintBg.style = Paint.Style.STROKE
        canvas.drawRoundRect(0F, 0F, width.toFloat(), height.toFloat(), 10.0F, 10.0F, paintBg)
        drawValue(canvas)
        drawSymbol(canvas)
    }

    private fun drawValue(canvas: Canvas) {
        paintText.textSize = (height*3/7).toFloat()
        paintText.color = when (symbol) {
            SYMBOL_HEART -> redColor
            SYMBOL_DIAMOND -> redColor
            else -> Color.BLACK
        }
        canvas.drawText(getTextOfValue(), width.toFloat() * 0.1F, paintText.textSize+height.toFloat() * 0.01F, paintText)
        Log.d("pokerWebSocket", "value: $value")
    }

    private fun getTextOfValue(): String {
        if (value < 11)
            return value.toString()

        return when (value) {
            11 -> "J"
            12 -> "Q"
            13 -> "K"
            14 -> "A"
            else -> "2"
        }
    }

    private fun drawSymbol(canvas: Canvas) {
        val imageResource = when (symbol) {
            SYMBOL_HEART -> R.drawable.heart
            SYMBOL_SPADE -> R.drawable.spade
            SYMBOL_DIAMOND -> R.drawable.diamond
            SYMBOL_CLUB -> R.drawable.club
            else -> R.drawable.heart
        }
        val startX = (width*4/9).toFloat()
        val startY = (height*14/27).toFloat()
        val bitmapSymBol = BitmapFactory.decodeResource(resources, imageResource)
        canvas.drawBitmap(bitmapSymBol, null, RectF(startX, startY, width.toFloat() -15.0F, height.toFloat()-20.0F), null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(w, h)
    }

    companion object {
        private const val SYMBOL_HEART = 0
        private const val SYMBOL_SPADE = 1
        private const val SYMBOL_DIAMOND = 2
        private const val SYMBOL_CLUB = 3

        private val redColor = Color.rgb(180, 0, 0)
    }
}