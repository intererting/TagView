package com.yly.tagview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.widget.TextView

class ShapeTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    init {

        val cstmAttrs = context.obtainStyledAttributes(
            attrs,
            R.styleable.ShapeTextView,
            defStyleAttr,
            0
        )

        val normalDrawable = GradientDrawable()//普通状态
        val checkedDrawable = GradientDrawable()//选中状态
        //stroke and color
        if (cstmAttrs.hasValue(R.styleable.ShapeTextView_shape_common_stroke_width)) {
            val strokeWidth = cstmAttrs.getDimensionPixelSize(R.styleable.ShapeTextView_shape_common_stroke_width, 0)
            if (cstmAttrs.hasValue(R.styleable.ShapeTextView_shape_normal_stroke_color)) {
                val normalStrokeColor = cstmAttrs.getColor(R.styleable.ShapeTextView_shape_normal_stroke_color, -1)
                normalDrawable.setStroke(strokeWidth, normalStrokeColor)
            }
            if (cstmAttrs.hasValue(R.styleable.ShapeTextView_shape_checked_stroke_color)) {
                val checkedStrokeColor = cstmAttrs.getColor(R.styleable.ShapeTextView_shape_checked_stroke_color, -1)
                checkedDrawable.setStroke(strokeWidth, checkedStrokeColor)
            }
        }
        //bgColor
        if (cstmAttrs.hasValue(R.styleable.ShapeTextView_shape_normal_bg_color)) {
            val normalBgColor = cstmAttrs.getColor(R.styleable.ShapeTextView_shape_normal_bg_color, -1)
            normalDrawable.setColor(normalBgColor)
        }
        if (cstmAttrs.hasValue(R.styleable.ShapeTextView_shape_checked_bg_color)) {
            val checkedBgColor = cstmAttrs.getColor(R.styleable.ShapeTextView_shape_checked_bg_color, -1)
            checkedDrawable.setColor(checkedBgColor)
        }
        //radius
        if (cstmAttrs.hasValue(R.styleable.ShapeTextView_shape_normal_radius)) {
            val normalRadius = cstmAttrs.getDimension(R.styleable.ShapeTextView_shape_normal_radius, 0f)
            normalDrawable.cornerRadius = normalRadius
        }
        if (cstmAttrs.hasValue(R.styleable.ShapeTextView_shape_checked_radius)) {
            val checkedRadius = cstmAttrs.getDimension(R.styleable.ShapeTextView_shape_checked_radius, 0f)
            checkedDrawable.cornerRadius = checkedRadius
        }
        //textcolor
        if (cstmAttrs.hasValue(R.styleable.ShapeTextView_shape_normal_text_color) &&
            cstmAttrs.hasValue(R.styleable.ShapeTextView_shape_checked_text_color)
        ) {
            val normalTextColor = cstmAttrs.getColor(R.styleable.ShapeTextView_shape_normal_text_color, -1)
            val checkedTextColor = cstmAttrs.getColor(R.styleable.ShapeTextView_shape_checked_text_color, -1)
            val stateTextColor = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(checkedTextColor, normalTextColor)
            )
            setTextColor(stateTextColor)
        }
        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
        stateListDrawable.addState(intArrayOf(), normalDrawable)
        background = stateListDrawable
        cstmAttrs.recycle()
    }
}