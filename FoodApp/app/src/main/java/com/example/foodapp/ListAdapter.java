package com.example.foodapp;

        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.res.ColorStateList;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.BaseAdapter;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.PopupMenu;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.core.content.ContextCompat;

        import java.sql.Connection;
        import java.sql.PreparedStatement;
        import java.sql.ResultSet;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;


public class ListAdapter extends BaseAdapter {
    Connection connection;
    Context context;
    String[][] data;
    LayoutInflater inflater;
    public static final int RECIPE_SEARCH = 0, INGREDIENT_SEARCH = 1, INGREDIENT_ADDED = 2,
            ADD_TO_COOKBOOK = 3, RECIPE_PAGE_INGREDIENTS = 4, COOKBOOKS = 5, RECIPES_IN_COOKBOOK = 6,
    SEARCHED_INGREDIENTS = 7, ADDED_INGREDIENTS = 8, SEARCHED_DIETS = 9;
    private int option;
    public ListAdapter(Context ctx, List<Map<String,String>> list, int opt){
        connection = null;
        context = ctx;
        option = opt;
        inflater = LayoutInflater.from(ctx);
        switch(option) {
            case RECIPES_IN_COOKBOOK:
            case RECIPE_SEARCH: {
                data = new String[list.size()][3];
                for (int i = 0; i < list.size(); i++) {
                    data[i][0] = list.get(i).get("Id");
                    data[i][1] = list.get(i).get("Name");
                    data[i][2] = list.get(i).get("Cook Time");
                }
                break;
            }
            case ADDED_INGREDIENTS:
            case SEARCHED_INGREDIENTS:
            case INGREDIENT_SEARCH: {
                data = new String[list.size()][2];
                for (int i = 0; i < list.size(); i++) {
                    data[i][0] = list.get(i).get("Id");
                    data[i][1] = list.get(i).get("Name");
                }
                break;
            }
            case INGREDIENT_ADDED: {
                data = new String[list.size()][3];
                for (int i = 0; i < list.size(); i++) {
                    data[i][0] = list.get(i).get("Id");
                    data[i][1] = list.get(i).get("Name");
                    data[i][2] = list.get(i).get("Amount");
                }
                break;
            }
            case ADD_TO_COOKBOOK:
            case COOKBOOKS: {
                data = new String[list.size()][2];
                for(int i = 0; i < list.size(); i++){
                    data[i][0] = list.get(i).get("IdCookbook");
                    data[i][1] = list.get(i).get("NameCookbook");
                }
                break;
            }
            case SEARCHED_DIETS:{
                data = new String[list.size()][2];
                for(int i = 0; i < list.size(); i++){
                    data[i][0] = list.get(i).get("IdDiet");
                    data[i][1] = list.get(i).get("NameDiet");
                }
                break;
            }
            case RECIPE_PAGE_INGREDIENTS:{
                data = new String[list.size()][3];
                for(int i = 0; i < list.size(); i++){
                    data[i][0] = list.get(i).get("IdIngredient");
                    data[i][1] = list.get(i).get("NameIngredient");
                    data[i][2] = list.get(i).get("AmountIngredient");
                }
                break;
            }
        }
    }
    public ListAdapter(Context ctx, List<Map<String,String>> list, int opt, Connection con){
        this(ctx, list, opt);
        connection = con;
    }
    protected boolean isAdded(String id){
        for(int i = 0; i < CreateRecipe.idIngredients.size(); i++){
            if(CreateRecipe.idIngredients.get(i).get("Id") == id){
                return true;
            }
        }
        return false;
    }
    protected boolean isAddedToSearchedIngredients(String id){
        for(int i = 0; i < SearchByIngredients.idIngredients.size(); i++){
            if(SearchByIngredients.idIngredients.get(i).get("Id") == id){
                return true;
            }
        }
        return false;
    }
    protected void removeFromCookbook(int pos){
        String query, message;
        query = "delete from [LinkCookbookRecipe] where link_cb_id = ? and " +
                "link_recipe_id = ?";
        message = "Recipe removed!";

        try{
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, RecipesFragment.selectedCookbookId);
            ps.setString(2, data[pos][0]);
            ps.execute();
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
            catch(Exception e){
            Log.e("DB Error:", e.getMessage());
        }

        try {
            CookbookPage cp = (CookbookPage) context;
            cp.recreate();
        }
        catch(Exception e){
            Log.e("Activity Error: ", e.getMessage());
        }
    }
    protected void removeRecipe(int pos){
        String query, message;
        query = "delete from [Recipe] where recipe_id = ?";
        message = "Recipe deleted!";

        try{
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, data[pos][0]);
            ps.execute();
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
        catch(Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ERROR DB: ", e.getMessage());
        }

            try {
                CookbookPage cp = (CookbookPage) context;
                cp.recreate();

            }
            catch(Exception e){
                Log.e("Activity Error: ", e.getMessage());
            }
    }
    protected void searchRecipesFavoriteBtn(int pos, boolean ok){

        // If recipe is already in favorites, clicking the button will remove it from favorites
            String query, message;
            if(ok){
                query = "delete from [Favorites] where fav_account_id = ? and " +
                            "fav_recipe_id = ?";
                message = "Recipe removed!";
            }
            else{
                query = "insert into [Favorites] values (?,?)";
                message = "Recipe added!";
            }

            try{
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, MainActivity.idUser);
                ps.setString(2, data[pos][0]);
                ps.execute();
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
            catch(Exception e){
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ERROR DB: ", e.getMessage());
            }

            // If it is the Favorites page, we refresh the activity
            if(RecipesFragment.selectedCookbookId.equals("")){
                try {
                    CookbookPage cp = (CookbookPage) context;
                    cp.recreate();
                }
                catch(Exception e){
                    Log.e("Activity Error: ", e.getMessage());
                }
            }
    }
    private void searchRecipesActionsBtn(ImageButton actions, int pos){
        PopupMenu popup = new PopupMenu(context, actions);
        popup.getMenuInflater().inflate(R.menu.search_recipe_menu_popup, popup.getMenu());
        Menu menu = popup.getMenu();
        boolean isFav = false;
        try {
            if (connection != null) {
                String query = "select * from [Favorites] where fav_account_id = ? and" +
                        " fav_recipe_id = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, MainActivity.idUser);
                ps.setString(2, data[pos][0]);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    isFav = true;
                    menu.getItem(1).setTitle("Remove Favorite");
                }
            }
        }
        catch(Exception e){
            Log.e("ERROR", e.getMessage());
        }
        boolean ok = isFav;
        popup.setOnMenuItemClickListener(menuItem -> {
            switch(menuItem.getItemId()) {
                case R.id.search_recipe_add_to_cookbook: {
                    SearchFragment.selectedRecipeId = data[pos][0];
                    Intent goToAddToCookbook = new Intent(context, AddToCookbook.class);
                    context.startActivity(goToAddToCookbook);
                    break;
                }

                case R.id.search_recipe_favorite: {
                    searchRecipesFavoriteBtn(pos, ok);
                    break;
                }
            }
            return true;
        });
        popup.show();
    }
    private void recipesCookbookBtnMenuActions(ImageButton actions, int pos){
        PopupMenu popup = new PopupMenu(context, actions);
        popup.getMenuInflater().inflate(R.menu.recipe_in_cookbook_menu_popup, popup.getMenu());
        Menu menu = popup.getMenu();
        boolean isFav = false;
        try {
            if (connection != null) {
                String query = "select * from [Favorites] where fav_account_id = ? and" +
                        " fav_recipe_id = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, MainActivity.idUser);
                ps.setString(2, data[pos][0]);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    isFav = true;
                    menu.getItem(1).setTitle("Remove Favorite");
                }
            }
        }
        catch(Exception e){
            Log.e("ERROR", e.getMessage());
        }
        boolean ok = isFav;
        popup.setOnMenuItemClickListener(menuItem -> {
            switch(menuItem.getItemId()) {
                case R.id.recipe_in_cookbook_remove: {
                    removeFromCookbook(pos);
                    break;
                }

                case R.id.recipe_in_cookbook_favorite: {
                    searchRecipesFavoriteBtn(pos, ok);
                    break;
                }
            }
            return true;
        });
        popup.show();
    }
    @Override
    public int getCount(){
        return data.length;
    }
    @Override
    public Object getItem(int pos){
        return null;
    }
    @Override
    public long getItemId(int pos){
        return 0;
    }
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        switch (option) {
            case RECIPE_SEARCH: {
                convertView = inflater.inflate(R.layout.layout_recipes_list_item, null);
                TextView txtName = convertView.findViewById(R.id.search_recipe_list_item_txt_name),
                        txtCookTime = convertView.findViewById(R.id.search_recipe_list_item_txt_cook);
                txtName.setText(data[pos][1]);
                txtCookTime.setText(Integer.parseInt(data[pos][2]) / 60 + "h " +
                        Integer.parseInt(data[pos][2]) % 60 + "m");


                // Add on click for each list view item;
                LinearLayout recipeItem = convertView.findViewById(R.id.search_recipe_item_layout);
                recipeItem.setOnClickListener(view->{
                    SearchFragment.selectedRecipeId = data[pos][0];
                    Intent goToRecipePage = new Intent(context, RecipePage.class);
                    context.startActivity(goToRecipePage);
                });

                //Add functionality for the menu button
                ImageButton actions = convertView.findViewById(R.id.search_recipe_list_item_btn_actions);
                actions.setOnClickListener(view -> searchRecipesActionsBtn(actions ,pos));
                break;
            }

            case INGREDIENT_SEARCH:{
                AddIngredients ingActivity = (AddIngredients) context;
                convertView = inflater.inflate(R.layout.layout_ingredients_search, null);
                TextView txtName = convertView.findViewById(R.id.txt_search_ingredient_name);
                txtName.setText(data[pos][1]);

                // Add functionality for the add button
                ImageButton btnAdd = convertView.findViewById(R.id.btn_search_ingredient_add);
                btnAdd.setOnClickListener(view -> {
                    ingActivity.txtInfo.setText("");
                    if(isAdded(data[pos][0])){
                        ingActivity.txtInfo.setText("Ingredient already added!");
                        return;
                    }
                    AddIngredients act = (AddIngredients) context;
                    ButtonChecks.ingredientsAmount(context, act.dialogAddIngredient);
                    AddIngredients.id = data[pos][0];
                    AddIngredients.name = data[pos][1];
                });
                break;
            }

            case INGREDIENT_ADDED:{

                AddIngredients ingActivity = (AddIngredients) context;
                convertView = inflater.inflate(R.layout.layout_ingredients_added, null);
                TextView txtName = convertView.findViewById(R.id.txt_added_ingredient_name),
                txtAmount = convertView.findViewById(R.id.txt_added_ingredient_amount);
                ImageView editBtn = convertView.findViewById(R.id.img_added_ingredient_amount);
                txtName.setText(data[pos][1]);
                txtAmount.setText(data[pos][2] + " g");
                DialogInterface.OnClickListener dialogEditAmount = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE: {
                            CreateRecipe.idIngredients.get(pos).put("Amount",
                                    ButtonChecks.boxIngredientAmount.getText().toString());
                            ingActivity.searchBar.clearFocus();
                            ingActivity.initAddedIngredients();
                            break;
                        }
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };
                // Edit amount when clicking text
                txtAmount.setOnClickListener(view->ButtonChecks.ingredientsAmountEdit(context,
                        dialogEditAmount, Integer.parseInt(data[pos][2])));
                editBtn.setOnClickListener(view->ButtonChecks.ingredientsAmountEdit(context,
                        dialogEditAmount, Integer.parseInt(data[pos][2])));

                // Add functionality for the remove button
                ImageButton btnRemove = convertView.findViewById(R.id.btn_added_ingredient_remove);
                btnRemove.setOnClickListener(view -> {
                    CreateRecipe.idIngredients.remove(pos);
                    ingActivity.initAddedIngredients();
                });
                break;
            }

            case ADD_TO_COOKBOOK:{
                convertView = inflater.inflate(R.layout.layout_add_to_cookbook_item, null);

                TextView txtCookbookName = convertView.findViewById(R.id.txt_add_to_cookbook_item);
                txtCookbookName.setText(data[pos][1]);
                LinearLayout item = convertView.findViewById(R.id.layout_add_to_cookbook_item);
                item.setOnClickListener(view->{
                    if(AddToCookbook.selectedCookbookId.equals("")) {
                        txtCookbookName.setTextColor(ContextCompat.getColor(context, R.color.orange));
                        AddToCookbook.selectedCookbookId = data[pos][0];
                        AddToCookbook.selectedCookbookPos = pos;
                    }
                    else{
                        View lastSelectedItem = parent.getChildAt(AddToCookbook.selectedCookbookPos);
                        TextView lastSelectedItemName = lastSelectedItem.findViewById(R.id.txt_add_to_cookbook_item);
                        lastSelectedItemName.setTextColor(ContextCompat.getColor(context, R.color.black));
                        txtCookbookName.setTextColor(ContextCompat.getColor(context, R.color.orange));
                        AddToCookbook.selectedCookbookId = data[pos][0];
                        AddToCookbook.selectedCookbookPos = pos;
                    }
                    AddToCookbook.btnSave.setEnabled(true);
                    AddToCookbook.btnSave.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                            context, R.color.orange)));
                });
                break;
            }

            case RECIPE_PAGE_INGREDIENTS:{
                convertView = inflater.inflate(R.layout.layout_ingredients_recipe_page, null);

                TextView txtIngredientName = convertView.findViewById(R.id.recipe_page_ingredients_item_name);
                txtIngredientName.setText(data[pos][1]);

                TextView txtIngredientAmount = convertView.findViewById(R.id.recipe_page_ingredients_item_amount);
                txtIngredientAmount.setText(data[pos][2] + "g");

                break;
            }

            case COOKBOOKS:{
                convertView = inflater.inflate(R.layout.layout_cookbook_list_item, null);

                TextView txtCookbookName = convertView.findViewById(R.id.txt_cookbook_list_item);
                txtCookbookName.setText(data[pos][1]);

                LinearLayout cookbookItem = convertView.findViewById(R.id.layout_cookbook_list_item);
                cookbookItem.setOnClickListener(view->{
                    RecipesFragment.selectedCookbookId = data[pos][0];
                    Intent goToCookbookPage = new Intent(context, CookbookPage.class);
                    context.startActivity(goToCookbookPage);
                    MainActivity act = new MainActivity();
                    act.finish();
                });

                break;
            }

            case RECIPES_IN_COOKBOOK:{
                convertView = inflater.inflate(R.layout.layout_recipes_list_item, null);

                TextView txtRecipeName = convertView.findViewById(R.id.search_recipe_list_item_txt_name);
                txtRecipeName.setText(data[pos][1]);

                TextView txtRecipeCooktime = convertView.findViewById(R.id.search_recipe_list_item_txt_cook);
                String time = "";
                time += Integer.parseInt(data[pos][2]) / 60 + "h";
                time += Integer.parseInt(data[pos][2]) % 60 + "m";
                txtRecipeCooktime.setText(time);

                ImageButton btnMenu = convertView.findViewById(R.id.search_recipe_list_item_btn_actions);
                if(!RecipesFragment.selectedCookbookId.equals("")) {
                    btnMenu.setOnClickListener(view -> recipesCookbookBtnMenuActions(btnMenu, pos));
                }else if(Profile.isActive){
                    btnMenu.setImageResource(R.drawable.button_remove_item);
                    btnMenu.setOnClickListener(view -> removeRecipe(pos));
                }
                else{
                    btnMenu.setImageResource(R.drawable.button_remove_item);
                    btnMenu.setOnClickListener(view -> searchRecipesFavoriteBtn(pos, true));
                }

                LinearLayout itemRecipe = convertView.findViewById(R.id.search_recipe_item_layout);
                itemRecipe.setOnClickListener(view -> {
                    CookbookPage.selectedRecipeId = data[pos][0];
                    Intent goToRecipePage = new Intent(context, RecipePage.class);
                    context.startActivity(goToRecipePage);
                });

                break;
            }

            case SEARCHED_INGREDIENTS:{
                SearchByIngredients searchedIngredients = (SearchByIngredients) context;
                convertView = inflater.inflate(R.layout.layout_ingredients_search, null);
                TextView txtName = convertView.findViewById(R.id.txt_search_ingredient_name);
                txtName.setText(data[pos][1]);

                // Add functionality for the add button
                ImageButton btnAdd = convertView.findViewById(R.id.btn_search_ingredient_add);
                btnAdd.setOnClickListener(view -> {
                    searchedIngredients.txtInfo.setText("");
                    if(isAddedToSearchedIngredients(data[pos][0])){
                        searchedIngredients.txtInfo.setText("Ingredient already added!");
                        return;
                    }
                    Map<String, String> map = new HashMap();
                    map.put("Id", data[pos][0]);
                    map.put("Name", data[pos][1]);
                    SearchByIngredients.idIngredients.add(map);
                    searchedIngredients.initAddedIngredients();
                    searchedIngredients.checkIngredients();
                });
                break;
            }

            case ADDED_INGREDIENTS:{
                SearchByIngredients ingActivity = (SearchByIngredients) context;
                convertView = inflater.inflate(R.layout.layout_search_by_ingredient_searched_ingredient_item, null);
                TextView txtName = convertView.findViewById(R.id.search_by_ingredients_item_name);
                txtName.setText(data[pos][1]);
                // Add functionality for the remove button
                ImageButton btnRemove = convertView.findViewById(R.id.search_by_ingredients_btn_ingredient_remove);
                btnRemove.setOnClickListener(view -> {
                    SearchByIngredients.idIngredients.remove(pos);
                    ingActivity.initAddedIngredients();
                    ingActivity.checkIngredients();
                });
                break;
            }
            case SEARCHED_DIETS:{
                convertView = inflater.inflate(R.layout.layout_add_to_cookbook_item, null);

                TextView txtCookbookName = convertView.findViewById(R.id.txt_add_to_cookbook_item);
                txtCookbookName.setText(data[pos][1]);
                LinearLayout item = convertView.findViewById(R.id.layout_add_to_cookbook_item);
                item.setOnClickListener(view->{
                    if(SearchByDiet.selectedDietId.equals("")) {
                        txtCookbookName.setTextColor(ContextCompat.getColor(context, R.color.orange));
                        SearchByDiet.selectedDietId = data[pos][0];
                        SearchByDiet.selectedDietPos = pos;
                    }
                    else{
                        View lastSelectedItem = parent.getChildAt(SearchByDiet.selectedDietPos);
                        TextView lastSelectedItemName = lastSelectedItem.findViewById(R.id.txt_add_to_cookbook_item);
                        lastSelectedItemName.setTextColor(ContextCompat.getColor(context, R.color.black));
                        txtCookbookName.setTextColor(ContextCompat.getColor(context, R.color.orange));
                        SearchByDiet.selectedDietId = data[pos][0];
                        SearchByDiet.selectedDietPos = pos;
                    }
                    SearchByDiet.btnSave.setEnabled(true);
                    SearchByDiet.btnSave.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                            context, R.color.orange)));
                });
                break;
            }


        }

        return convertView;
    }

}

