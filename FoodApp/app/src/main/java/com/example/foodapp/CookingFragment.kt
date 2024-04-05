package com.example.foodapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.sql.Connection

/**
 * A simple [Fragment] subclass.
 * Use the [CookingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CookingFragment : Fragment() {
    protected var connection: Connection? = null
    protected var btnSearchByIngredients: Button? = null
    protected var btnSearchByDiet: Button? = null
    private var mParam1: String? = null
    private var mParam2: String? = null
    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = main!!.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.orange)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = true
            }
        }
    }

    private fun initViews(view: View) {
        btnSearchByIngredients = view.findViewById(R.id.cooking_fragment_btn_search_by_ingredients)
        btnSearchByDiet = view.findViewById(R.id.cooking_fragment_btn_search_by_diets)
        listRecipes = view.findViewById(R.id.cooking_fragment_recipes_list)
        txtCook = view.findViewById(R.id.cooking_fragment_txt)
    }

    private fun setOnClickViews() {
        btnSearchByIngredients!!.setOnClickListener { view: View? -> clickSearchIngredients() }
        btnSearchByDiet!!.setOnClickListener { view: View? -> clickSearchDiet() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cooking, container, false)
    }

    private fun clickSearchIngredients() {
        val goToSearchIngredients = Intent(main, SearchByIngredients::class.java)
        startActivity(goToSearchIngredients)
    }

    private fun clickSearchDiet() {
        val goToSearchDiet = Intent(main, SearchByDiet::class.java)
        startActivity(goToSearchDiet)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If connection is not established, establish it inside the fragment;
        // If connection is not established, establish it inside the fragment;
        connection = MainActivity.connection
        try {
            if (connection!!.isClosed) {
                val con = ConnectionDB()
                connection = con.connect()
            }
        } catch (e: Exception) {
            Log.e("ERROR DB", e.message!!)
        }

        // Inflate the layout for this fragment
        main = activity as MainActivity?
        initViews(view)
        setOnClickViews()
        changeStatusBarColor()
    }

    companion object {
        var listRecipes: ListView? = null
        var txtCook: TextView? = null
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        protected var main: MainActivity? = null
        fun newInstance(param1: String?, param2: String?): CookingFragment {
            val fragment = CookingFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }

        fun initListRecipes(data: List<Map<String?, String?>>) {
            val adapter = ListAdapter(main, data, ListAdapter.RECIPE_SEARCH, MainActivity.connection)
            listRecipes!!.adapter = adapter
        }
    }
}