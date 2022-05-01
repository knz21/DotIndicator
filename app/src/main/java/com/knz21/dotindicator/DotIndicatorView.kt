package com.knz21.dotindicator

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible

class DotIndicatorView : View {

    constructor(context: Context) : super(context) {
        setup(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setup(context, attrs)
    }

    companion object {

        private const val DEFAULT_BASE_VISIBLE_DOT_COUNT = 6

        private const val ANIMATION_DURATION = 200L
    }

    private var normalSize: Int = 0

    private val normalRadius: Float by lazy { normalSize / 2f }

    private var smallSize: Int = 0

    private val smallRadius: Float by lazy { smallSize / 2f }

    private var margin: Int = 0

    private val activePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val inactivePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var xCoordinate: Float = 0f

    private val yCoordinate: Float by lazy { normalRadius }

    private var baseVisibleDotCount = DEFAULT_BASE_VISIBLE_DOT_COUNT

    private var itemCount: Int = 0

    private var bias: Bias = Bias.Right

    private var dotPosition: Int = 0

    private var lastItemPosition: Int = 0

    private var animator: ValueAnimator? = null

    private var animationType: AnimationType = AnimationType.RightToCenter

    private var animationValue: Float = 0f

    private val maxCenterPosition: Int get() = baseVisibleDotCount - 2

    private val areAllItemsShown: Boolean get() = baseVisibleDotCount >= itemCount

    private fun setup(context: Context, attributeSet: AttributeSet?) {
        if (attributeSet != null) {
            val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DotIndicatorView)
            activePaint.color = typedArray.getColor(
                R.styleable.DotIndicatorView_active_dot_color,
                ResourcesCompat.getColor(resources, R.color.default_active_dot, null)
            )
            inactivePaint.color = typedArray.getColor(
                R.styleable.DotIndicatorView_inactive_dot_color,
                ResourcesCompat.getColor(resources, R.color.default_inactive_dot, null)
            )
            normalSize = typedArray.getDimensionPixelSize(
                R.styleable.DotIndicatorView_normal_dot_size,
                resources.getDimensionPixelSize(R.dimen.default_normal_size)
            )
            smallSize = typedArray.getDimensionPixelSize(
                R.styleable.DotIndicatorView_small_dot_size,
                resources.getDimensionPixelSize(R.dimen.default_small_size)
            )
            margin = typedArray.getDimensionPixelSize(
                R.styleable.DotIndicatorView_dot_margin,
                resources.getDimensionPixelSize(R.dimen.default_margin)
            )
            baseVisibleDotCount = typedArray.getInteger(
                R.styleable.DotIndicatorView_base_visible_dot_count,
                DEFAULT_BASE_VISIBLE_DOT_COUNT
            )
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = if (areAllItemsShown) {
            normalSize * itemCount + margin * (itemCount - 1)
        } else {
            normalSize * (baseVisibleDotCount - 1) + smallSize * 2 + margin * (baseVisibleDotCount + 2)
        }
        val height = normalSize
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        when {
            areAllItemsShown -> drawSimpleCircles(canvas)
            animator != null -> drawCirclesWhileAnimation(canvas)
            else -> drawCirclesAlongWithBias(canvas)
        }
    }

    private fun drawSimpleCircles(canvas: Canvas) {
        xCoordinate = 0f
        repeat(itemCount) {
            drawCircleAndShiftXCoordinate(canvas, normalRadius, it == dotPosition)
        }
    }

    private fun drawCirclesWhileAnimation(canvas: Canvas) {
        when (animationType) {
            AnimationType.RightToCenter -> {
                xCoordinate = (smallSize + margin) * (1 - animationValue) + margin
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(normalRadius, smallRadius))
                repeat(baseVisibleDotCount - 2) {
                    drawCircleAndShiftXCoordinate(canvas, normalRadius)
                }
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(smallRadius, normalRadius), true)
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(0f, smallRadius))
            }
            AnimationType.CenterToCenterLeft -> {
                xCoordinate = margin * (1 - animationValue)
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(smallRadius, 0f))
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(normalRadius, smallRadius))
                repeat(baseVisibleDotCount - 2) {
                    drawCircleAndShiftXCoordinate(canvas, normalRadius)
                }
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(smallRadius, normalRadius), true)
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(0f, smallRadius))
            }
            AnimationType.CenterToLeft -> {
                xCoordinate = margin * (1 - animationValue)
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(smallRadius, 0f))
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(normalRadius, smallRadius))
                repeat(baseVisibleDotCount - 2) {
                    drawCircleAndShiftXCoordinate(canvas, normalRadius)
                }
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(smallRadius, normalRadius), true)
            }
            AnimationType.LeftToCenter -> {
                xCoordinate = margin * animationValue
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(0f, smallRadius))
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(smallRadius, normalRadius), true)
                repeat(baseVisibleDotCount - 2) {
                    drawCircleAndShiftXCoordinate(canvas, normalRadius)
                }
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(normalRadius, smallRadius))
            }
            AnimationType.CenterToCenterRight -> {
                xCoordinate = margin * animationValue
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(0f, smallRadius))
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(smallRadius, normalRadius), true)
                repeat(baseVisibleDotCount - 2) {
                    drawCircleAndShiftXCoordinate(canvas, normalRadius)
                }
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(normalRadius, smallRadius))
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(smallRadius, 0f))
            }
            AnimationType.CenterToRight -> {
                xCoordinate = (smallSize + margin) * animationValue + margin
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(smallRadius, normalRadius), true)
                repeat(baseVisibleDotCount - 2) {
                    drawCircleAndShiftXCoordinate(canvas, normalRadius)
                }
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(normalRadius, smallRadius))
                drawCircleAndShiftXCoordinate(canvas, calculateRadiusInAnimation(smallRadius, 0f))
            }
        }
    }

    private fun drawCirclesAlongWithBias(canvas: Canvas) {
        xCoordinate = when (bias) {
            Bias.Right -> (smallSize + margin * 2).toFloat()
            Bias.Center, Bias.Left -> margin.toFloat()
        }
        when (bias) {
            Bias.Right -> {
                repeat(baseVisibleDotCount - 1) {
                    drawCircleAndShiftXCoordinate(canvas, normalRadius, it == dotPosition)
                }
                drawCircleAndShiftXCoordinate(canvas, smallRadius)
            }
            Bias.Center -> {
                drawCircleAndShiftXCoordinate(canvas, smallRadius)
                repeat(baseVisibleDotCount - 1) {
                    drawCircleAndShiftXCoordinate(canvas, normalRadius, it == dotPosition)
                }
                drawCircleAndShiftXCoordinate(canvas, smallRadius)
            }
            Bias.Left -> {
                drawCircleAndShiftXCoordinate(canvas, smallRadius)
                repeat(baseVisibleDotCount - 1) {
                    drawCircleAndShiftXCoordinate(canvas, normalRadius, it == dotPosition)
                }
            }
        }
    }

    private fun drawCircleAndShiftXCoordinate(canvas: Canvas, radius: Float, isActive: Boolean = false) {
        xCoordinate += radius
        canvas.drawCircle(xCoordinate, yCoordinate, radius, if (isActive) activePaint else inactivePaint)
        xCoordinate += radius + margin
    }

    fun setItemCount(count: Int) {
        isVisible = count > 1
        itemCount = count
        requestLayout()
        invalidate()
    }

    fun updatePosition(itemPosition: Int) {
        if (itemPosition !in 0 until itemCount) return
        if (areAllItemsShown) {
            dotPosition = itemPosition
            invalidate()
            return
        }

        animator = when (bias) {
            Bias.Right -> if (itemPosition == baseVisibleDotCount - 1) {
                animationType = AnimationType.RightToCenter
                createAnimator()
            } else null
            Bias.Center -> when {
                itemPosition == 0 -> {
                    animationType = AnimationType.CenterToRight
                    createAnimator()
                }
                itemPosition >= itemCount - 1 -> {
                    animationType = AnimationType.CenterToLeft
                    createAnimator()
                }
                else -> {
                    val newDotPosition = (dotPosition + itemPosition - lastItemPosition).coerceIn(0, maxCenterPosition)
                    when {
                        dotPosition == 0 && newDotPosition == 0 -> {
                            animationType = AnimationType.CenterToCenterRight
                            createAnimator()
                        }
                        dotPosition == maxCenterPosition && newDotPosition == maxCenterPosition -> {
                            animationType = AnimationType.CenterToCenterLeft
                            createAnimator()
                        }
                        else -> null
                    }
                }
            }
            Bias.Left -> if (itemPosition == itemCount - baseVisibleDotCount) {
                animationType = AnimationType.LeftToCenter
                createAnimator()
            } else null
        }
        animator?.start()

        bias = when (bias) {
            Bias.Right -> when {
                itemPosition >= itemCount - 1 -> Bias.Left
                itemPosition >= baseVisibleDotCount - 1 -> Bias.Center
                else -> Bias.Right
            }
            Bias.Center -> when {
                itemPosition == 0 -> Bias.Right
                itemPosition >= itemCount - 1 -> Bias.Left
                else -> Bias.Center
            }
            Bias.Left -> when {
                itemPosition == 0 -> Bias.Right
                itemPosition <= itemCount - baseVisibleDotCount -> Bias.Center
                else -> Bias.Left
            }
        }
        dotPosition = when (bias) {
            Bias.Right -> {
                itemPosition
            }
            Bias.Center -> {
                (dotPosition + itemPosition - lastItemPosition).coerceIn(0, maxCenterPosition)
            }
            Bias.Left -> {
                itemPosition - itemCount + baseVisibleDotCount - 1
            }
        }
        this.lastItemPosition = itemPosition
        invalidate()
    }

    private fun createAnimator(): ValueAnimator {
        animator?.cancel()
        return ValueAnimator.ofFloat(0f, 1f).apply {
            duration = ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                animationValue = valueAnimator.animatedValue as? Float ?: 0f
                invalidate()
            }
            addListener(object : AnimatorListener() {

                override fun onAnimationEnd(animation: Animator?) {
                    animator = null
                    invalidate()
                }
            })
        }
    }

    private fun calculateRadiusInAnimation(from: Float, to: Float): Float = from - (from - to) * animationValue

    private enum class Bias {
        Right,
        Center,
        Left
    }

    private enum class AnimationType {
        RightToCenter,
        CenterToCenterLeft,
        CenterToLeft,
        LeftToCenter,
        CenterToCenterRight,
        CenterToRight
    }
}

abstract class AnimatorListener : Animator.AnimatorListener {

    override fun onAnimationStart(animation: Animator?) {}

    override fun onAnimationEnd(animation: Animator?) {}

    override fun onAnimationCancel(animation: Animator?) {}

    override fun onAnimationRepeat(animation: Animator?) {}
}