package com.wang.wqrcode

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Author: wangxiaojie6
 * Date: 2018/5/23
 */
class ResultAdapter(private val mResults: ArrayList<String>) : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_result, parent, false)
        return ResultViewHolder(itemView)
    }



    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.mResultTV.text = mResults[position]
    }

    override fun getItemCount(): Int {
        return mResults.size
    }

    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val mResultTV = itemView.findViewById<TextView>(R.id.result_tv)

    }
}