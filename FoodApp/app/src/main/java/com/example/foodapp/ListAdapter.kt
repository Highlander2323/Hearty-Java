package com.example.foodapp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.sql.Connection

open class ListAdapter(var context: Context?, list: List<Map<String?, String?>>, private val option: Int) : BaseAdapter() {
    var connection: Connection? = null
    lateinit var data: Array<Array<String?>>
    var inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        when (option) {
            RECIPES_IN_COOKBOOK, RECIPE_SEARCH -> {
                data = Array(list.size) { arrayOfNulls(3) }
                var i = 0
                while (i < list.size) {
                    data[i][0] = list[i]["Id"]
                    data[i][1] = list[i]["Name"]
                    data[i][2] = list[i]["Cook Time"]
                    i++
                }
            }

            ADDED_INGREDIENTS, SEARCHED_INGREDIENTS, INGREDIENT_SEARCH -> {
                data = Array(list.size) { arrayOfNulls(2) }
                var i = 0
                while (i < list.size) {
                    data[i][0] = list[i]["Id"]
                    data[i][1] = list[i]["Name"]
                    i++
                }
            }

            INGREDIENT_ADDED -> {
                data = Array(list.size) { arrayOfNulls(3) }
                var i = 0
                while (i < list.size) {
                    data[i][0] = list[i]["Id"]
                    data[i][1] = list[i]["Name"]
                    data[i][2] = list[i]["Amount"]
                    i++
                }
            }

            ADD_TO_COOKBOOK, COOKBOOKS -> {
                data = Array(list.size) { arrayOfNulls(2) }
                var i = 0
                while (i < list.size) {
                    data[i][0] = list[i]["IdCookbook"]
                    data[i][1] = list[i]["NameCookbook"]
                    i++
                }
            }

            SEARCHED_DIETS -> {
                data = Array(list.size) { arrayOfNulls(2) }
                var i = 0
                while (i < list.size) {
                    data[i][0] = list[i]["IdDiet"]
                    data[i][1] = list[i]["NameDiet"]
                    i++
                }
            }

            RECIPE_PAGE_INGREDIENTS -> {
                data = Array(list.size) { arrayOfNulls(3) }
                var i = 0
                while (i < list.size) {
                    data[i][0] = list[i]["IdIngredient"]
                    data[i][1] = list[i]["NameIngredient"]
                    data[i][2] = list[i]["AmountIngredient"]
                    i++
                }
            }
        }
    }

    constructor(ctx: Context?, list: List<Map<String?, String?>>, opt: Int, con: Connection?) : this(ctx, list, opt) {
        connection = con
    }

    protected fun isAdded(id: String?): Boolean {
        for (i in CreateRecipe.idIngredients.indices) {
            if (CreateRecipe.idIngredients[i]["Id"] === id) {
                return true
            }
        }
        return false
    }

    protected fun isAddedToSearchedIngredients(id: String?): Boolean {
        for (i in SearchByIngredients.idIngredients.indices) {
            if (SearchByIngredients.idIngredients[i]["Id"] === id) {
                return true
            }
        }
        return false
    }

    protected fun removeFromCookbook(pos: Int) {
        val query: String
        val message: String
        query = "delete from [LinkCookbookRecipe] where link_cb_id = ? and " +
                "link_recipe_id = ?"
        message = "Recipe removed!"
        try {
            val ps = connection!!.prepareStatement(query)
            ps.setString(1, RecipesFragment.selectedCookbookId)
            ps.setString(2, data[pos][0])
            ps.execute()
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("DB Error:", e.message!!)
        }
        try {
            val cp = context as CookbookPage?
            cp!!.recreate()
        } catch (e: Exception) {
            Log.e("Activity Error: ", e.message!!)
        }
    }

    private fun removeRecipe(pos: Int) {
        val query = "delete from [Recipe] where recipe_id = ?"
        val message = "Recipe deleted!"
        try {
            val ps = connection!!.prepareStatement(query)
            ps.setString(1, data[pos][0])
            ps.execute()
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            Log.e("ERROR DB: ", e.message!!)
        }
        try {
            val cp = context as CookbookPage?
            cp!!.recreate()
        } catch (e: Exception) {
            Log.e("Activity Error: ", e.message!!)
        }
    }

    private fun searchRecipesFavoriteBtn(pos: Int, ok: Boolean) {

        // If recipe is already in favorites, clicking the button will remove it from favorites
        val query: String
        val message: String
        if (ok) {
            query = "delete from [Favorites] where fav_account_id = ? and " +
                    "fav_recipe_id = ?"
            message = "Recipe removed!"
        } else {
            query = "insert into [Favorites] values (?,?)"
            message = "Recipe added!"
        }
        try {
            val ps = connection!!.prepareStatement(query)
            ps.setString(1, MainActivity.idUser)
            ps.setString(2, data[pos][0])
            ps.execute()
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            Log.e("ERROR DB: ", e.message!!)
        }

        // If it is the Favorites page, we refresh the activity
        if (RecipesFragment.selectedCookbookId == "") {
            try {
                val cp = context as CookbookPage?
                cp!!.recreate()
            } catch (e: Exception) {
                Log.e("Activity Error: ", e.message!!)
            }
        }
    }

    private fun searchRecipesActionsBtn(actions: ImageButton, pos: Int) {
        val popup = PopupMenu(context, actions)
        popup.menuInflater.inflate(R.menu.search_recipe_menu_popup, popup.menu)
        val menu = popup.menu
        var isFav = false
        try {
            if (connection != null) {
                val query = "select * from [Favorites] where fav_account_id = ? and" +
                        " fav_recipe_id = ?"
                val ps = connection!!.prepareStatement(query)
                ps.setString(1, MainActivity.idUser)
                ps.setString(2, data[pos][0])
                val rs = ps.executeQuery()
                if (rs.next()) {
                    isFav = true
                    menu.getItem(1).title = "Remove Favorite"
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.message!!)
        }
        val ok = isFav
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.search_recipe_add_to_cookbook -> {
                    SearchFragment.selectedRecipeId = data[pos][0]
                    val goToAddToCookbook = Intent(context, AddToCookbook::class.java)
                    context!!.startActivity(goToAddToCookbook)
                }

                R.id.search_recipe_favorite -> {
                    searchRecipesFavoriteBtn(pos, ok)
                }
            }
            true
        }
        popup.show()
    }

    private fun recipesCookbookBtnMenuActions(actions: ImageButton, pos: Int) {
        val popup = PopupMenu(context, actions)
        popup.menuInflater.inflate(R.menu.recipe_in_cookbook_menu_popup, popup.menu)
        val menu = popup.menu
        var isFav = false
        try {
            if (connection != null) {
                val query = "select * from [Favorites] where fav_account_id = ? and" +
                        " fav_recipe_id = ?"
                val ps = connection!!.prepareStatement(query)
                ps.setString(1, MainActivity.idUser)
                ps.setString(2, data[pos][0])
                val rs = ps.executeQuery()
                if (rs.next()) {
                    isFav = true
                    menu.getItem(1).title = "Remove Favorite"
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.message!!)
        }
        val ok = isFav
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.recipe_in_cookbook_remove -> {
                    removeFromCookbook(pos)
                }

                R.id.recipe_in_cookbook_favorite -> {
                    searchRecipesFavoriteBtn(pos, ok)
                }
            }
            true
        }
        popup.show()
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(pos: Int): Any? {
        return null
    }

    override fun getItemId(pos: Int): Long {
        return 0
    }

    override fun getView(pos: Int, convertView: View, parent: ViewGroup): View {
        var convertView = convertView
        when (option) {
            RECIPE_SEARCH -> {
                convertView = inflater.inflate(R.layout.layout_recipes_list_item, null)
                val txtName = convertView.findViewById<TextView>(R.id.search_recipe_list_item_txt_name)
                val txtCookTime = convertView.findViewById<TextView>(R.id.search_recipe_list_item_txt_cook)
                txtName.text = data[pos][1]
                txtCookTime.text = (data[pos][2]!!.toInt() / 60).toString() + "h " + data[pos][2]!!.toInt() % 60 + "m"


                // Add on click for each list view item;
                val recipeItem = convertView.findViewById<LinearLayout>(R.id.search_recipe_item_layout)
                recipeItem.setOnClickListener { view: View? ->
                    SearchFragment.selectedRecipeId = data[pos][0]
                    val goToRecipePage = Intent(context, RecipePage::class.java)
                    context!!.startActivity(goToRecipePage)
                }

                //Add functionality for the menu button
                val actions = convertView.findViewById<ImageButton>(R.id.search_recipe_list_item_btn_actions)
                actions.setOnClickListener { view: View? -> searchRecipesActionsBtn(actions, pos) }
            }

            INGREDIENT_SEARCH -> {
                val ingActivity = context as AddIngredients?
                convertView = inflater.inflate(R.layout.layout_ingredients_search, null)
                val txtName = convertView.findViewById<TextView>(R.id.txt_search_ingredient_name)
                txtName.text = data[pos][1]

                // Add functionality for the add button
                val btnAdd = convertView.findViewById<ImageButton>(R.id.btn_search_ingredient_add)
                btnAdd.setOnClickListener { view: View? ->
                    ingActivity!!.txtInfo!!.text = ""
                    if (isAdded(data[pos][0])) {
                        ingActivity.txtInfo!!.text = "Ingredient already added!"
                        return@setOnClickListener
                    }
                    val act = context as AddIngredients?
                    ButtonChecks.ingredientsAmount(context, act!!.dialogAddIngredient)
                    AddIngredients.id = data[pos][0]
                    AddIngredients.name = data[pos][1]
                }
            }

            INGREDIENT_ADDED -> {
                val ingActivity = context as AddIngredients?
                convertView = inflater.inflate(R.layout.layout_ingredients_added, null)
                val txtName = convertView.findViewById<TextView>(R.id.txt_added_ingredient_name)
                val txtAmount = convertView.findViewById<TextView>(R.id.txt_added_ingredient_amount)
                val editBtn = convertView.findViewById<ImageView>(R.id.img_added_ingredient_amount)
                txtName.text = data[pos][1]
                txtAmount.text = data[pos][2] + " g"
                val dialogEditAmount = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            CreateRecipe.idIngredients.get(pos).put("Amount",
                                    ButtonChecks.boxIngredientAmount!!.text.toString())
                            ingActivity!!.searchBar!!.clearFocus()
                            ingActivity.initAddedIngredients()
                        }

                        DialogInterface.BUTTON_NEGATIVE -> {}
                    }
                }
                // Edit amount when clicking text
                txtAmount.setOnClickListener { view: View? ->
                    ButtonChecks.ingredientsAmountEdit(context,
                            dialogEditAmount, data[pos][2]!!.toInt())
                }
                editBtn.setOnClickListener { view: View? ->
                    ButtonChecks.ingredientsAmountEdit(context,
                            dialogEditAmount, data[pos][2]!!.toInt())
                }

                // Add functionality for the remove button
                val btnRemove = convertView.findViewById<ImageButton>(R.id.btn_added_ingredient_remove)
                btnRemove.setOnClickListener { view: View? ->
                    CreateRecipe.idIngredients.removeAt(pos)
                    ingActivity!!.initAddedIngredients()
                }
            }

            ADD_TO_COOKBOOK -> {
                convertView = inflater.inflate(R.layout.layout_add_to_cookbook_item, null)
                val txtCookbookName = convertView.findViewById<TextView>(R.id.txt_add_to_cookbook_item)
                txtCookbookName.text = data[pos][1]
                val item = convertView.findViewById<LinearLayout>(R.id.layout_add_to_cookbook_item)
                item.setOnClickListener { view: View? ->
                    if (AddToCookbook.selectedCookbookId == "") {
                        txtCookbookName.setTextColor(ContextCompat.getColor(context!!, R.color.orange))
                        AddToCookbook.selectedCookbookId = data[pos][0]
                        AddToCookbook.selectedCookbookPos = pos
                    } else {
                        val lastSelectedItem = parent.getChildAt(AddToCookbook.selectedCookbookPos)
                        val lastSelectedItemName = lastSelectedItem.findViewById<TextView>(R.id.txt_add_to_cookbook_item)
                        lastSelectedItemName.setTextColor(ContextCompat.getColor(context!!, R.color.black))
                        txtCookbookName.setTextColor(ContextCompat.getColor(context!!, R.color.orange))
                        AddToCookbook.selectedCookbookId = data[pos][0]
                        AddToCookbook.selectedCookbookPos = pos
                    }
                    AddToCookbook.btnSave!!.setEnabled(true)
                    AddToCookbook.btnSave!!.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                            context!!, R.color.orange)))
                }
            }

            RECIPE_PAGE_INGREDIENTS -> {
                convertView = inflater.inflate(R.layout.layout_ingredients_recipe_page, null)
                val txtIngredientName = convertView.findViewById<TextView>(R.id.recipe_page_ingredients_item_name)
                txtIngredientName.text = data[pos][1]
                val txtIngredientAmount = convertView.findViewById<TextView>(R.id.recipe_page_ingredients_item_amount)
                txtIngredientAmount.text = data[pos][2] + "g"
            }

            COOKBOOKS -> {
                convertView = inflater.inflate(R.layout.layout_cookbook_list_item, null)
                val txtCookbookName = convertView.findViewById<TextView>(R.id.txt_cookbook_list_item)
                txtCookbookName.text = data[pos][1]
                val cookbookItem = convertView.findViewById<LinearLayout>(R.id.layout_cookbook_list_item)
                cookbookItem.setOnClickListener { view: View? ->
                    RecipesFragment.selectedCookbookId = data[pos][0]
                    val goToCookbookPage = Intent(context, CookbookPage::class.java)
                    context!!.startActivity(goToCookbookPage)
                    val act = MainActivity()
                    act.finish()
                }
            }

            RECIPES_IN_COOKBOOK -> {
                convertView = inflater.inflate(R.layout.layout_recipes_list_item, null)
                val txtRecipeName = convertView.findViewById<TextView>(R.id.search_recipe_list_item_txt_name)
                txtRecipeName.text = data[pos][1]
                val txtRecipeCooktime = convertView.findViewById<TextView>(R.id.search_recipe_list_item_txt_cook)
                var time = ""
                time += (data[pos][2]!!.toInt() / 60).toString() + "h"
                time += (data[pos][2]!!.toInt() % 60).toString() + "m"
                txtRecipeCooktime.text = time
                val btnMenu = convertView.findViewById<ImageButton>(R.id.search_recipe_list_item_btn_actions)
                if (RecipesFragment.selectedCookbookId != "") {
                    btnMenu.setOnClickListener { view: View? -> recipesCookbookBtnMenuActions(btnMenu, pos) }
                } else if (Profile.isActive) {
                    btnMenu.setImageResource(R.drawable.button_remove_item)
                    btnMenu.setOnClickListener { view: View? -> removeRecipe(pos) }
                } else {
                    btnMenu.setImageResource(R.drawable.button_remove_item)
                    btnMenu.setOnClickListener { view: View? -> searchRecipesFavoriteBtn(pos, true) }
                }
                val itemRecipe = convertView.findViewById<LinearLayout>(R.id.search_recipe_item_layout)
                itemRecipe.setOnClickListener { view: View? ->
                    CookbookPage.selectedRecipeId = data[pos][0]
                    val goToRecipePage = Intent(context, RecipePage::class.java)
                    context!!.startActivity(goToRecipePage)
                }
            }

            SEARCHED_INGREDIENTS -> {
                val searchedIngredients = context as SearchByIngredients?
                convertView = inflater.inflate(R.layout.layout_ingredients_search, null)
                val txtName = convertView.findViewById<TextView>(R.id.txt_search_ingredient_name)
                txtName.text = data[pos][1]

                // Add functionality for the add button
                val btnAdd = convertView.findViewById<ImageButton>(R.id.btn_search_ingredient_add)
                btnAdd.setOnClickListener { view: View? ->
                    searchedIngredients!!.txtInfo!!.text = ""
                    if (isAddedToSearchedIngredients(data[pos][0])) {
                        searchedIngredients.txtInfo!!.text = "Ingredient already added!"
                        return@setOnClickListener
                    }
                    val map: MutableMap<String?, String?> = HashMap()
                    map["Id"] = data[pos][0]
                    map["Name"] = data[pos][1]
                    SearchByIngredients.idIngredients.add(map)
                    searchedIngredients.initAddedIngredients()
                    searchedIngredients.checkIngredients()
                }
            }

            ADDED_INGREDIENTS -> {
                val ingActivity = context as SearchByIngredients?
                convertView = inflater.inflate(R.layout.layout_search_by_ingredient_searched_ingredient_item, null)
                val txtName = convertView.findViewById<TextView>(R.id.search_by_ingredients_item_name)
                txtName.text = data[pos][1]
                // Add functionality for the remove button
                val btnRemove = convertView.findViewById<ImageButton>(R.id.search_by_ingredients_btn_ingredient_remove)
                btnRemove.setOnClickListener { view: View? ->
                    SearchByIngredients.idIngredients.removeAt(pos)
                    ingActivity!!.initAddedIngredients()
                    ingActivity.checkIngredients()
                }
            }

            SEARCHED_DIETS -> {
                convertView = inflater.inflate(R.layout.layout_add_to_cookbook_item, null)
                val txtCookbookName = convertView.findViewById<TextView>(R.id.txt_add_to_cookbook_item)
                txtCookbookName.text = data[pos][1]
                val item = convertView.findViewById<LinearLayout>(R.id.layout_add_to_cookbook_item)
                item.setOnClickListener { view: View? ->
                    if (SearchByDiet.selectedDietId == "") {
                        txtCookbookName.setTextColor(ContextCompat.getColor(context!!, R.color.orange))
                        SearchByDiet.selectedDietId = data[pos][0]
                        SearchByDiet.selectedDietPos = pos
                    } else {
                        val lastSelectedItem = parent.getChildAt(SearchByDiet.selectedDietPos)
                        val lastSelectedItemName = lastSelectedItem.findViewById<TextView>(R.id.txt_add_to_cookbook_item)
                        lastSelectedItemName.setTextColor(ContextCompat.getColor(context!!, R.color.black))
                        txtCookbookName.setTextColor(ContextCompat.getColor(context!!, R.color.orange))
                        SearchByDiet.selectedDietId = data[pos][0]
                        SearchByDiet.selectedDietPos = pos
                    }
                    SearchByDiet.btnSave!!.setEnabled(true)
                    SearchByDiet.btnSave!!.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                            context!!, R.color.orange)))
                }
            }
        }
        return convertView
    }

    companion object {
        const val RECIPE_SEARCH = 0
        const val INGREDIENT_SEARCH = 1
        const val INGREDIENT_ADDED = 2
        const val ADD_TO_COOKBOOK = 3
        const val RECIPE_PAGE_INGREDIENTS = 4
        const val COOKBOOKS = 5
        const val RECIPES_IN_COOKBOOK = 6
        const val SEARCHED_INGREDIENTS = 7
        const val ADDED_INGREDIENTS = 8
        const val SEARCHED_DIETS = 9
    }
}