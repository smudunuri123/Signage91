package com.app.signage91.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.signage91.app.MyApplication
import com.app.signage91.databinding.FragmentRestApiBinding
import com.app.signage91.utils.retrofit.ApiService
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RESTApiFragment : Fragment() {

    lateinit var mApiService: ApiService
    private var _binding: FragmentRestApiBinding? = null
    private val binding get() = _binding

    companion object {
        fun newInstance(): RESTApiFragment {
            return RESTApiFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRestApiBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mApiService = (activity?.application as MyApplication).apiService

        getUsers()
        //addUser()
    }

    private fun addUser() {
        binding?.apply {
            progressCircleIndeterminate.show()
            responseTextView.visibility = View.GONE
        }
        CoroutineScope(Dispatchers.IO).launch {
            val response = mApiService.addUser()
            withContext(Dispatchers.Main) {
                binding?.apply {
                    progressCircleIndeterminate.hide()
                    responseTextView.visibility = View.VISIBLE
                    responseTextView.text =
                        GsonBuilder().setPrettyPrinting().create().toJson(response)
                }
            }
        }
    }

    private fun getUsers() {
        binding?.apply {
            progressCircleIndeterminate.show()
            responseTextView.visibility = View.GONE
        }
        CoroutineScope(Dispatchers.IO).launch {
            val response = mApiService.getUser()
            withContext(Dispatchers.Main) {
                binding?.apply {
                    progressCircleIndeterminate.hide()
                    responseTextView.visibility = View.VISIBLE
                    responseTextView.text =
                        GsonBuilder().setPrettyPrinting().create().toJson(response)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}