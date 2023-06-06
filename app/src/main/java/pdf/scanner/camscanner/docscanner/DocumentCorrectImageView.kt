package pdf.scanner.camscanner.docscanner

import pdf.scanner.camscanner.docscanner.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


@SuppressLint("AppCompatCustomView")
class DocumentCorrectImageView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ImageView(context, attrs, defStyleAttr) {
    private val _pointPaint: Paint
    private val _pointFillPaint: Paint
    private val _linePaint: Paint
    private val _guideLinePaint: Paint
    private var mPoint: Point? = null
    private val mMatrix = FloatArray(9)
    private val mPointLinePath: Path = Path()
    private val LEFT_TOP = 0
    private val RIGHT_TOP = 1
    private val RIGHT_BOTTOM = 2
    private val LEFT_BOTTOM = 3
    private var _scaleX = 0f
    private var _scaleY = 0f
    private var _rectWidth = 0
    private var _rectHeight = 0
    private var _rectTop = 0
    private var _rectLeft = 0
    private val _lineWidth: Float
    private val _pointColor: Int
    private val _pointWidth: Float
    private val _guideLineWidth: Float
    private val _pointFillColor: Int
    private val _pointFillAlpha: Int
    private val _lineColor: Int
    private val _guideLineColor: Int

    constructor(context: Context) : this(context, null) {}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    private lateinit var mChoosePoints: Array<Point>

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.DocumentCorrectImageView)
        _lineColor = typedArray.getColor(R.styleable.DocumentCorrectImageView_LineColor, -0xff0001)
        _lineWidth =
            typedArray.getDimension(R.styleable.DocumentCorrectImageView_LineWidth, dp2px(1f))
        _pointColor =
            typedArray.getColor(R.styleable.DocumentCorrectImageView_PointColor, -0xff0001)
        _pointWidth =
            typedArray.getDimension(R.styleable.DocumentCorrectImageView_PointWidth, dp2px(1f))
        _guideLineWidth = typedArray.getDimension(
            R.styleable.DocumentCorrectImageView_GuideLineWidth,
            dp2px(0.5f)
        )
        _guideLineColor =
            typedArray.getColor(R.styleable.DocumentCorrectImageView_GuideLineColor, Color.WHITE)
        _pointFillColor =
            typedArray.getColor(R.styleable.DocumentCorrectImageView_PointFillColor, Color.WHITE)
        _pointFillAlpha = 0.coerceAtLeast(
            typedArray.getInt(
                R.styleable.DocumentCorrectImageView_PointFillAlpha,
                175
            )
        ).coerceAtMost(255)
        typedArray.recycle()
        _pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        _pointPaint.color = _pointColor
        _pointPaint.strokeWidth = _pointWidth
        _pointPaint.style = Paint.Style.STROKE
        _pointFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        _pointFillPaint.color = _pointFillColor
        _pointFillPaint.style = Paint.Style.FILL
        _pointFillPaint.alpha = _pointFillAlpha
        _linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        _linePaint.color = _lineColor
        _linePaint.strokeWidth = _lineWidth
        _linePaint.style = Paint.Style.STROKE
        _guideLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        _guideLinePaint.color = _guideLineColor
        _guideLinePaint.style = Paint.Style.FILL
        _guideLinePaint.strokeWidth = _guideLineWidth
    }

    fun setPoints(mChoosePoints: Array<Point>) {
        if (this.drawable != null) {
            this.mChoosePoints = mChoosePoints
            invalidate()
        }
    }

    val cropPoints: Array<Point>
        get() = mChoosePoints
    val isIrRegular: Boolean
        get() {
            if (!isNull(mChoosePoints)) {
                val leftTop: Point = mChoosePoints[0]
                val rightTop: Point = mChoosePoints[1]
                val rightBottom: Point = mChoosePoints[2]
                val leftBottom: Point = mChoosePoints[3]
                return operator(leftTop, rightBottom, leftBottom.x, leftBottom.y) * operator(
                    leftTop,
                    rightBottom,
                    rightTop.x,
                    rightTop.y
                ) < 0 &&
                        operator(leftBottom, rightTop, leftTop.x, leftTop.y) * operator(
                    leftBottom,
                    rightTop,
                    rightBottom.x,
                    rightBottom.y
                ) < 0
            }
            return false
        }

    private fun dp2px(dp: Float): Float {
        val density: Float = resources.displayMetrics.density
        return dp * density
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val drawable: Drawable = drawable
        imageMatrix.getValues(mMatrix)
        _scaleX = mMatrix[0]
        _scaleY = mMatrix[4]
        if (drawable != null) {
            val intrinsicWidth = drawable.intrinsicWidth
            val intrinsicHeight = drawable.intrinsicHeight
            _rectWidth = (intrinsicWidth * _scaleX).roundToInt()
            _rectHeight = (intrinsicHeight * _scaleY).roundToInt()
            _rectTop = (height - _rectHeight) / 2
            _rectLeft = (width - _rectWidth) / 2
        }
        if (isNull(mChoosePoints)) {
            mPointLinePath.reset()
            val leftTop: Point = mChoosePoints[0]
            val rightTop: Point = mChoosePoints[1]
            val rightBottom: Point = mChoosePoints[2]
            val leftBottom: Point = mChoosePoints[3]
            mPointLinePath.moveTo(getPointX(leftTop), getPointY(leftTop))
            mPointLinePath.lineTo(getPointX(rightTop), getPointY(rightTop))
            mPointLinePath.lineTo(getPointX(rightBottom), getPointY(rightBottom))
            mPointLinePath.lineTo(getPointX(leftBottom), getPointY(leftBottom))
            mPointLinePath.close()
            val path: Path = mPointLinePath
            if (path != null) {
                canvas.drawPath(path, _linePaint)
            }
            for (point in mChoosePoints) {
                canvas.drawCircle(getPointX(point), getPointY(point), dp2px(10f), _pointPaint)
                canvas.drawCircle(getPointX(point), getPointY(point), dp2px(10f), _pointFillPaint)
            }
        }
    }

    private fun operator(point1: Point, point2: Point, x: Int, y: Int): Int {
        val point1X: Int = point1.x
        val point1Y: Int = point1.y
        val point2X: Int = point2.x
        val point2Y: Int = point2.y
        return (x - point1X) * (point2Y - point1Y) - (y - point1Y) * (point2X - point1X)
    }

    private fun getPointX(point: Point): Float {
        return point.x * _scaleX + _rectLeft
    }

    private fun getPointY(point: Point): Float {
        return point.y * _scaleY + _rectTop
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var variable = true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isNull(mChoosePoints)) {
                    for (point in mChoosePoints) {
                        val downX = event.x
                        val downY = event.y
                        val pointX = getPointX(point)
                        val pointY = getPointY(point)
                        val distance = sqrt(
                            (downX - pointX).toDouble().pow(2.0) + (downY - pointY).toDouble()
                                .pow(2.0)
                        )
                        if (distance < dp2px(14f)) {
                            mPoint = point
                        }
                    }
                }
                if (mPoint == null) variable = false
            }
            MotionEvent.ACTION_MOVE -> {
                val pointType : PointType?  = getPointType(mPoint)
                val x = ((event.x.coerceAtLeast(_rectLeft.toFloat())
                    .coerceAtMost((_rectLeft + _rectWidth).toFloat()) - _rectLeft) / _scaleX).toInt()
                val y = ((event.y.coerceAtLeast(_rectTop.toFloat())
                    .coerceAtMost((_rectTop + _rectHeight).toFloat()) - _rectTop) / _scaleY).toInt()
                if (mPoint != null && pointType != null) {
                    if (pointType === PointType.LEFT_TOP && moveLeftTop(
                            x,
                            y
                        ) || pointType === PointType.RIGHT_TOP && moveRightTop(
                            x,
                            y
                        ) || pointType === PointType.RIGHT_BOTTOM && moveRightBottom(
                            x,
                            y
                        ) || pointType === PointType.LEFT_BOTTOM && moveLeftBottom(x, y)
                    ) {
                        mPoint!!.x = x
                        mPoint!!.y = y
                    }
                }
            }
            MotionEvent.ACTION_UP -> mPoint = null
        }
        invalidate()
        return variable || super.onTouchEvent(event)
    }

    private fun compare(point1: Point, point2: Point, x: Int, y: Int, point3: Point): Boolean {
        return operator(point1, point2, x, y) *
                operator(point1, point2, point3.x, point3.y) <= 0
    }

    private fun moveLeftTop(x: Int, y: Int): Boolean {
        compare(
            mChoosePoints[RIGHT_TOP], mChoosePoints[LEFT_BOTTOM], x, y,
            mChoosePoints[RIGHT_BOTTOM]
        )
        compare(
            mChoosePoints[RIGHT_TOP], mChoosePoints[RIGHT_BOTTOM], x, y,
            mChoosePoints[LEFT_BOTTOM]
        )
        compare(
            mChoosePoints[LEFT_BOTTOM], mChoosePoints[RIGHT_BOTTOM], x, y,
            mChoosePoints[RIGHT_TOP]
        )
        return true
    }

    private fun moveRightTop(x: Int, y: Int): Boolean {
        compare(
            mChoosePoints[LEFT_TOP], mChoosePoints[RIGHT_BOTTOM], x, y,
            mChoosePoints[LEFT_BOTTOM]
        )
        compare(
            mChoosePoints[LEFT_TOP], mChoosePoints[LEFT_BOTTOM], x, y,
            mChoosePoints[RIGHT_BOTTOM]
        )
        compare(
            mChoosePoints[LEFT_BOTTOM], mChoosePoints[RIGHT_BOTTOM], x, y,
            mChoosePoints[LEFT_TOP]
        )
        return true
    }

    private fun moveRightBottom(x: Int, y: Int): Boolean {
        compare(mChoosePoints[RIGHT_TOP], mChoosePoints[LEFT_BOTTOM], x, y, mChoosePoints[LEFT_TOP])
        compare(mChoosePoints[LEFT_TOP], mChoosePoints[RIGHT_TOP], x, y, mChoosePoints[LEFT_BOTTOM])
        compare(mChoosePoints[LEFT_TOP], mChoosePoints[LEFT_BOTTOM], x, y, mChoosePoints[RIGHT_TOP])
        return true
    }

    private fun moveLeftBottom(x: Int, y: Int): Boolean {
        compare(
            mChoosePoints[LEFT_TOP], mChoosePoints[RIGHT_BOTTOM], x, y,
            mChoosePoints[RIGHT_TOP]
        )
        compare(
            mChoosePoints[LEFT_TOP], mChoosePoints[RIGHT_TOP], x, y,
            mChoosePoints[RIGHT_BOTTOM]
        )
        compare(
            mChoosePoints[RIGHT_TOP], mChoosePoints[RIGHT_BOTTOM], x, y,
            mChoosePoints[LEFT_TOP]
        )
        return true
    }

    private fun getPointType(point: Point?): PointType? {
        var type: PointType? = null
        if (point != null) {
            if (isNull(mChoosePoints)) {
                for (i in mChoosePoints.indices) {
                    if (point === mChoosePoints[i]) {
                        type = PointType.values()[i]
                    }
                }
            }
        }
        return type
    }

    private fun isNull(points: Array<Point>?): Boolean {
        return points != null && points.size == 4
    }
}