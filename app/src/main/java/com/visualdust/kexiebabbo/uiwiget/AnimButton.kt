package com.visualdust.kexiebabbo.uiwiget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatButton
import com.visualdust.kexiebabbo.R

class AnimButton : AppCompatButton {
    private var wid = 0
    private var heigh = 0
    private var backDrawable: GradientDrawable? = null
    private var isMorphing = false
    private var startAngle = 0
    private var paint: Paint? = null
    private lateinit var arcValueAnimator: ValueAnimator

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        isMorphing = false
        backDrawable = GradientDrawable()
        val colorDrawable = context.getColor(R.color.colorPrimaryDark)
        backDrawable!!.setColor(colorDrawable)
        backDrawable!!.cornerRadius = 120f
        background = backDrawable
        text = "登陆"
        paint = Paint()
        paint!!.color = resources.getColor(R.color.colorPrimaryDark)
        paint!!.strokeWidth = 4f
        paint!!.style = Paint.Style.STROKE
        paint!!.textSize = 2f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heighMode = MeasureSpec.getMode(heightMeasureSpec)
        val heighSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode == MeasureSpec.EXACTLY) {
            wid = widthSize
        }
        if (heighMode == MeasureSpec.EXACTLY) {
            heigh = heighSize
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    fun startAnim() {
        isMorphing = true
        text = ""
        val valueAnimator = ValueAnimator.ofInt(wid, heigh)
        valueAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val leftOffset = (wid - value) / 2
            val rightOffset = wid - leftOffset
            backDrawable!!.setBounds(leftOffset, 0, rightOffset, heigh)
        }
        val objectAnimator =
            ObjectAnimator.ofFloat(backDrawable, "cornerRadius", 120f, heigh / 2.toFloat())
        val animatorSet = AnimatorSet()
        animatorSet.duration = 500
        animatorSet.playTogether(valueAnimator, objectAnimator)
        animatorSet.start()
        showArc()
    }

    fun actJump() {
        isMorphing = false
        arcValueAnimator!!.cancel()
        visibility = View.GONE
    }

    fun regainBackground() {
        visibility = View.VISIBLE
        backDrawable!!.setBounds(0, 0, wid, heigh)
        backDrawable!!.cornerRadius = 24f
        background = backDrawable
        text = "登陆"
        isMorphing = false
    }

    private fun showArc() {
        arcValueAnimator = ValueAnimator.ofInt(0, 1080)
        arcValueAnimator.addUpdateListener(AnimatorUpdateListener { animation ->
            startAngle = animation.animatedValue as Int
            invalidate()
        })
        arcValueAnimator.setInterpolator(LinearInterpolator())
        arcValueAnimator.setRepeatCount(ValueAnimator.INFINITE)
        arcValueAnimator.setDuration(3000)
        arcValueAnimator.start()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isMorphing == true) {
            val rectF = RectF(
                (getWidth() * 5 / 12).toFloat(),
                (height / 7).toFloat(),
                (getWidth() * 7 / 12).toFloat(),
                (height - height / 7).toFloat()
            )
            canvas.drawArc(rectF, startAngle.toFloat(), 270f, false, paint!!)
        }
    }
}