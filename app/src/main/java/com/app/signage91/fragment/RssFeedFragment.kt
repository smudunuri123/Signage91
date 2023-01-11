package com.app.signage91.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.signage91.TEXT_VIEW_SCROLLING_SPPED
import com.app.signage91.adapter.ArticleAdapter
import com.app.signage91.app.MyApplication
import com.app.signage91.components.TextViewComponent
import com.app.signage91.databinding.FragmentRssFeedBinding
import com.app.signage91.models.xml_parser.Article
import com.app.signage91.utils.retrofit.RssFeedApiService
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RssFeedFragment : Fragment() {

    lateinit var mApiService: RssFeedApiService
    private var _binding: FragmentRssFeedBinding? = null
    private val binding get() = _binding

    private var articleList = ArrayList<Article>()
    private var articleAdapter: ArticleAdapter? = null
    private var url = ""

    companion object {
        fun newInstance(): RssFeedFragment {
            return RssFeedFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRssFeedBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mApiService = (activity?.application as MyApplication).rssFeedApiService

        getAllFeeds()
    }

    private fun getAllFeeds() {
        binding?.progressCircleIndeterminate?.show()
        CoroutineScope(Dispatchers.IO).launch {
            val response = mApiService.getFeed("http://timesofindia.indiatimes.com/rssfeeds/-2128936835.cms")
            withContext(Dispatchers.Main) {
                binding?.progressCircleIndeterminate?.hide()
                //Log.i("__GSON", GsonBuilder().setPrettyPrinting().create().toJson(response))
                if (response.articleList != null) {
                    url = response.feedImage?.url ?: ""
                    articleList.addAll(response.articleList!!)
                    //articleAdapter?.notifyDataSetChanged()
                }
                //setUpArticlesRecyclerView()


                var wholeData = ""
                for (i in articleList.indices) {
                    wholeData = wholeData + articleList.get(i).title
                }


                binding?.apply {
                    titleTextView.apply {
                        text = wholeData
                        if (url.isNotEmpty()) {
                            Glide.with(this).load(url).into(imageView1);
                            imageView1.visibility = View.VISIBLE
                            imageView2.visibility = View.GONE
                        } else {
                            imageView1.visibility = View.GONE
                            imageView2.visibility = View.GONE
                        }
                        setDirection(TextViewComponent.Direction.LEFT)
                        setSpeed(TEXT_VIEW_SCROLLING_SPPED.MEDIUM.value)
                        setDelayed(0)
                        //startMarquee()
                    }
                }

            }
        }
    }

    /*private fun setUpArticlesRecyclerView() {
        articleAdapter =
            ArticleAdapter(requireContext(), articleList, url, object : ArticleListener {
                override fun onItemClicked(account: Article, position: Int) {

                }
            })
        binding.feedsRecyclerView.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }*/

}