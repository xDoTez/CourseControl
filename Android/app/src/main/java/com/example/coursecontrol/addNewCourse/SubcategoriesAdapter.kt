package com.example.coursecontrol.addNewCourse


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.model.NewSubcategory
import com.example.coursecontrol.R.layout.item_new_subcategory
import com.example.coursecontrol.R.id.txtNewSubcategoryName
import com.example.coursecontrol.R.id.txtNewSubcategoryPoints
import com.example.coursecontrol.R.id.txtNewSubcategoryRequirements


class SubcategoriesAdapter(private val innerItems: List<NewSubcategory>) :
    RecyclerView.Adapter<SubcategoriesAdapter.InnerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(item_new_subcategory, parent, false)
        return InnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        val innerItem = innerItems[position]
        holder.bind(innerItem)
    }

    override fun getItemCount(): Int {
        return innerItems.size
    }

    inner class InnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(txtNewSubcategoryName)
        private val points: TextView = itemView.findViewById(txtNewSubcategoryPoints)
        private val requirements: TextView = itemView.findViewById(txtNewSubcategoryRequirements)

        fun bind(innerItem: NewSubcategory) {
            name.text = innerItem.name
            points.text = innerItem.points.toString()
            requirements.text = innerItem.requirements.toString()
        }
    }
}