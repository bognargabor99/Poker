package hu.bme.aut.onlab.poker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.onlab.poker.databinding.ItemResultBinding
import hu.bme.aut.onlab.poker.model.WinningPlayer
import hu.bme.aut.onlab.poker.network.PokerClient

class ResultsAdapter : ListAdapter<WinningPlayer, ResultsAdapter.ViewHolder>(RunItemCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = getItem(position)
        holder.result = result

        holder.binding.tvName.text = if (result.userName == PokerClient.userName) "You" else "Opponent"
        holder.binding.tvWinAmount.text = result.winAmount.toString()
        if (result.hand != null) {
            holder.binding.tvHandType.visibility = View.VISIBLE
            holder.binding.handCard1.isUpside = true
            holder.binding.handCard2.isUpside = true
            holder.binding.handCard1.value = result.inHandCards?.first()?.value!!
            holder.binding.handCard1.symbol = result.inHandCards.first().suit.ordinal
            holder.binding.handCard2.value = result.inHandCards.last().value
            holder.binding.handCard2.symbol = result.inHandCards.last().suit.ordinal
            holder.binding.tvHandType.text = result.hand.type.asString
        }
    }

    companion object{
        object RunItemCallback : DiffUtil.ItemCallback<WinningPlayer>(){
            override fun areItemsTheSame(oldItem: WinningPlayer, newItem: WinningPlayer): Boolean {
                return oldItem.userName == newItem.userName
            }

            override fun areContentsTheSame(oldItem: WinningPlayer, newItem: WinningPlayer): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(val binding: ItemResultBinding) : RecyclerView.ViewHolder(binding.root) {
        var result: WinningPlayer? = null
    }
}