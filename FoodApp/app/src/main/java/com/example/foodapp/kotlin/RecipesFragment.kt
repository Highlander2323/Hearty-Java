package com.example.foodapp.kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.foodapp.R
import java.sql.PreparedStatement

/**
 * A simple [Fragment] subclass.
 * Use the [RecipesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecipesFragment : Fragment() {
    private lateinit var btnAdd: Button
    private lateinit var btnProfile: Button
    private lateinit var btnAddMod: Button
    private lateinit var btnFavs: Button
    private lateinit var imgInfo: ImageView
    private lateinit var txtInfo: TextView
    private var main: MainActivity? = null
    private var cookbooks: ListView? = null
    private var mParam1: String? = null
    private var mParam2: String? = null


    private fun clickAddIngMod() {
        val goToAddIngMod = Intent(main, AddIngredientsMod::class.java)
        startActivity(goToAddIngMod)
    }

    private fun clickAdd() {
        val popup = PopupMenu(main, btnAdd)
        popup.menuInflater.inflate(R.menu.add_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            if (menuItem.itemId == R.id.add_cookbook) {
                val createCookbook = Intent(main, CreateCookbook::class.java)
                startActivity(createCookbook)
                main!!.finish()
            } else {
                val createRecipe = Intent(main, CreateRecipe::class.java)
                startActivity(createRecipe)
            }
            true
        }
        popup.show()
    }

    private fun clickFavorites() {
        val goToFavs = Intent(main, CookbookPage::class.java)
        startActivity(goToFavs)
    }

    private fun clickProfile() {
        val profile = Intent(main, Profile::class.java)
        startActivity(profile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipes, container, false)
    }

    private fun setListView() {
        val data: MutableList<Map<String?, String?>> = ArrayList()
        try {
            if (MainActivity.connection != null) {
                val query = "select * from [Cookbook] where cb_account_id = ?"
                val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
                ps.setString(1, MainActivity.idUser)
                val rs = ps.executeQuery()
                if (!rs.next()) {
                    imgInfo.visibility = View.VISIBLE
                    txtInfo.visibility = View.VISIBLE
                    return
                } else {
                    imgInfo.visibility = View.INVISIBLE
                    txtInfo.visibility = View.INVISIBLE
                }
                do {
                    val map: MutableMap<String?, String?> = HashMap()
                    map["IdCookbook"] = rs.getString("cb_id")
                    map["NameCookbook"] = rs.getString("cb_name")
                    data.add(map)
                } while (rs.next())
            }
        } catch (e: Exception) {
            Log.e("ERROR DB:", e.message!!)
        }
        val adapter = ListAdapter(activity, data, ListAdapter.COOKBOOKS, MainActivity.connection)
        cookbooks!!.adapter = adapter
    }

    protected fun initViews(view: View) {
        btnAdd = view.findViewById(R.id.fragment_recipes_btn_add)
        btnProfile = view.findViewById(R.id.fragment_recipes_btn_profile)
        btnAddMod = view.findViewById(R.id.fragment_recipes_btn_add_ingredients_mod)
        btnFavs = view.findViewById(R.id.fragment_recipes_btn_favorites)
        cookbooks = view.findViewById(R.id.fragment_recipes_cookbooks_list)
        txtInfo = view.findViewById(R.id.fragment_recipes_txt_info)
        imgInfo = view.findViewById(R.id.fragment_recipes_img_info)
        setListView()
    }

    protected fun setOnClickViews() {
        btnAdd.setOnClickListener { click: View? -> clickAdd() }
        btnProfile.setOnClickListener { click: View? -> clickProfile() }
        btnAddMod.setOnClickListener { click: View? -> clickAddIngMod() }
        btnFavs.setOnClickListener { click: View? -> clickFavorites() }
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
        initViews(view)
        main = activity as MainActivity?
        setOnClickViews()
    }

    companion object {
        var selectedCookbookId: String? = ""

        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(param1: String?, param2: String?): RecipesFragment {
            val fragment = RecipesFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}