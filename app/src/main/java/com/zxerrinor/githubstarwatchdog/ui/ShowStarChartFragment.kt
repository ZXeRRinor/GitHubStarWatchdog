package com.zxerrinor.githubstarwatchdog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.omegar.mvp.MvpAppCompatFragment
import com.omegar.mvp.presenter.InjectPresenter
import com.zxerrinor.githubstarwatchdog.CurrentValuesStore
import com.zxerrinor.githubstarwatchdog.R
import com.zxerrinor.githubstarwatchdog.databinding.FragmentShowStarChartBinding
import com.zxerrinor.githubstarwatchdog.presenters.ShowStarChartPresenter
import com.zxerrinor.githubstarwatchdog.views.ShowStarChartView
import java.text.DecimalFormat
import java.time.Month
import java.util.*


class ShowStarChartFragment : MvpAppCompatFragment(), ShowStarChartView {
    private var _binding: FragmentShowStarChartBinding? = null
    private val binding get() = _binding!!

    @InjectPresenter
    lateinit var showStarChartPresenter: ShowStarChartPresenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowStarChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.currentRepoFavouriteSwitch.setOnClickListener {
            showStarChartPresenter.onCurrentRepoFavouriteSwitchClicked()
        }

        binding.showMonthButton.setOnClickListener {
            showStarChartPresenter.onShowMonthButtonClicked(binding.monthInput.selectedItem.toString())
            findNavController().navigate(R.id.action_ShowStarChartFragment_to_ShowUserListOfMonth)
        }
    }

    override fun setIsFavouriteSwitchState(state: Boolean) {
        CurrentValuesStore.activity.runOnUiThread {
            binding.currentRepoFavouriteSwitch.isChecked = state
        }
    }

    override fun setChartEntries(entries: List<BarEntry>, label: String, start: Int) {
        val chart = binding.starChart
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null) {
                    CurrentValuesStore.month = e.x.toInt()
                    findNavController().navigate(R.id.action_ShowStarChartFragment_to_ShowUserListOfMonth)
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

    override fun setMonthInputAdapter(monthList: List<String>) {
        val monthInputAdapter = ArrayAdapter(
            CurrentValuesStore.activity,
            android.R.layout.simple_spinner_item,
            monthList
        )
        monthInputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        CurrentValuesStore.activity.runOnUiThread {
            binding.monthInput.adapter = monthInputAdapter
            monthInputAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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