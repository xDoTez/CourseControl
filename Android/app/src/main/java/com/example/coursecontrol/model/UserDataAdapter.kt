// UserDataAdapter.kt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.R
import com.example.coursecontrol.model.Course
import com.example.coursecontrol.model.CourseData

class UserDataAdapter(
    private val courseDataList: List<CourseData>,
    private val onItemClick: (CourseData) -> Unit
) : RecyclerView.Adapter<UserDataAdapter.ViewHolder>() {

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

        fun bind(courseData: CourseData) {
            textCourseName.text = courseData.course.name
            textSemester.text = "Semester: ${courseData.course.semester}"
            textEctsPoints.text = "ECTS Points: ${courseData.course.ects}"

            textCourseName.setOnClickListener {
                onItemClick(courseData)
            }
        }
    }

}
