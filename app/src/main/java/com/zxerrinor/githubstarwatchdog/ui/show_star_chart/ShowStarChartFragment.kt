package com.zxerrinor.githubstarwatchdog.ui.show_star_chart

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Spinner
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.switchmaterial.SwitchMaterial
import com.omega_r.adapters.OmegaSpinnerAdapter
import com.omega_r.base.components.OmegaFragment
import com.omegar.mvp.ktx.providePresenter
import com.zxerrinor.githubstarwatchdog.MONTH_ARGUMENT_NAME
import com.zxerrinor.githubstarwatchdog.R
import com.zxerrinor.githubstarwatchdog.ui.base.BaseFragment
import java.text.DecimalFormat
import java.time.Month
import java.util.*


class ShowStarChartFragment : BaseFragment(R.layout.fragment_show_star_chart), ShowStarChartView {

    override val presenter: ShowStarChartPresenter by providePresenter()

    private val starsPerMonthChart: BarChart by bind(R.id.chart_stars_per_month)
    private val currentRepoIsFavouriteSwitch: SwitchMaterial by bind(R.id.switch_current_repo_is_favourite)
    private val monthSpinner: Spinner by bind(R.id.spinner_month) {
        adapter = monthSpinnerAdapter
    }
    private val monthSpinnerAdapter: OmegaSpinnerAdapter.StringAdapter by bind(init = {
        return@bind OmegaSpinnerAdapter.StringAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    })
    private val showMonthButton: Button by bind(R.id.button_show_month)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentRepoIsFavouriteSwitch.setOnClickListener {
            presenter.onCurrentRepoFavouriteSwitchClicked()
        }

        showMonthButton.setOnClickListener {
            presenter.onShowMonthButtonClicked(monthSpinner.selectedItem.toString())
        }
    }

    override fun setIsFavouriteSwitchState(state: Boolean) {
        val activity = activity ?: throw IllegalStateException("Fragment must be in activity")
        activity.runOnUiThread {
            currentRepoIsFavouriteSwitch.isChecked = state
        }
    }

    override fun setChartEntries(entries: List<BarEntry>, label: String, start: Int) {
        val chart = starsPerMonthChart
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null) {
                    val bundle = Bundle()
                    bundle.putByte(MONTH_ARGUMENT_NAME, e.x.toInt().toByte())
                    findNavController().navigate(
                        R.id.action_ShowStarChartFragment_to_ShowUserListOfMonthFragment,
                        bundle
                    )
                }
            }

            override fun onNothingSelected() {
                // nothing
            }
        })
        val data = BarData(BarDataSet(entries, label))
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = 12
        xAxis.axisMinimum = start.toFloat() - 0.5F
        chart.data = data
        chart.setFitBars(true)
        xAxis.valueFormatter = CustomValueFormatter(xAxis.mDecimals)
        chart.invalidate()
    }

    override fun setMonthSpinnerContent(monthList: List<String>) {
        val activity = activity ?: throw IllegalStateException("Fragment must be in activity")
        activity.runOnUiThread {
            monthSpinnerAdapter.list = monthList
        }
    }

    class CustomValueFormatter(
        decimalDigits: Int
    ) : ValueFormatter() {
        private var mFormat: DecimalFormat
        override fun getFormattedValue(value: Float): String {
            return Month.values()[(value.toInt() - 1) % 12].name.toLowerCase(Locale.ROOT)
                .capitalize(Locale.ROOT)
                .substring(0, 3)
        }

        init {
            val b = StringBuffer()
            for (i in 0 until decimalDigits) {
                if (i == 0) b.append(".")
                b.append("0")
            }
            mFormat = DecimalFormat("###,###,###,##0$b")
        }
    }
}