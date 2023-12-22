package com.example.uas_papb

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.uas_papb.data.Item
import com.google.firebase.firestore.core.View

class DataListAdapter(private val mList: List<Item>) : RecyclerView.Adapter<DataListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item_holder, parent, false)
        return ViewHolder(view).listen { datas, _ ->
            val item = mList[datas]
            onClick(parent, item)
        }
    }

    override fun onBindViewHolder(holder: DataListAdapter.ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: Item) {
            val imageView: ImageView = itemView.findViewById(R.id.iv_movImage_user)
            val movieName: TextView = itemView.findViewById(R.id.tv_movName_user)
            val movieRating: TextView = itemView.findViewById(R.id.tv_movRat_user)
            movieName.text = data.name
            movieRating.text = data.rating.toString()
            Glide.with(itemView).load(Uri.parse(data.image)).override(350,120).into(imageView)
        }
    }

    private fun <T: RecyclerView.ViewHolder> T.listen(event: (position: Int, Type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    private fun onClick(parent: ViewGroup, item: Item) {
        val intent = Intent(parent.context.applicationContext, DetailMovie::class.java)
        intent.putExtra("id", item.id)
        intent.putExtra("name", item.name)
        intent.putExtra("author", item.author)
        intent.putExtra("storyline", item.storyline)
        intent.putExtra("tag", item.tag)
        intent.putExtra("image", item.image)
        intent.putExtra("rating", item.rating)
        parent.context.startActivity(intent)
    }
}