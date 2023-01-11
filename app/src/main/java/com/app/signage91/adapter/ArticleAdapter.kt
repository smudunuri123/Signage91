package com.app.signage91.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.signage91.TEXT_VIEW_SCROLLING_SPPED
import com.app.signage91.components.TextViewComponent
import com.app.signage91.databinding.AdapterArticleBinding
import com.app.signage91.models.ArticleListener
import com.app.signage91.models.xml_parser.Article
import com.bumptech.glide.Glide

class ArticleAdapter(
    var context: Context,
    var articleList: ArrayList<Article>,
    var url: String,
    var articleListener: ArticleListener
) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AdapterArticleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val article = articleList.get(position)
        holder.bindView(article, articleListener)
    }

    override fun getItemCount(): Int {
        return articleList.size
    }

    inner class ViewHolder(var binding: AdapterArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(article: Article, articleListener: ArticleListener) {
            binding.apply {
                /*val textViewComponent = TextViewComponent(
                    context
                ).apply {
                    layoutParams =
                        (binding.itemLayout.layoutParams as RecyclerView.LayoutParams).apply {
                            height = ViewGroup.LayoutParams.WRAP_CONTENT
                            width = ViewGroup.LayoutParams.MATCH_PARENT
                        }
                    setDirection(TextViewComponent.Direction.LEFT)
                    text = article.title
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                    textSize = 20f
                    background =
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.textview_background
                        )
                    setDelayed(0)
                    setSpeed(TEXT_VIEW_SCROLLING_SPPED.LOW.value)
                }
                textViewComponent.setOnClickListener {
                    textViewComponent.startMarquee()
                }
                binding.itemLayout.addView(textViewComponent)

                textViewComponent.setDirection(TextViewComponent.Direction.LEFT)
                textViewComponent.startMarquee()*/


                titleTextView.apply {
                    text = article.title?.trim()
                    if (bindingAdapterPosition % 2 == 0) {

                        if (url.isNotEmpty()) {
                            Glide.with(this).load(url).into(imageView1);
                            imageView1.visibility = View.VISIBLE
                            imageView2.visibility = View.GONE
                        } else {
                            imageView1.visibility = View.GONE
                            imageView2.visibility = View.GONE
                        }
                        setDirection(TextViewComponent.Direction.LEFT)
                        setSpeed(TEXT_VIEW_SCROLLING_SPPED.HIGH.value)
                    } else {

                        if (url.isNotEmpty()) {
                            imageView1.visibility = View.GONE
                            imageView2.visibility = View.VISIBLE
                            Glide.with(this).load(url).into(imageView2);
                        } else {
                            imageView1.visibility = View.GONE
                            imageView2.visibility = View.GONE
                        }
                        setDirection(TextViewComponent.Direction.RIGHT)
                        setSpeed(TEXT_VIEW_SCROLLING_SPPED.LOW.value)
                    }
                    setDelayed(0)
                    //startMarquee()
                }

                //linkTextView.text = article.link

                root.setOnClickListener {
                    articleListener.onItemClicked(article, bindingAdapterPosition)
                }
            }
        }
    }
}