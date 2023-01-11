package com.app.signage91.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.signage91.databinding.FragmentAdbCommandsBinding
import java.io.BufferedReader
import java.io.InputStreamReader


class ADBCommandsFragment : Fragment() {

    private var _binding: FragmentAdbCommandsBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): ADBCommandsFragment {
            val fragment = ADBCommandsFragment()
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdbCommandsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            commandsEditText.setText("input keyevent 120")

            submitButton.setOnClickListener {
                if (commandsEditText.text.toString().isNotEmpty()) {
                    runAdbCommands(commandsEditText.text.toString())
                    //runShellCommand("input keyevent 27")
                } else {
                    commandsEditText.error = "This field can't be empty."
                }
            }
        }
    }

    private fun runAdbCommands(commandString: String) {
        try {
            //Runtime.getRuntime().exec(arrayOf(command))
            val process = Runtime.getRuntime().exec(commandString)
            val bufferedReader = BufferedReader(
                InputStreamReader(process.inputStream)
            )
            //val p = Runtime.getRuntime().exec(arrayOf("bash", "-l", "-c", "input keyevent 120"))
            /*val command = arrayOf("/bash" , "-c" , commandString)
            val p = Runtime.getRuntime().exec(command).waitFor()*/

            //Runtime.getRuntime().exec(commandString)


            /*val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            val cmd = "/system/bin/input keyevent 23\n"
            os.writeBytes(cmd)*/


            //Runtime.getRuntime().exec(commandString)


            /*val arrayCommand =
                arrayOf("sh", "-c", "dumpsys telephony.registry | grep \"permission\"")
            val r = Runtime.getRuntime()
            val process = r.exec(arrayCommand)*/

            //val stdoutString: String = convertInputStreamToString(process.inputStream)
            //val stderrString: String = convertInputStreamToString(process.errorStream)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT).show()
            Log.i("_ERROR", e.message.toString())
        }
    }

    fun runShellCommand(command: String) {
        // Run the command
        val process = Runtime.getRuntime().exec(command)
        val bufferedReader = BufferedReader(
            InputStreamReader(process.inputStream)
        )

        // Grab the results
        val log = StringBuilder()
        var line: String?
        line = bufferedReader.readLine()
        while (line != null) {
            log.append(line + "\n")
            line = bufferedReader.readLine()
        }
        val Reader = BufferedReader(
            InputStreamReader(process.errorStream)
        )

        // if we had an error during ex we get here
        val error_log = StringBuilder()
        var error_line: String?
        error_line = Reader.readLine()
        while (error_line != null) {
            error_log.append(error_line + "\n")
            error_line = Reader.readLine()
        }
        if (error_log.toString() != "")
            Log.i("ADB_COMMAND", "command : $command $log error $error_log")
        else
            Log.i("ADB_COMMAND", "command : $command $log")
    }
}