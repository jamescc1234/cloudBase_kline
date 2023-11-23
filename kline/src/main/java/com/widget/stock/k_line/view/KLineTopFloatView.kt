package com.widget.stock.k_line.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.util.Pair
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import binance.stock.library.R
import com.contants.KlineConfig
import com.utils.LocalStore
import com.utils.gone
import com.utils.visible
import com.widget.stock.StockUtils
import com.widget.stock.k_line.configure.KlineConstants
import com.widget.stock.k_line.data.KLineDataValid
import com.widget.stock.k_line.data.TradeInfo
import com.widget.stock.utils.Utils
import kotlinx.android.synthetic.main.view_kline_floating.view.*
import java.util.ArrayList

/**
 * @author: Galon
 * @date: 2022/10/18
 */
class KLineTopFloatView : LinearLayout, KLineChartFrameLayout.OnKLineListener {

    constructor(context: Context) : this(context, null) {
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0) {

    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private var floatingTvDate: TextView? = null
    private var floatingTvOpen: TextView? = null
    private var floatingTvHigh: TextView? = null
    private var floatingTvLow: TextView? = null
    private var floatingTvClose: TextView? = null
    private var floatingTvChange: TextView? = null
    private var floatingTvVol: TextView? = null

    private var decNum: Int = 2
    private var dateFormat: String = "MM-dd HH:mm"
    var symbol: String = ""

    private fun init(context: Context, attrs: AttributeSet?) {
        val v = LayoutInflater.from(context).inflate(R.layout.view_kline_top_floating, this)
        floatingTvDate = v.findViewById(R.id.floatingTvDate)
        floatingTvOpen = v.findViewById(R.id.floatingTvOpen)
        floatingTvHigh = v.findViewById(R.id.floatingTvHigh)
        floatingTvLow = v.findViewById(R.id.floatingTvLow)
        floatingTvClose = v.findViewById(R.id.floatingTvClose)
        floatingTvChange = v.findViewById(R.id.floatingTvChange)
        floatingTvVol = v.findViewById(R.id.floatingTvVol)
        visibility = View.GONE
    }

    fun setDecNum(decNum: Int) {
        this.decNum = decNum
    }

    fun setDateFormat(dateFormat: String) {
        this.dateFormat = dateFormat
    }

    override fun onCursorVisible(
        mKLineChartFrameLayout: KLineChartFrameLayout,
        isCursorVisible: Boolean
    ) {
        val style = LocalStore.getInstance().getInt(KlineConstants.KEY_KLINE_FLOAT_STYLE, 0)

        if (mKLineChartFrameLayout.onlyShowFloatingView) {
            visibility = View.GONE
            return
        }

        val oritation = mKLineChartFrameLayout.orientation
        visibility = if (isCursorVisible) {
            if (style == 1 && oritation == Configuration.ORIENTATION_PORTRAIT) {
                View.VISIBLE
            } else {
                View.GONE
            }
        } else {
            View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onKLineListener(
        mKLineChartFrameLayout: KLineChartFrameLayout,
        kLineList: ArrayList<KLineDataValid>?,
        position: Int,
        cursorX: Float,
        cursorY: Float,
        tradeMap: Map<Long, Pair<TradeInfo, TradeInfo>>
    ) {
        val item = kLineList!![position] ?: return
        var oldItem: KLineDataValid? = null
        if (position - 1 > -1) {
            oldItem = kLineList!![position - 1]
        }
        floatingTvDate!!.text = Utils.getInstance().date2String(dateFormat, item.time)
        floatingTvOpen!!.text = if (KlineConfig.SHOW_US_PRICE) {
            StockUtils.getInstance().parse2USString(item.open, false, decNum)
        } else {
            StockUtils.getInstance().parse2String(item.open, decNum)
        }
        floatingTvHigh!!.text = if (KlineConfig.SHOW_US_PRICE) {
            StockUtils.getInstance().parse2USString(item.hig, false, decNum)
        } else {
            StockUtils.getInstance().parse2String(item.hig, decNum)
        }
        floatingTvLow!!.text = if (KlineConfig.SHOW_US_PRICE) {
            StockUtils.getInstance().parse2USString(item.low, false, decNum)
        } else {
            StockUtils.getInstance().parse2String(item.low, decNum)
        }
        floatingTvClose!!.text = if (KlineConfig.SHOW_US_PRICE) {
            StockUtils.getInstance().parse2USString(item.close, false, decNum)
        } else {
            StockUtils.getInstance().parse2String(item.close, decNum)
        }
        StockUtils.getInstance().formatTextZDFOfZDF(
            StockUtils.getInstance().getZDF(
                if (oldItem == null) item.open else oldItem.close,
                item.close
            ), floatingTvChange!!
        )
        floatingTvVol!!.text = StockUtils.getInstance()
            .parse2USString(item!!.cje, false, false, 2)
        tradeMap[item.time]?.first?.let {
            lnlyBuy.visible()
            tvBuyPrice.text = "${context.getString(R.string.buy)} ${it.amount}"
            tvBuyAmount.text = "@${it.price}"
        } ?: lnlyBuy.gone()
        lnlyBuy.setOnClickListener {
//            Router.build(ORDER_LIST_MAIN)
//                .withString("symbol", symbol)
//                .withInt("chooseType",2)
//                .navigation(context)
        }

        tradeMap[item.time]?.second?.let {
            lnlySell.visible()
            tvSellPrice.text = "${context.getString(R.string.sell)} ${it.amount}"
            tvSellAmount.text = "@${it.price}"
        } ?: lnlySell.gone()
        lnlySell.setOnClickListener {
//            Router.build(ORDER_LIST_MAIN)
//                .withString("symbol", symbol)
//                .withInt("chooseType",2)
//                .navigation(context)
        }
    }

    override fun onScrollLeft() {
    }

    override fun onKLineEndDataChange(
        mKLineChartFrameLayout: KLineChartFrameLayout?,
        endKLineData: KLineDataValid?,
        preKLineData: KLineDataValid?
    ) {
    }

    override fun onKLineNewDataChange(
        mKLineChartFrameLayout: KLineChartFrameLayout?,
        endKLineData: KLineDataValid?,
        preKLineData: KLineDataValid?
    ) {
    }

    override fun onEndPoint(
        mKLineChartFrameLayout: KLineChartFrameLayout?,
        isCurrent: Boolean,
        x: Float,
        y: Float
    ) {
    }
}