package com.app.signage91.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.signage91.R
import com.app.signage91.TEXT_VIEW_SCROLLING_SPPED
import com.app.signage91.components.TextViewComponent
import com.app.signage91.databinding.FragmentTestTextBinding

class TestTextViewFragment : Fragment() {

    private var _binding: FragmentTestTextBinding? = null
    private val binding get() = _binding

    private var textViewComponent: TextViewComponent? = null

    companion object {
        fun newInstance(): TestTextViewFragment {
            val fragment = TestTextViewFragment()
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTestTextBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textFromResources = getString(R.string.lorem_ipsume)
        var finalText = ""
        val textArray = textFromResources.split("")
        for (i in textArray.indices){
            val text = textArray[i]
            finalText = finalText + text + "\n"
        }

        textViewComponent = TextViewComponent(
            requireContext()
        ).apply {
            layoutParams =
                (binding?.mainLayout?.layoutParams as LinearLayoutCompat.LayoutParams).apply {
                    height = 500
                    width = MATCH_PARENT
                }
            setDirection(TextViewComponent.Direction.LEFT)
            //text = getString(R.string.lorem_ipsume)
            text = finalText
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            textSize = 20f
            background =
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.textview_background
                )
            setDelayed(0)
            setSpeed(TEXT_VIEW_SCROLLING_SPPED.LOW.value)
        }
        binding?.mainLayout?.addView(textViewComponent)

        binding?.apply {
            lowButton.setOnClickListener {
                textViewComponent?.setSpeed(TEXT_VIEW_SCROLLING_SPPED.LOW.value)
            }
            mediumButton.setOnClickListener {
                textViewComponent?.setSpeed(TEXT_VIEW_SCROLLING_SPPED.MEDIUM.value)
            }
            highButton.setOnClickListener {
                textViewComponent?.setSpeed(TEXT_VIEW_SCROLLING_SPPED.HIGH.value)
            }
            leftToRightButton.setOnClickListener {
                textViewComponent?.setDirection(TextViewComponent.Direction.LEFT)
                //textViewComponent?.rotation = 0f
            }
            rightToLeftButton.setOnClickListener {
                textViewComponent?.setDirection(TextViewComponent.Direction.RIGHT)
                //textViewComponent?.rotation = 0f
            }
            upToDownDirectionButton.setOnClickListener {
                textViewComponent?.setDirection(TextViewComponent.Direction.UP)
                //textViewComponent?.rotation = -90f
            }
            downToUpDirectionButton.setOnClickListener {
                textViewComponent?.setDirection(TextViewComponent.Direction.DOWN)
                //textViewComponent?.rotation = -270f
            }

            /*marqueeTextView.apply {
                isSelected = true
            }*/
            //marqueeTextView.text = finalText

            //marqueeTextView.text = finalText
        }
    }
}