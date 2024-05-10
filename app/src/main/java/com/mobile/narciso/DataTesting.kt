package com.mobile.narciso

import android.os.Bundle
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

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDatatestingBinding.inflate(inflater, container, false)

        val HRsensorDataListString = arguments?.getStringArrayList("HRsensorDataList")
        val PPGsensorDataListString = arguments?.getStringArrayList("PPGsensorDataList")

        // Conversione dell'ArrayList<String> in ArrayList<Float>
        val HRsensorDataList = HRsensorDataListString?.map { it.toFloat() } as ArrayList<Float>
        val PPGsensorDataList = PPGsensorDataListString?.map { it.toFloat() } as ArrayList<Float>


        //TODO display all plots of data collected during dataCollection session
        val lineGraphView1 = binding.idGraphView1
        val lineGraphView2 = binding.idGraphView2

        createGraph(lineGraphView1,HRsensorDataList)
        createGraph(lineGraphView2,PPGsensorDataList)

        return binding.root

    }

    private fun createGraph(lineGraphView: GraphView, sensorDataList: ArrayList<Float>) {


        // on below line we are adding data to our graph view.
        /*val series: LineGraphSeries<DataPoint> = LineGraphSeries(
            arrayOf(
                // on below line we are adding
                // each point on our x and y axis.
                DataPoint(0.0, 1.0),
                DataPoint(1.0, 3.0),
                DataPoint(2.0, 4.0),
                DataPoint(3.0, 9.0),
                DataPoint(4.0, 6.0),
                DataPoint(5.0, 3.0),
                DataPoint(6.0, 6.0),
                DataPoint(7.0, 1.0),
                DataPoint(8.0, 2.0)
            )
        )*/

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

        lineGraphView.addSeries(series)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*binding.gotoDataCollection.setOnClickListener {
            Toast.makeText(requireContext(), "sto andando a data collection!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_DataTesting_to_DataCollection)
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}