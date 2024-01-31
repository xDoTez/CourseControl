import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.R
import hr.foi.air.core.model.CategoryData

class CategoryAdapter(private val categories: List<CategoryData>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        val categoryRequirements: TextView = itemView.findViewById(R.id.categoryRequirements)
        val subcategoriesLayout: ViewGroup = itemView.findViewById(R.id.subcategoriesLayout)
        val userPoints: TextView = itemView.findViewById(R.id.userPoints)
        val totalCategoryPoints: TextView = itemView.findViewById(R.id.totalCategoryPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryData = categories[position]
        val category = categoryData.category

        holder.categoryName.text = category.name
        holder.categoryRequirements.text = "Requirement: ${category.requirements} points"

        holder.subcategoriesLayout.removeAllViews()

        for (subcategoryData in categoryData.subcategories.orEmpty()) {
            val subcategoryView =
                LayoutInflater.from(holder.subcategoriesLayout.context)
                    .inflate(R.layout.subcategory_item, holder.subcategoriesLayout, false)

            val subcategoryName: TextView = subcategoryView.findViewById(R.id.subcategoryName)
            val subcategoryPoints: TextView = subcategoryView.findViewById(R.id.subcategoryPoints)
            val subcategoryUserPoints: TextView = subcategoryView.findViewById(R.id.subcategoryUserPoints)
            val subcategoryRequirements: TextView =
                subcategoryView.findViewById(R.id.subcategoryRequirements)

            subcategoryName.text = subcategoryData.subcategory.name
            subcategoryPoints.text = "Points: ${subcategoryData.subcategory.points}"
            subcategoryRequirements.text =
                "Requirement: ${subcategoryData.subcategory.requirements} points"
            subcategoryUserPoints.text ="Achieved: ${subcategoryData.subcategoryUserData.points}"

            holder.subcategoriesLayout.addView(subcategoryView)
        }

        holder.userPoints.text = "Total Points Achieved: ${categoryData.categoryUserData.points}"
        val totalCategoryPoints = categoryData.category.points
        holder.totalCategoryPoints.text = "Total Points: $totalCategoryPoints"
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}
