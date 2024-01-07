package com.example.coursecontrol.addNewCourse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.R
import com.example.coursecontrol.model.Program

class ProgramAdapter (
    private val programDataList: List<Program>,
    private val onItemClick: (Program) -> Unit
    ) : RecyclerView.Adapter<ProgramAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_program, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val programData = programDataList[position]
            holder.bind(programData)
        }

        override fun getItemCount(): Int {
            return programDataList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textProgramName: TextView = itemView.findViewById(R.id.textProgramName)

            fun bind(program: Program) {
                textProgramName.text = program.name

                textProgramName.setOnClickListener {
                    onItemClick(program)
                }
            }
        }
}