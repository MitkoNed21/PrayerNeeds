package com.mitkoned.prayerneeds.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mitkoned.prayerneeds.data.model.PrayerNeed
import com.mitkoned.prayerneeds.R
import com.mitkoned.prayerneeds.databinding.LayoutRvPrayerNeedBinding
import java.text.SimpleDateFormat
import java.util.*

class PrayerNeedsAdapter (
//    private val prayerNeeds: MutableList<PrayerNeed>,
    private val listener: OnItemClickListener,
    private val itemRangeInsertedListener: OnItemRangeInsertedListener? = null
) : ListAdapter<PrayerNeed, PrayerNeedsAdapter.PrayerNeedViewHolder>(DiffCallback()) {
    inner class PrayerNeedViewHolder(
        private val binding: LayoutRvPrayerNeedBinding
    ) : RecyclerView.ViewHolder(binding.root) {
//        lateinit var foregroundView: ConstraintLayout

        init {
            binding.apply {
                root.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val prayerNeed = getItem(pos)
                        listener.onItemClick(prayerNeed)
                    }
                }

                cbAnswered.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val prayerNeed = getItem(pos)
                        listener.onCheckBoxClick(prayerNeed, cbAnswered.isChecked)
                    }
                }
            }
        }

        fun bind(prayerNeed: PrayerNeed) {
            binding.apply {
                cbAnswered.isChecked = prayerNeed.answered
                tvDesc.text = prayerNeed.description
                SimpleDateFormat("dd MMM|yyyy", Locale.US)
                    .format(prayerNeed.dateConverted)
                    .split('|')
                    .also { dateParts ->
                        tvDate.text = dateParts[0]
                        tvYear.text = dateParts[1]
                    }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerNeedViewHolder {
        val binding = LayoutRvPrayerNeedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        val viewHolder = PrayerNeedViewHolder(binding)

        this.registerAdapterDataObserver(
            DataObserver(itemRangeInsertedListener)
        )

        // TODO: idk????
        // viewHolder.foregroundView = view.findViewById(R.id.cl_prayerNeedForeground)
        return viewHolder
    }

    // TODO: idk???
//    override fun getItemCount(): Int {
//        return prayerNeeds.size
//    }

    override fun onBindViewHolder(holder: PrayerNeedViewHolder, position: Int) {
        try {
            val currentItem = getItem(position)

            holder.bind(currentItem)

            holder.itemView.findViewById<CheckBox>(R.id.cb_answered)
                .setOnCheckedChangeListener { _, isChecked ->
                if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                    // TODO: FIX
                    //[holder.adapterPosition].answered = isChecked

//                    if (hidingAnsweredPrayerNeeds.value && isChecked) {
//                        prayerNeeds.removeAt(holder.adapterPosition)
//                        this.notifyItemRemoved(holder.adapterPosition)
//                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(holder.itemView.context, e.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    interface OnItemClickListener {
        fun onItemClick(prayerNeed: PrayerNeed)
        fun onCheckBoxClick(prayerNeed: PrayerNeed, isChecked: Boolean)
    }

    interface OnItemRangeInsertedListener {
        fun onItemRangeInserted()
    }

    class DiffCallback : DiffUtil.ItemCallback<PrayerNeed>() {
        override fun areItemsTheSame(
            oldItem: PrayerNeed, newItem: PrayerNeed
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: PrayerNeed, newItem: PrayerNeed
        ) = oldItem == newItem
    }

    class DataObserver(
        private val onItemRangeInsertedCallback: OnItemRangeInsertedListener? = null
    ) : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            onItemRangeInsertedCallback?.onItemRangeInserted()
        }
    }
}