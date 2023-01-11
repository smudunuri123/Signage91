package com.app.signage91.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.signage91.RSSFeedCompoundSettingsModel
import com.app.signage91.components.RSSFeedViewCompoundComponents
import com.app.signage91.databinding.FragmentCompoundRssFeedBinding

class RssFeedComponentFragment : Fragment() {

    private var _binding: FragmentCompoundRssFeedBinding? = null
    private val binding get() = _binding

    companion object {
        fun newInstance(): RssFeedComponentFragment {
            val fragment = RssFeedComponentFragment()
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCompoundRssFeedBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.mainLayout?.apply {
            addView(
                RSSFeedViewCompoundComponents(
                    requireContext(),
                    null,
                    RSSFeedCompoundSettingsModel(
                        rssFeedUrl = "http://timesofindia.indiatimes.com/rssfeeds/-2128936835.cms",
                        width =0.0,
                        height = 800.0,
                        slidingDuration = 5000
                    )
                )
            )
        }
    }
}