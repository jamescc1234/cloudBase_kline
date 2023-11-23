package com.james.kline_library

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.ColorInt
import com.drawing.DrawingData
import com.drawing.DrawingStatusData
import com.drawing.DrawingType
import com.utils.DrawLineMessageViewController
import com.utils.DrawingStatusListener
import com.utils.LocalStore
import com.utils.TouchArea
import com.widget.stock.k_line.adapter.KLineChartAdapter
import com.widget.stock.k_line.configure.KlineConstants
import com.widget.stock.k_line.data.KLineData
import com.widget.stock.k_line.view.KLineChartFrameLayout
import com.widget.stock.k_line.view.KLineChartMainView
import com.widget.stock.k_line.view.KLineFloatingView
import com.widget.stock.k_line.view.KLineTopFloatView
import org.json.JSONArray
import org.json.JSONException

class MainActivity : AppCompatActivity(), DrawLineMessageViewController {
    private var mKlineChartAdapter = KLineChartAdapter()
    private val klineFloatingView by lazy { KLineFloatingView(this) }
    private var isDrawing = false
    private var isZoomEnabled = false
    private var kLineChartFrameLayout: KLineChartFrameLayout? = null
    private var klineTopFloatingView: KLineTopFloatView? = null
    private var kLineChartMainView: KLineChartMainView? = null
    private var myKLineThread: MyKLineThread? = null
    private var layoutDrawLineMessage: View? = null
    private var appCompatTextViewLabel: TextView? = null
    private var appCompatTextViewContent: TextView? = null
    private val colorList = arrayListOf(Color.BLUE, Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN)

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val lp = linearLayout.layoutParams as LinearLayout.LayoutParams
            lp.height = resources.getDimensionPixelSize(binance.stock.library.R.dimen.x1050)
            lp.weight = 0f
            linearLayout.layoutParams = lp
        } else {
            val lp = linearLayout.layoutParams as LinearLayout.LayoutParams
            lp.height = 0
            lp.weight = 1f
            linearLayout.layoutParams = lp
        }
    }

    /**
     * 解析K线
     */
    inner class MyKLineThread : XBaseThread() {
        var obj: Any? = null
        override fun running() {
            try {
                var list: ArrayList<KLineData> = ArrayList()
//                if (tradPairInfo.type == 1) {
                    var ja = JSONArray(Parse.getInstance().toString(obj))
                    for (index in 0 until ja.length()) {
                        if (isCancel) {
                            return
                        }
                        var jas = ja.getJSONArray(index)
                        list.add(
                            KLineData(
                                "",
                                Parse.getInstance().parseLong(jas[0]),
                                Parse.getInstance().parseDouble(jas[2]),
                                Parse.getInstance().parseDouble(jas[1]),
                                Parse.getInstance().parseDouble(jas[3]),
                                Parse.getInstance().parseDouble(jas[4]),
                                Parse.getInstance().parseDouble(jas[5]),
                                Parse.getInstance().parseDouble(jas[7])
                            )
                        )
                    }
//                } else if (tradPairInfo.type == 2) {
//                    var jo = JSONObject(Parse.getInstance().toString(obj))
//                    val data = jo.getJSONObject("data")
//                    val ja = data.getJSONArray("list")
//                    for (i in 0 until ja.length()) {
//                        if (isCancel) {
//                            return
//                        }
//                        var jas = ja.getJSONArray(i)
//                        list.add(
//                            0,
//                            KLineData(
//                                "",
//                                Parse.getInstance().parseLong(jas[0]),
//                                Parse.getInstance().parseDouble(jas[2]),
//                                Parse.getInstance().parseDouble(jas[1]),
//                                Parse.getInstance().parseDouble(jas[3]),
//                                Parse.getInstance().parseDouble(jas[4]),
//                                Parse.getInstance().parseDouble(jas[5]),
//                                Parse.getInstance().parseDouble(jas[7])
//                            )
//                        )
//                    }
//                }
                if (isCancel) {
                    return
                }
//                HandlerUtils.getInstance().sendMessage(handler, 0x2, list)

                runOnUiThread {
                    Log.d("MainActivity", "list.size: ${list.size}")
                    mKlineChartAdapter.setData(list)
                    kLineChartFrameLayout?.setAdapter(mKlineChartAdapter)
                }
                return
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            if (isCancel) {
                return
            }
//            HandlerUtils.getInstance().sendEmptyMessage(handler, 0x3)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalStore.initialize(this)
        setContentView(R.layout.activity_main)
        setData()

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
    }


    private fun setData() {
        LocalStore.getInstance().put(KlineConstants.KEY_KLINE_FLOAT_STYLE, 1)
        klineFloatingView.apply {
            symbol = "BTCUSDT"
            setDateFormat("yyyy-MM-dd")
            showAmpl = true
//            setDecNum(2)
        }

        klineTopFloatingView = findViewById<KLineTopFloatView>(R.id.topFloatView)?.apply {
            symbol = "BTCUSDT"
            setDateFormat("yyyy-MM-dd")
        }
        kLineChartFrameLayout = findViewById<KLineChartFrameLayout>(R.id.klineChartFragment).apply {
//            setAdapter(mKlineChartAdapter)
//            setIsZoomToLineStatus(true)
            setFloatingWindow(klineFloatingView)
            setIsTapToShowAbstractEnabled(true)
            onlyShowFloatingView = true
            klineTopFloatingView?.let {
                addOnKLineListener(it)
                it.setDecNum(4)
            }

            setMagnifierLayout(findViewById(R.id.magnifier))
//            symbol = "BTCUSDT"
            setSymbolAndFlagTime("BTCUSDT", "1min")
            decPlace = 4
            klineFloatingView.setDecNum(4)

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setIsDrawingStatus(true)
            }

            addDrawingStatusListener(object: DrawingStatusListener {
                override fun notifyDrawingCompleted() {
                }

                override fun notifyDrawingSelectedStatus(drawingData: DrawingData?) {
                }

                override fun reportDrawingStatusChanged(drawingStatusData: DrawingStatusData) {
                    val isFinished = drawingStatusData.alreadyAddedPoint == drawingStatusData.needPoint

                    if (isFinished) {
                        showDrawLineMessageView(false)
                    } else {
                        showDrawLineMessageView(true)

                        val content = drawingStatusData.run {
                            val surfix = "($alreadyAddedPoint/$needPoint)"
                            val context = context ?: return@run surfix
                            context.getString(
                                R.string.marketdetail_click_n_anchor_points_to_finish_drawing,
                                needPoint
                            ) + surfix
                        }
                        updateContent(content)
                        val lineType = context
                            ?.let { drawingStatusData.type } ?: ""
                        val selected = context?.getString(R.string.marketdetail_selected)
                        val title = "$lineType $selected"
                        updateTitle(title)
                    }
//                    Toast.makeText(this@MainActivity, drawingStatusData.toString(), Toast.LENGTH_SHORT).show()
                }

                override fun reportDoubleTap(area: TouchArea) {
                }

            })
        }
        kLineChartMainView = findViewById<KLineChartMainView?>(R.id.klineChartMainView).apply {
            decPlace = 4
            setAnodeColor(Color.parseColor("#FF0000"))
            setCathodeColor(Color.parseColor("#00FF00"))
        }
        layoutDrawLineMessage = findViewById(R.id.layoutDrawLineMessage)
        appCompatTextViewLabel = layoutDrawLineMessage?.findViewById(binance.stock.library.R.id.appCompatTextViewLabel)
        appCompatTextViewContent = layoutDrawLineMessage?.findViewById(binance.stock.library.R.id.appCompatTextViewContent)

        try {
            val jsonString = assets.open("testdata.json").bufferedReader().use { it.readText() }

            if (myKLineThread != null) {
                myKLineThread!!.cancel()
            }
            myKLineThread = MyKLineThread()
            myKLineThread!!.obj = jsonString
            ExecutorServiceUtils.getInstance().executorService.execute(myKLineThread)
        } catch (e: Exception) {

        }


//        val list = arrayListOf(
//            KLineData("", 1673352000000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673355600000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673359200000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673362800000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673366400000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673370000000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673373600000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673377200000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673380800000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673384400000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673352000000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673355600000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673359200000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673362800000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673366400000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673370000000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673373600000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673377200000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673380800000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673384400000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673352000000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673355600000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673359200000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673362800000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673366400000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673370000000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673373600000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673377200000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673380800000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673384400000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673352000000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673355600000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673359200000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673362800000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673366400000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673370000000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673373600000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673377200000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0),
//            KLineData("", 1673380800000, 17257.19, 17252.89, 17218.44, 17224.15, 8539.8137, 147171813202.0),
//            KLineData("",1673384400000,  17326.07, 17246.45, 17220.0, 17286.59, 21263.07475, 3672508753714.0)
//        )
//        mKlineChartAdapter.setData(list)
        initEvent()
    }

    private fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun initEvent() {
        findViewById<ImageView>(R.id.ivSwitchLandscapeNew).setOnClickListener {
            requestedOrientation = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
        findViewById<KLineChartMainView>(R.id.klineChartMainView).apply {
            kLineMainIndex = KLineChartMainView.KLineMainIndex.TIME
        }

        findViewById<Button>(R.id.drawingBtn).setOnClickListener {
            isDrawing = !isDrawing
            kLineChartFrameLayout?.setIsDrawingStatus(isDrawing)

//            if (isDrawing) {
//                kLineChartFrameLayout?.startDrawing(DrawingType.FIBONACCI)
//            }

            (it as Button).text = if (isDrawing) {
                getString(R.string.disable_drawing)
            } else {
                getString(R.string.enable_drawing)
            }
        }

        findViewById<Button>(R.id.zoomEnable).setOnClickListener {
            isZoomEnabled = !isZoomEnabled
            kLineChartFrameLayout?.setIsZoomToLineStatus(isZoomEnabled)

            (it as Button).text = if (isZoomEnabled) {
                getString(R.string.disable_drawing)
            } else {
                getString(R.string.enable_drawing)
            }
        }

        findViewById<Spinner>(R.id.spinner).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("onNothingSelected", "onNothingSelected")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position > 0 && kLineChartFrameLayout?.drawingStatus?.getDrawingMode() == true) {
                    kLineChartFrameLayout?.startDrawing(DrawingType.values()[position - 1])
                }
            }
        }

        findViewById<Button>(R.id.deleteBtn).setOnClickListener {
            kLineChartFrameLayout?.deleteCurrentSelectedDrawing()
        }

        findViewById<Button>(R.id.clearBtn).setOnClickListener {
            kLineChartFrameLayout?.deleteAllDrawing()
        }

        findViewById<Button>(R.id.lockBtn).setOnClickListener {
            kLineChartFrameLayout?.lockLine()
        }

        findViewById<Button>(R.id.continueBtn).setOnClickListener {
            kLineChartFrameLayout?.toggleContinueDrawing()
        }

        findViewById<Button>(R.id.hideBtn).setOnClickListener {
            kLineChartFrameLayout?.toggleHideDrawing()
        }

        findViewById<Button>(R.id.colorBtn).setOnClickListener {
            kLineChartFrameLayout?.setDrawingLineColor(colorList[(0 until colorList.size).random()])
        }

        findViewById<Button>(R.id.fillColorBtn).setOnClickListener {
            kLineChartFrameLayout?.setDrawingFillColor(adjustAlpha(colorList[(0 until colorList.size).random()], 0.3f))
        }

        findViewById<Button>(R.id.widthBtn).setOnClickListener {
            kLineChartFrameLayout?.setDrawingWidth((1..10).random().toFloat())
        }

        findViewById<Button>(R.id.styleBtn).setOnClickListener {
            kLineChartFrameLayout?.setDrawingLineStyle(
                (1..5).random().toFloat(),
                (1..5).random().toFloat()
            )
        }
    }

    override fun updateTitle(title: CharSequence?) {
        appCompatTextViewLabel?.text = title
    }

    override fun updateContent(content: CharSequence?) {
        appCompatTextViewContent?.text = content
    }

    override fun showDrawLineMessageView(show: Boolean) {
        layoutDrawLineMessage?.visibility = if (show) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }
}