package com.example.coursecontrol.addNewCourse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.R
import com.example.coursecontrol.model.NewCategory

class CategoriesAdapter(private val outerItems: List<NewCategory>) :
    RecyclerView.Adapter<CategoriesAdapter.OuterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OuterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_new_category, parent, false)
        return OuterViewHolder(view)
    }

    override fun onBindViewHolder(holder: OuterViewHolder, position: Int) {
        val outerItem = outerItems[position]
        holder.bind(outerItem)
    }

    override fun getItemCount(): Int {
        return outerItems.size
    }

    inner class OuterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.txtNewCategoryName)
        private val points: TextView = itemView.findViewById(R.id.txtNewCategoryPoints)
        private val requirements: TextView = itemView.findViewById(R.id.txtNewCategoryRequirements)
        private val txtSubcategoriesList: TextView = itemView.findViewById(R.id.txtSubcategoriesList)
        private val innerRecyclerView: RecyclerView = itemView.findViewById(R.id.subcategoriesRecyclerView)

        fun bind(outerItem: NewCategory) {
            name.text = outerItem.name
            points.text = outerItem.points.toString()
            requirements.text = outerItem.requirements.toString()

            if(outerItem.subcategories.isNullOrEmpty()){
                txtSubcategoriesList.text = ""
            }

            if(!outerItem.subcategories.isNullOrEmpty()){
                val innerAdapter = SubcategoriesAdapter(outerItem.subcategories)
                innerRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
                innerRecyclerView.adapter = innerAdapter
            }
        }
    }
}