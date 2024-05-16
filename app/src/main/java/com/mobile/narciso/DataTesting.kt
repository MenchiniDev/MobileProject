package com.mobile.narciso

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.mobile.narciso.databinding.FragmentDatatestingBinding

class DataTesting : Fragment() {

    private var _binding: FragmentDatatestingBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDatatestingBinding.inflate(inflater, container, false)

        val HRsensorDataListString = arguments?.getStringArrayList("HRsensorDataList")
        val PPGsensorDataListString = arguments?.getStringArrayList("PPGsensorDataList")
        val EDAsensorDataListString = arguments?.getStringArrayList("EDAsensorDataList")
        val EEGsensorDataListString = arguments?.getStringArrayList("EEGsensorDataList")

        Log.d("EEGsensorDataList", EEGsensorDataListString.toString())

        // Conversione dell'ArrayList<String> in ArrayList<Float>
        val HRsensorDataList = HRsensorDataListString?.map { it.toFloat() } as ArrayList<Float>
        val PPGsensorDataList = PPGsensorDataListString?.map { it.toFloat() } as ArrayList<Float>
        val EDAsensorDataList = EDAsensorDataListString?.map { it.toFloat() } as ArrayList<Float>

        Log.d("EDAsensorDataList", EDAsensorDataList.toString())
        Log.d("PPGsensorDataList", PPGsensorDataList.toString())


        val EEGsensorDataList = ArrayList<Float>()

        val lineGraphView1 = binding.idGraphView1
        val lineGraphView2 = binding.idGraphView2
        val lineGraphView3 = binding.idGraphView3

        val lineGraphView4 = binding.idGraphView4
        val lineGraphView5 = binding.idGraphView5
        val lineGraphView6 = binding.idGraphView6
        val lineGraphView7 = binding.idGraphView7

        //lets plot all different values of channel 1
        for (i in 0 until MainActivity.EEGsensordataList.size) {
            EEGsensorDataList.add(MainActivity.EEGsensordataList[i].channel1.toFloat())
        }
        createGraph(lineGraphView4,EEGsensorDataList, "CHANNEL 1")

        //lets plot all different values of channel 2
        for (i in 0 until MainActivity.EEGsensordataList.size) {
            EEGsensorDataList.add(MainActivity.EEGsensordataList[i].channel2.toFloat())
        }
        createGraph(lineGraphView5,EEGsensorDataList, "CHANNEL 2")

        //lets plot all different values of channel 3
        for (i in 0 until MainActivity.EEGsensordataList.size) {
            EEGsensorDataList.add(MainActivity.EEGsensordataList[i].channel3.toFloat())
        }
        createGraph(lineGraphView6,EEGsensorDataList, "CHANNEL 3")

        //lets plot all different values of channel 4
        for (i in 0 until MainActivity.EEGsensordataList.size) {
            EEGsensorDataList.add(MainActivity.EEGsensordataList[i].channel4.toFloat())
        }
        createGraph(lineGraphView7,EEGsensorDataList, "CHANNEL 4")


        //TODO display all plots of data collected during dataCollection session


        createGraph(lineGraphView1,HRsensorDataList, "Hear Rate")
        createGraph(lineGraphView2,PPGsensorDataList, "PPG value")
        createGraph(lineGraphView3,EDAsensorDataList, "EDA value")

        return binding.root

    }

    private fun createGraph(lineGraphView: GraphView, sensorDataList: ArrayList<Float>, s: String) {

        // Creazione di un array di DataPoint vuoto
        val dataPoints = ArrayList<DataPoint>()

        for (i in sensorDataList.indices) {
            dataPoints.add(DataPoint(i.toDouble(), sensorDataList[i].toDouble()))
        }

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(dataPoints.toTypedArray())

        // on below line adding animation
        lineGraphView.animate()

        // on below line we are setting scrollable
        // for point graph view
        lineGraphView.viewport.isScrollable = true

        // on below line we are setting scalable.
        lineGraphView.viewport.isScalable = true

        // on below line we are setting scalable y
        lineGraphView.viewport.setScalableY(true)

        // on below line we are setting scrollable y
        lineGraphView.viewport.setScrollableY(true)

        // on below line we are setting color for series.
        series.color = binding.root.resources.getColor(R.color.colorPrimary2)

        lineGraphView.title = s

        lineGraphView.addSeries(series)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}