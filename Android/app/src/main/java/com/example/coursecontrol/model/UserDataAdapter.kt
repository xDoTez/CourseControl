package com.example.coursecontrol.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.R

class UserDataAdapter(private val courseDataList: List<CourseData>) :
    RecyclerView.Adapter<UserDataAdapter.UserDataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserDataViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)

        return UserDataViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserDataViewHolder, position: Int) {
        val courseData = courseDataList[position]
        holder.bind(courseData)
    }

    override fun getItemCount(): Int {
        return courseDataList.size
    }

    inner class UserDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val courseNameTextView: TextView = itemView.findViewById(R.id.textCourseName)
        private val semesterEctsTextView: TextView = itemView.findViewById(R.id.textSemesterEcts)

        fun bind(courseData: CourseData) {
            val course = courseData.course

            courseNameTextView.text = course.name
            semesterEctsTextView.text = "Semester: ${course.semester}, ECTS: ${course.ects}"
        }
    }
}
