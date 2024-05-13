package com.mobile.narciso

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

        Log.d("SONO QUI","SONO QUI 3")

        // Conversione dell'ArrayList<String> in ArrayList<Float>
        val HRsensorDataList = HRsensorDataListString?.map { it.toFloat() } as ArrayList<Float>
        val PPGsensorDataList = PPGsensorDataListString?.map { it.toFloat() } as ArrayList<Float>

        Toast.makeText(requireContext(), "SONO QUI", Toast.LENGTH_SHORT).show()
        Log.d("SONO QUI","SONO QUI 1")


        //TODO display all plots of data collected during dataCollection session
        val lineGraphView1 = binding.idGraphView1
        val lineGraphView2 = binding.idGraphView2

        Log.d("SONO QUI","SONO QUI 2")
        createGraph(lineGraphView1,HRsensorDataList)
        createGraph(lineGraphView2,PPGsensorDataList)
        Log.d("SONO QUI","SONO QUI 4")

        return binding.root

    }

    private fun createGraph(lineGraphView: GraphView, sensorDataList: ArrayList<Float>) {

        // Creazione di un array di DataPoint vuoto
        val dataPoints = ArrayList<DataPoint>()

        for (i in sensorDataList.indices) {
            dataPoints.add(DataPoint(i.toDouble(), sensorDataList[i].toDouble()))
        }

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(dataPoints.toTypedArray())

        // on below line adding animation
        lineGraphView.animate()

        Log.d("SONO QUI","SONO QUI 5")

        // on below line we are setting scrollable
        // for point graph view
        lineGraphView.viewport.isScrollable = true

        // on below line we are setting scalable.
        lineGraphView.viewport.isScalable = true

        // on below line we are setting scalable y
        lineGraphView.viewport.setScalableY(true)

        // on below line we are setting scrollable y
        lineGraphView.viewport.setScrollableY(true)

        Log.d("SONO QUI","SONO QUI 6")

        // on below line we are setting color for series.
        series.color = binding.root.resources.getColor(R.color.colorPrimary2)

        lineGraphView.addSeries(series)
        Log.d("SONO QUI","SONO QUI 7")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*binding.gotoDataCollection.setOnClickListener {
            Toast.makeText(requireContext(), "sto andando a data collection!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_DataTesting_to_DataCollection)
        }*/

        Log.d("SONO QUI","SONO QUI 8")
    }

    override fun onDestroyView() {
        Log.d("SONO QUI","SONO QUI 9")
        super.onDestroyView()
        _binding = null
    }
}