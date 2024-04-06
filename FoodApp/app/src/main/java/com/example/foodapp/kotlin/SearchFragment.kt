package com.example.foodapp.kotlin

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.foodapp.R
import java.sql.PreparedStatement

class SearchFragment : Fragment() {
    //ATTRIBUTES
    protected var recipes: ListView? = null
    protected var searchBar: SearchView? = null
    protected var txtInfo: TextView? = null
    protected var imgInfo: ImageView? = null
    protected var main: MainActivity? = null
    private fun initViews(view: View) {
        recipes = view.findViewById(R.id.search_recipes_list)
        searchBar = view.findViewById(R.id.search_search_bar)
        txtInfo = view.findViewById(R.id.search_recipes_txt)
        imgInfo = view.findViewById(R.id.search_recipes_img)
    }

    private fun setOnClickViews() {
        searchBar!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(search: String): Boolean {
                if (search != "") {
                    txtInfo!!.visibility = View.INVISIBLE
                    imgInfo!!.visibility = View.INVISIBLE
                    setListView(search)
                } else {
                    recipes!!.adapter = null
                    imgInfo!!.setImageResource(R.drawable.search_recipe)
                    txtInfo!!.text = "SEARCH FOR A RECIPE"
                    txtInfo!!.visibility = View.VISIBLE
                    imgInfo!!.visibility = View.VISIBLE
                }
                searchBar!!.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText == "") {
                    recipes!!.adapter = null
                    imgInfo!!.setImageResource(R.drawable.search_recipe)
                    txtInfo!!.text = "SEARCH FOR A RECIPE"
                    txtInfo!!.visibility = View.VISIBLE
                    imgInfo!!.visibility = View.VISIBLE
                }
                return true
            }
        })
    }

    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = main!!.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.orange)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    private fun setListView(name: String) {
        val data: MutableList<Map<String?, String?>> = ArrayList()
        try {
            if (MainActivity.connection != null) {
                val query = "select recipe_id,recipe_name,recipe_cook_time from [Recipe] where" +
                        " recipe_name like ? order by recipe_name offset 0 rows fetch next 15 rows " +
                        "only"
                val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
                ps.setString(1, "%$name%")
                val rs = ps.executeQuery()
                if (!rs.next()) {
                    imgInfo!!.visibility = View.VISIBLE
                    txtInfo!!.visibility = View.VISIBLE
                    imgInfo!!.setImageResource(R.drawable.no_results)
                    txtInfo!!.text = "NO RECIPES FOUND"
                    return
                }
                do {
                    val dtrecipe: MutableMap<String?, String?> = HashMap()
                    dtrecipe["Id"] = rs.getString("recipe_id")
                    dtrecipe["Name"] = rs.getString("recipe_name")
                    dtrecipe["Cook Time"] = rs.getString("recipe_cook_time")
                    data.add(dtrecipe)
                } while (rs.next())
            } else {
                Toast.makeText(main, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(main, e.message, Toast.LENGTH_LONG).show()
            Log.e("ERROR", e.message!!)
        }
        val adapter = ListAdapter(activity, data, ListAdapter.RECIPE_SEARCH, MainActivity.connection)
        recipes!!.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If connection is not established, establish it inside the fragment;
        try {
            if (MainActivity.connection!!.isClosed()) {
                val con = ConnectionDB()
                MainActivity.connection = con.connect()
            }
        } catch (e: Exception) {
            Log.e("ERROR DB", e.message!!)
        }

        // Initializations
        main = activity as MainActivity?
        changeStatusBarColor()
        initViews(view)
        setOnClickViews()
    }

    companion object {
        var selectedRecipeId: String? = ""
    }
}