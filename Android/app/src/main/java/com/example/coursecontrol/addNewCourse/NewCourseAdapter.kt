package com.example.coursecontrol.addNewCourse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.R
import hr.foi.air.core.model.Course

class NewCourseAdapter (
    private val courseDataList: List<Course>,
    private val onItemClick: (Course) -> Unit
) : RecyclerView.Adapter<NewCourseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val courseData = courseDataList[position]
        holder.bind(courseData)
    }

    override fun getItemCount(): Int {
        return courseDataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textCourseName: TextView = itemView.findViewById(R.id.textCourseName)
        private val textSemester: TextView = itemView.findViewById(R.id.textSemester)
        private val textEctsPoints: TextView = itemView.findViewById(R.id.textEctsPoints)

        fun bind(courseData: Course) {
            textCourseName.text = courseData.name
            textSemester.text = "Semester: ${courseData.semester}"
            textEctsPoints.text = "ECTS Points: ${courseData.ects}"

            textCourseName.setOnClickListener {
                onItemClick(courseData)
            }
        }
    }

}