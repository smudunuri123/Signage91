package com.app.signage91.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.signage91.CommunicationServiceGrpc
import com.app.signage91.Request
import com.app.signage91.Response
import com.app.signage91.databinding.FragmentGrpcBinding
import com.google.gson.GsonBuilder
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*
import java.io.PrintWriter
import java.io.StringWriter

@OptIn(DelicateCoroutinesApi::class)
class GrpcFragment : Fragment() {
    private var _binding: FragmentGrpcBinding? = null
    private val binding get() = _binding

    companion object {
        val GRPC_HOST = "10.0.2.2"
        val GRPC_PORT = "50051"
        /*val GRPC_HOST = "192.168.1.86"
        val GRPC_PORT = "8080"*/
        val GRPC_BASE_URL = "$GRPC_HOST:$GRPC_PORT"

        fun newInstance(): GrpcFragment {
            val fragment = GrpcFragment()
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGrpcBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getGrpcData(GRPC_HOST, GRPC_PORT, "This is the test.")
    }

    private fun getGrpcData(host: String, portStr: String, message: String) {
        binding?.progressCircleIndeterminate?.show()
        binding?.titleTextView?.visibility = View.GONE
        GlobalScope.launch {
            var reply: Response? = null
            try {
                val port = if (portStr.isEmpty()) 0 else portStr.toInt()
                val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
                //val channel = ManagedChannelBuilder.forTarget(GRPC_BASE_URL).usePlaintext().build()
                val stub = CommunicationServiceGrpc.newBlockingStub(channel)
                val request = Request.newBuilder().apply {
                    mediaPlayerCode = message
                }
                reply = stub.communicate(request.build())
            } catch (e: Exception) {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                e.printStackTrace(pw)
                pw.flush()
                "Call failed: $sw"
                Log.i("__CALL_FAILED", "Call failed: $sw")
            }

            withContext(Dispatchers.Main) {
                binding?.progressCircleIndeterminate?.hide()
                binding?.titleTextView?.visibility = View.VISIBLE
                binding?.titleTextView?.text = reply?.text ?: ""
                //binding.titleTextView.text = GsonBuilder().setPrettyPrinting().create().toJson(reply)
            }
        }
    }

}