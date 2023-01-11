package com.app.signage91.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import java.lang.ref.WeakReference

private const val MARQUEE_DELAY = 1200
private const val MARQUEE_STOPPED: Byte = 0x0
private const val MARQUEE_STARTING: Byte = 0x1
private const val MARQUEE_RUNNING: Byte = 0x2
private const val MARQUEE_CONSTANT = 1.2f

class TextViewComponent : AppCompatTextView {
    private var mPaint: Paint? = null
    private var mText: CharSequence? = null
    private val currentGravity = (Gravity.CENTER_VERTICAL or Gravity.LEFT)
    private var mCurrentX = 0f
    private var mCurrentY = 0f
    private var firstScrollX = 0f
    private var firstScrollY = 0f
    private var endX = 0f
    private var endY = 0f
    private var mViewWidth = 0f
    private var mViewHeight = 0f
    private var mTextWidth = 0f
    private var mTextHeight = 0f
    private var isFirstPaint = true
    private var mMarquee: Marquee? = null
    private var mSroll = 0f
    private var speedMultiple = 1.0f
    private var mMarqueeRepeatLimit = 1000
    private var mDirection: Direction? = Direction.LEFT

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    private fun init() {
        mText = this.text
        mPaint = this.paint
        mDirection = Direction.LEFT
        if (mDirection == Direction.LEFT || mDirection == Direction.RIGHT) {
            isSingleLine = true
            gravity = currentGravity
        } else {
            isSingleLine = false
            gravity = Gravity.CENTER
        }
        //mMarqueeRepeatLimit = this.marqueeRepeatLimit
        val density = this.context.resources.displayMetrics.density
        mSroll = 1.50f / 3.0f * density * speedMultiple
        mMarquee = Marquee(this)
        mMarquee?.setRepeatLimit(mMarqueeRepeatLimit)
        mMarquee?.marquee_delay = MARQUEE_DELAY
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        isFirstPaint()
        calculateCurrentXOrY()
    }

    private fun isFirstPaint() {
        if (isFirstPaint) {
            isFirstPaint = false
            textWidthAndHeight()
            startMarquee()
        }
    }

    private fun calculateCurrentXOrY() {
        when (mDirection) {
            Direction.LEFT -> mCurrentX =
                (mCurrentX + mSroll)
            Direction.RIGHT -> mCurrentX =
                (mCurrentX - mSroll)
            Direction.UP -> mCurrentY = (mCurrentY + mSroll)
            Direction.DOWN -> mCurrentY =
                (mCurrentY - mSroll)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewWidth = this.measuredWidth.toFloat()
        mViewHeight = this.measuredHeight.toFloat()
        firstScrollX = scrollX.toFloat()
        firstScrollY = scrollY.toFloat()
        mCurrentX = firstScrollX
        mCurrentY = firstScrollY
        textWidthAndHeight()
    }

    private fun textWidthAndHeight() {
        if (null != mText && mText!!.length > 0) {
            val metrics = mPaint!!.fontMetrics
            mTextWidth = mPaint!!.measureText(mText, 0, mText!!.length)
            mTextHeight = this.textSize - metrics.descent
            mTextHeight = mTextWidth / 2 - metrics.descent
            restScrollRelatedVariables()
        }
    }


    /*private fun restScrollRelatedVariables() {
        when (mDirection) {
            Direction.LEFT -> endX = firstScrollX + mTextWidth
            Direction.RIGHT -> endX = firstScrollX + mViewWidth
            Direction.UP -> endY = firstScrollY + mTextHeight
            Direction.DOWN -> endY = firstScrollY + mViewHeight
        }
    }*/

    private fun restScrollRelatedVariables() {
        when (mDirection) {
            Direction.LEFT -> endX = firstScrollX + mTextWidth
            Direction.RIGHT -> endX = firstScrollX + mViewWidth
            Direction.UP -> endY = firstScrollY + mTextHeight / 2 + mViewHeight / 2
            Direction.DOWN -> endY = firstScrollY + mTextHeight / 2 + mViewHeight / 2
        }
    }

    enum class Direction {
        LEFT, RIGHT, UP, DOWN
    }

    private inner class Marquee internal constructor(v: TextViewComponent) : Thread() {

        private val mView: WeakReference<TextViewComponent>
        var marquee_delay = -1
        private var mRepeatLimit = 0
        private var mStatus = MARQUEE_STOPPED
        val isStoped: Boolean
            get() = if (mStatus == MARQUEE_STOPPED) {
                true
            } else false

        override fun run() {
            val marqueeTextView = mView.get()
            if (mRepeatLimit == 0) {
                returnToOriginaPositon(marqueeTextView)
                stopMarquee()
                return
            }
            if (mStatus == MARQUEE_STOPPED) {
                stopMarquee()
                return
            }
            if (null != marqueeTextView) {
                mStatus = MARQUEE_RUNNING
                textScrollTo(marqueeTextView)
                when (mDirection) {
                    Direction.LEFT -> {
//                        Log.i("MarqueeTextView", "mCurrentX = $mCurrentX")
//                        Log.i("MarqueeTextView", "endX = $endX")
                        if (mCurrentX >= endX) { // 0+
                            marqueeTextView.removeCallbacks(this)
                            if (mStatus == MARQUEE_RUNNING) {
                                if (mRepeatLimit >= 0) {
                                    mRepeatLimit--
                                }
                                mCurrentX = firstScrollX - mViewWidth
                                post(mMarquee)
                            }
                        }
                    }
                    Direction.RIGHT -> {
//                        Log.i("MarqueeTextView", "mCurrentX = $mCurrentX")
//                        Log.i("MarqueeTextView", "endX = $endX")
                        if (-mCurrentX >= endX) { // 0-
                            marqueeTextView.removeCallbacks(this)
                            if (mStatus == MARQUEE_RUNNING) {
                                if (mRepeatLimit >= 0) {
                                    mRepeatLimit--
                                }
                                mCurrentX = firstScrollX + mTextWidth
                                post(mMarquee)
                            }
                        }
                    }
                    Direction.UP -> {
//                        Log.i("MarqueeTextView", "mCurrentY = $mCurrentY")
//                        Log.i("MarqueeTextView", "endY = $endY")
                        if (mCurrentY >= endY) { // 0+
                            marqueeTextView.removeCallbacks(this)
                            if (mStatus == MARQUEE_RUNNING) {
                                if (mRepeatLimit >= 0) {
                                    mRepeatLimit--
                                }
                                //mCurrentY = firstScrollY - mTextHeight / 2 - mViewHeight / 2
                                mCurrentY = firstScrollY - mTextHeight * 2 - mViewHeight * 2
                                post(mMarquee)
                            }
                        }
                    }
                    Direction.DOWN -> {
                        /*Log.i("MarqueeTextView", "mCurrentY = $mCurrentY")
                        Log.i("MarqueeTextView", "endY = $endY")*/
                        if (-mCurrentY >= endY) { // 0-
                            marqueeTextView.removeCallbacks(this)
                            if (mStatus == MARQUEE_RUNNING) {
                                if (mRepeatLimit >= 0) {
                                    mRepeatLimit--
                                }
                                //mCurrentY = firstScrollY + mTextHeight / 2 + mViewHeight / 2
                                mCurrentY = firstScrollY + mTextHeight * 2 + mViewHeight * 2
                                post(mMarquee)
                            }
                        }
                    }
                }
            }
        }

        private fun returnToOriginaPositon(textViewComponent: TextViewComponent?) {
            when (mDirection) {
                Direction.LEFT -> textViewComponent!!.scrollTo(firstScrollX.toInt(), 0)
                Direction.RIGHT -> textViewComponent!!.scrollTo(firstScrollX.toInt(), 0)
                Direction.UP -> textViewComponent!!.scrollTo(0, firstScrollY.toInt())
                Direction.DOWN -> textViewComponent!!.scrollTo(0, firstScrollY.toInt())
            }
            textViewComponent!!.invalidate()
            post(this)
        }

        private fun textScrollTo(textViewComponent: TextViewComponent) {
            when (mDirection) {
                Direction.LEFT -> textViewComponent.scrollTo(mCurrentX.toInt(), 0)
                Direction.RIGHT -> textViewComponent.scrollTo(mCurrentX.toInt(), 0)
                Direction.UP -> textViewComponent.scrollTo(0, mCurrentY.toInt())
                Direction.DOWN -> textViewComponent.scrollTo(0, mCurrentY.toInt())
            }
            textViewComponent.invalidate()
            post(this)
        }

        fun getmStatus(): Byte {
            return mStatus
        }

        fun setmStatus(mStatus: Byte) {
            this.mStatus = mStatus
        }

        fun setRepeatLimit(repeatLimit: Int) {
            mRepeatLimit = repeatLimit
        }

        init {
            mView = WeakReference(v)
            marquee_delay = MARQUEE_DELAY
        }
    }

    fun stopMarquee() {
        mMarquee!!.setmStatus(MARQUEE_STOPPED)
        removeCallbacks(mMarquee)
    }


    private fun startMarquee() {
        if (mMarquee!!.getmStatus() == MARQUEE_STOPPED) {
            mMarquee!!.setmStatus(MARQUEE_STARTING)
            postDelayed(mMarquee, mMarquee!!.marquee_delay.toLong())
        }
    }

    fun setDelayed(msecDelay: Int) {
        if (msecDelay >= 0) {
            mMarquee!!.marquee_delay = msecDelay
        }
    }

    fun setSpeed(speedMultiple: Float) {
        if (speedMultiple < 0) {
            return
        }
        this.speedMultiple = speedMultiple
        val density = this.context.resources.displayMetrics.density
        mSroll = 1.40f / 3.0f * density * this.speedMultiple
    }

    fun setDirection(direction: Direction?) {
        mDirection = direction
        restScrollRelatedVariables()
        if (mDirection == Direction.LEFT || mDirection == Direction.RIGHT) {
            isSingleLine = true
            gravity = currentGravity
        } else {
            isSingleLine = false
            gravity = Gravity.CENTER
        }
    }

    override fun onDetachedFromWindow() {
        if (null != mMarquee) {
            removeCallbacks(mMarquee)
            mMarquee!!.interrupt()
        }
        super.onDetachedFromWindow()
    }

    override fun onTextChanged(
        text: CharSequence, start: Int,
        lengthBefore: Int, lengthAfter: Int
    ) {
        removeCallbacks(mMarquee)
        if (null != mDirection) {
            when (mDirection) {
                Direction.LEFT -> {
                    mCurrentX = firstScrollX
                    scrollTo(mCurrentX.toInt(), 0)
                }
                Direction.RIGHT -> {
                    mCurrentX = firstScrollX
                    scrollTo(mCurrentX.toInt(), 0)
                }
                Direction.UP -> {
                    mCurrentY = firstScrollY
                    scrollTo(0, mCurrentY.toInt())
                }
                Direction.DOWN -> {
                    mCurrentY = firstScrollY
                    scrollTo(0, mCurrentY.toInt())
                }
            }
            this.invalidate()
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        isFirstPaint = true
        mText = text
        if (null != mMarquee) {
            postDelayed(mMarquee, mMarquee!!.marquee_delay.toLong())
        } else {
            postDelayed(mMarquee, MARQUEE_DELAY.toLong())
        }
    }
}
