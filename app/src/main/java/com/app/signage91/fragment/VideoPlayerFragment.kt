package com.app.signage91.fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import com.app.signage91.databinding.FragmentVideoPlayerBinding
import com.app.signage91.models.ImageViewModel


class VideoPlayerFragment : Fragment() {

    private var _binding: FragmentVideoPlayerBinding? = null
    private val binding get() = _binding
    private var url = ""
    private var stopPosition = 0

    companion object {
        fun newInstance(url: String): VideoPlayerFragment {
            val fragment = VideoPlayerFragment()
            fragment.url = url
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideSystemUi()

        val mediacontroller = MediaController(requireContext())
        //mediacontroller.setAnchorView(binding.videoView)
        mediacontroller.hide()
        mediacontroller.visibility = View.GONE

        binding?.apply {
            videoView.setMediaController(mediacontroller)
            videoView.setVideoURI(Uri.parse(url))
            videoView.requestFocus()

            videoView.setOnPreparedListener { mp ->
                mp.setOnVideoSizeChangedListener { mp, width, height ->
                    videoView.setMediaController(mediacontroller)
                    mediacontroller.setAnchorView(videoView)
                }
            }
            videoView.setOnCompletionListener { mp ->
                Toast.makeText(
                    requireContext(),
                    "Video over",
                    Toast.LENGTH_SHORT
                ).show()
                mp.release()
                Toast.makeText(
                    requireContext(),
                    "Videos completed",
                    Toast.LENGTH_SHORT
                ).show()
                /*if (index++ === arrayList.size) {
                    index = 0
                    mp.release()
                    Toast.makeText(
                        requireContext(),
                        "Videos completed",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    videoView.setVideoURI(Uri.parse(arrayList.get(index)))
                    videoView.start()
                }*/
            }
            videoView.setOnErrorListener { mp, what, extra ->
                Log.d("API123", "What $what extra $extra")
                false
            }
            videoView.start()

            //makeVideoViewFullScreen()


            /*val controller = MediaController(requireContext())
            controller.requestLayout()
            controller.bringChildToFront(videoView)
            controller.
            controller.hide()
            controller.isVisible = false
            controller.visibility = GONE

            videoView.setMediaController(controller)*/

            videoView.setMediaController(null)

            //makeVideoViewFullScreen()
        }

//        binding.relativeLayout.apply {
//            val imageViewModel : ImageViewModel = getImageModel()
//            val imageViewComponent : ImageViewComponent? =
//                context?.let { it1 -> ImageViewComponent(it1, null, imageViewModel) }
//                imageViewComponent?.let { imageViewComponent ->
//                    addView(imageViewComponent)
//            }
//        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        binding?.videoView?.let {
            WindowInsetsControllerCompat(
                requireActivity().window,
                it
            ).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun getImageModel(): ImageViewModel {
        return ImageViewModel(
            "https://image.tmdb.org/t/p/w92/dRLSoufWtc16F5fliK4ECIVs56p.jpg",
            0.0,
            0.0,
            false,
            "centercrop",
            "test.jpg"
        )
    }

    private fun makeVideoViewFullScreen() {
        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
        val params = binding?.videoView?.layoutParams as ConstraintLayout.LayoutParams
        params.width = metrics.widthPixels
        params.height = metrics.heightPixels
        params.leftMargin = 0
        binding?.videoView?.layoutParams = params
    }

    private fun removeVideoViewFullScreen() {
        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
        val params = binding?.videoView?.layoutParams as ConstraintLayout.LayoutParams
        params.width = (300 * metrics.density).toInt()
        params.height = (250 * metrics.density).toInt()
        params.leftMargin = 30
        binding?.videoView?.setLayoutParams(params)
    }

    override fun onResume() {
        super.onResume()
        binding?.videoView?.seekTo(stopPosition)
        binding?.videoView?.start()
    }

    override fun onPause() {
        super.onPause()
        stopPosition = binding?.videoView?.currentPosition!!
        binding?.videoView?.pause()
    }
}