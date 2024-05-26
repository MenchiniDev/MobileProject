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

/**
 * DataTesting is a Fragment used for visualizing sensor data collected during a data collection session.
 * This fragment receives sensor data as arguments and plots them on different graphs using the GraphView library.
 *
 * The sensor data includes Heart Rate (HR), Photoplethysmography (PPG), Electrodermal Activity (EDA), and Electroencephalography (EEG) data.
 * Each type of sensor data is plotted on a separate graph, allowing the user to visualize the changes in sensor readings over time.
 *
 * The createGraph method is used to create a line graph for a given set of sensor data. It takes a GraphView, an ArrayList of sensor data, and a title for the graph as parameters.
 * This method creates a LineGraphSeries from the sensor data, configures the viewport and appearance of the graph, and adds the series to the graph.
 */

class DataTesting : Fragment() {

    private var _binding: FragmentDatatestingBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDatatestingBinding.inflate(inflater, container, false)

        //taking the sensor data from the arguments
        val HRsensorDataListString = arguments?.getStringArrayList("HRsensorDataList")
        val PPGsensorDataListString = arguments?.getStringArrayList("PPGsensorDataList")
        val EDAsensorDataListString = arguments?.getStringArrayList("EDAsensorDataList")

        // conversion of the ArrayList<String> in ArrayList<Float>
        val HRsensorDataList = HRsensorDataListString?.map { it.toFloat() } as ArrayList<Float>
        val PPGsensorDataList = PPGsensorDataListString?.map { it.toFloat() } as ArrayList<Float>
        val EDAsensorDataList = EDAsensorDataListString?.map { it.toFloat() } as ArrayList<Float>

        Log.d("EDAsensorDataList", EDAsensorDataList.toString())
        Log.d("PPGsensorDataList", PPGsensorDataList.toString())


        val EEGsensorDataList = ArrayList<Float>()

        //creating graph views for each sensor data
        val lineGraphView1 = binding.idGraphView1
        val lineGraphView2 = binding.idGraphView2
        val lineGraphView3 = binding.idGraphView3

        val lineGraphView4 = binding.idGraphView4
        val lineGraphView5 = binding.idGraphView5
        val lineGraphView6 = binding.idGraphView6
        val lineGraphView7 = binding.idGraphView7
        val lineGraphView8 = binding.idGraphView8
        val lineGraphView9 = binding.idGraphView9

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

        //lets plot all different values of channel 5
        for (i in 0 until MainActivity.EEGsensordataList.size) {
            EEGsensorDataList.add(MainActivity.EEGsensordataList[i].channel5.toFloat())
        }
        createGraph(lineGraphView8,EEGsensorDataList, "CHANNEL 5")

        //lets plot all different values of channel 6
        for (i in 0 until MainActivity.EEGsensordataList.size) {
            EEGsensorDataList.add(MainActivity.EEGsensordataList[i].channel6.toFloat())
        }
        createGraph(lineGraphView9,EEGsensorDataList, "CHANNEL 6")

        createGraph(lineGraphView1,HRsensorDataList, "Hearth Rate")
        createGraph(lineGraphView2,PPGsensorDataList, "PPG value")
        createGraph(lineGraphView3,EDAsensorDataList, "EDA value")

        return binding.root

    }

    private fun createGraph(lineGraphView: GraphView, sensorDataList: ArrayList<Float>, s: String) {

        // creating a list of data points
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
        series.color = binding.root.resources.getColor(R.color.colorPrimary)
        val gridLabelRenderer = lineGraphView.gridLabelRenderer
        gridLabelRenderer.gridColor = binding.root.resources.getColor(R.color.screen_background_color2)

        lineGraphView.title = s
        lineGraphView.titleColor = binding.root.resources.getColor(R.color.screen_background_color2)

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