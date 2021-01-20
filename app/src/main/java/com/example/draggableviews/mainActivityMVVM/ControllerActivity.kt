package com.example.draggableviews.mainActivityMVVM

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.draggableviews.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import kotlin.math.*


@Volatile var verticalValueFirst = 127
@Volatile var horizontalValueFirst = 127
@Volatile var verticalValueSecond = 127
@Volatile var horizontalValueSecond = 127
@Volatile var mode = 1

@AndroidEntryPoint
class ControllerActivity: AppCompatActivity() {

    @Inject
    lateinit var factory : ControllerActivityViewModelFactory
    private lateinit var viewModel: ControllerActivityViewModel

    private var radiusFirst: Float = 0f
    private var imageSemiSizeFirst = 0f

    private var radiusSecond: Float = 0f
    private var imageSemiSizeSecond = 0f

    private var firstControllerRootCenterX: Float = 0f
    private var firstControllerRootCenterY: Float = 0f

    private var firstControllerX: Float = 0f
    private var firstControllerY: Float = 0f

    private var firstControllerTouchX = 0f
    private var firstControllerTouchY = 0f


    private var secondControllerRootCenterX: Float = 0f
    private var secondControllerRootCenterY: Float = 0f

    private var secondControllerX: Float = 0f
    private var secondControllerY: Float = 0f

    private var secondControllerTouchX = 0f
    private var secondControllerTouchY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        supportActionBar!!.hide()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        viewModel = ViewModelProvider(this, factory).get(ControllerActivityViewModel::class.java)

        initViews()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews(){

        viewModel.checkingConnection().observe(this, {
            if(it != null){
                if(it == false) turnOnOff.text = "Connect"
                else turnOnOff.text = "Disconnect"
            }
        })
        firstControllerRoot.post {
            firstControllerRootCenterX = firstControllerRoot.x + firstControllerRoot.width / 2
            firstControllerRootCenterY = firstControllerRoot.y + firstControllerRoot.height / 2
            radiusFirst = (firstControllerRoot.height / 2).toFloat()
        }

        firstController.post {
            imageSemiSizeFirst = (firstController.width / 2).toFloat()
            firstControllerX = firstController.x + imageSemiSizeFirst
            firstControllerY = firstController.y + imageSemiSizeFirst
        }

        secondControllerRoot.post {
            secondControllerRootCenterX = secondControllerRoot.x + secondControllerRoot.width / 2
            secondControllerRootCenterY = secondControllerRoot.y + secondControllerRoot.height / 2
            radiusSecond = (secondControllerRoot.height / 2).toFloat()
        }

        secondController.post {
            imageSemiSizeSecond = (secondController.width / 2).toFloat()
            secondControllerX = secondController.x + imageSemiSizeSecond
            secondControllerY = secondController.y + imageSemiSizeSecond
        }

        getVersionButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            versionText.visibility = View.INVISIBLE

            viewModel.getVersion().observe(this, {
                if (it != null) {
                    progressBar.visibility = View.GONE
                    versionText.visibility = View.VISIBLE
                    versionText.text = it
                }
            })
        }

        turnOnOff.setOnClickListener {
            viewModel.start().observe(this, { image ->
                if (image != null) {
                    try {
                        val bmp = BitmapFactory.decodeByteArray(image, 0, image.size)
                        propeller.setImageBitmap(
                            Bitmap.createScaledBitmap(
                                bmp,
                                propeller.width,
                                propeller.height,
                                false
                            )
                        )
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            })
        }

        modeOne.setOnClickListener {
            modeOne.setTextColor(Color.parseColor("#ffffff"))
            modeTwo.setTextColor(Color.parseColor("#000000"))
            modeThree.setTextColor(Color.parseColor("#000000"))

            modeOne.backgroundTintList = ContextCompat.getColorStateList(
                this,
                R.color.colorPrimaryDark
            )
            modeTwo.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#d8d8d8"))
            modeThree.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#d8d8d8"))

            mode = 1
        }

        modeTwo.setOnClickListener {
            modeTwo.setTextColor(Color.parseColor("#ffffff"))
            modeOne.setTextColor(Color.parseColor("#000000"))
            modeThree.setTextColor(Color.parseColor("#000000"))

            modeTwo.backgroundTintList = ContextCompat.getColorStateList(
                this,
                R.color.colorPrimaryDark
            )
            modeOne.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#d8d8d8"))
            modeThree.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#d8d8d8"))

            mode = 2
        }

        modeThree.setOnClickListener {
            modeThree.setTextColor(Color.parseColor("#ffffff"))
            modeTwo.setTextColor(Color.parseColor("#000000"))
            modeOne.setTextColor(Color.parseColor("#000000"))

            modeThree.backgroundTintList = ContextCompat.getColorStateList(
                this,
                R.color.colorPrimaryDark
            )
            modeTwo.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#d8d8d8"))
            modeOne.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#d8d8d8"))

            mode = 3
        }

        val firstControllerListener = View.OnTouchListener(function = { view, motionEvent ->

            var out = true
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {

                val y = motionEvent.y
                val x = motionEvent.x

                val deltaX = x - firstControllerTouchX
                val deltaY = y - firstControllerTouchY
                var dist = sqrt(deltaX.pow(2) + deltaY.pow(2))
                var angle = atan((x - firstControllerTouchX) / (y - firstControllerTouchY))

                if (dist > radiusFirst) {
                    dist = radiusFirst
                }
                if (deltaY < 0) {
                    angle = -angle
                    firstController.x =
                        (dist * sin(angle.toDouble())).toFloat() + firstControllerX - imageSemiSizeFirst
                    firstController.y =
                        -(dist * cos(angle.toDouble())).toFloat() + firstControllerY - imageSemiSizeFirst
                } else {
                    firstController.x =
                        (dist * sin(angle.toDouble())).toFloat() + firstControllerX - imageSemiSizeFirst
                    firstController.y =
                        (dist * cos(angle.toDouble())).toFloat() + firstControllerY - imageSemiSizeFirst
                }

                val verticalValue =
                    ((firstControllerRootCenterY + radiusFirst - firstController.y - imageSemiSizeFirst) / (2 * radiusFirst) * 256).toInt()
                val horizontalValue =
                    ((firstController.x - firstControllerRootCenterX + radiusFirst + imageSemiSizeFirst) / (2 * radiusFirst) * 256).toInt()

                if (verticalValue == 0 && horizontalValue == 0) {
                    firstController.x = firstControllerX - imageSemiSizeFirst
                    firstController.y = firstControllerY - imageSemiSizeFirst
                } else {
                    verticalFirst.text = "vertical = $verticalValue"
                    horizontalFirst.text = "horizontal = ${horizontalValue - 1}"
                    verticalValueFirst = verticalValue
                    horizontalValueFirst = horizontalValue - 1
                }

            }

            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                firstControllerTouchX = motionEvent.x
                firstControllerTouchY = motionEvent.y
                if (sqrt(
                        (firstControllerTouchX - firstControllerX).pow(2) + (firstControllerTouchY - firstControllerY).pow(
                            2
                        )
                    ) > radiusFirst
                ) {
                    out = false
                } else {
                    vibratePhone()
                }
            }

            if (motionEvent.action == MotionEvent.ACTION_UP) {
                firstController.animate().translationX(0f).translationY(0f).duration = 100
                verticalFirst.text = "vertical = 127"
                horizontalFirst.text = "horizontal = 127"
                verticalValueFirst = 127
                horizontalValueFirst = 127
            }
            out
        })

        val secondControllerListener = View.OnTouchListener(function = { view, motionEvent ->

            var out = true
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {

                val y = motionEvent.y
                val x = motionEvent.x

                val deltaX = x - secondControllerTouchX
                val deltaY = y - secondControllerTouchY
                var dist = sqrt(deltaX.pow(2) + deltaY.pow(2))
                var angle = atan((x - secondControllerTouchX) / (y - secondControllerTouchY))

                if (dist > radiusSecond) {
                    dist = radiusSecond
                }
                if (deltaY < 0) {
                    angle = -angle
                    secondController.x =
                        (dist * sin(angle.toDouble())).toFloat() + secondControllerX - imageSemiSizeSecond
                    secondController.y =
                        -(dist * cos(angle.toDouble())).toFloat() + secondControllerY - imageSemiSizeSecond
                } else {
                    secondController.x =
                        (dist * sin(angle.toDouble())).toFloat() + secondControllerX - imageSemiSizeSecond
                    secondController.y =
                        (dist * cos(angle.toDouble())).toFloat() + secondControllerY - imageSemiSizeSecond
                }

                val verticalValue =
                    ((secondControllerRootCenterY + radiusSecond - secondController.y - imageSemiSizeSecond) / (2 * radiusSecond) * 256).toInt()
                val horizontalValue =
                    ((secondController.x - secondControllerRootCenterX + radiusSecond + imageSemiSizeSecond) / (2 * radiusSecond) * 256).toInt()

                if (verticalValue == 0 && horizontalValue == 0) {
                    secondController.x = secondControllerX - imageSemiSizeSecond
                    secondController.y = secondControllerY - imageSemiSizeSecond
                } else {
                    verticalSecond.text = "vertical = $verticalValue"
                    horizontalSecond.text = "horizontal = ${horizontalValue - 1}"
                    verticalValueSecond = verticalValue
                    horizontalValueSecond = horizontalValue - 1
                }

            }

            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                secondControllerTouchX = motionEvent.x
                secondControllerTouchY = motionEvent.y
                if (sqrt((secondControllerTouchX - secondControllerX).pow(2) + (secondControllerTouchY - secondControllerY).pow(2)) > radiusSecond) {
                    out = false
                } else {
                    vibratePhone()
                }
            }

            if (motionEvent.action == MotionEvent.ACTION_UP) {
                secondController.animate().translationX(0f).translationY(0f).duration = 100
                verticalSecond.text = "vertical = 127"
                horizontalSecond.text = "horizontal = 127"
                verticalValueSecond = 127
                horizontalValueSecond = 127
            }
            out
        })

        firstControllerArea.setOnTouchListener(firstControllerListener)
        secondControllerArea.setOnTouchListener(secondControllerListener)
    }

    private fun vibratePhone() {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(60)
        }
    }

}