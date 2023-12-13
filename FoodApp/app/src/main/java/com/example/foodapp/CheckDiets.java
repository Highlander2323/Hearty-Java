package com.example.foodapp;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public class CheckDiets {
    float fatsTotal = 0, carbsTotal = 0, proteinTotal = 0, totalDiets = 0;
    int servings;
    List<Map<String, String>> idIngredients;
    String idRecipe;
    Connection connection;

    public CheckDiets(List<Map<String, String>> ids, Connection con, int servs, String rec){
        idIngredients = ids;
        connection = con;
        servings = servs;
        idRecipe = rec;
    }
    public void calculateValues(){
        String query = "select * from Ingredient where ing_id = ?";
        PreparedStatement ps;
        try {
            for (int i = 0; i < idIngredients.size(); i++) {
                ps = connection.prepareStatement(query);
                ps.setString(1, idIngredients.get(i).get("Id"));
                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    fatsTotal += Float.parseFloat(rs.getString("ing_fats")) *
                            Integer.parseInt(idIngredients.get(i).get("Amount")) / 100;
                    carbsTotal += Float.parseFloat(rs.getString("ing_carbs")) *
                            Integer.parseInt(idIngredients.get(i).get("Amount")) / 100;
                    proteinTotal += Float.parseFloat(rs.getString("ing_protein")) *
                            Integer.parseInt(idIngredients.get(i).get("Amount")) / 100;}}}
        catch(Exception e){
            Log.e("ERROR DB: ", e.getMessage());}}
    public void checkDiets(){
        String query = "select diet_id from Diet";
        PreparedStatement ps;
        boolean ok;

        try {
                ps = connection.prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    totalDiets++;
                }
            }catch(Exception e){
            Log.e("ERROR DB: ", e.getMessage());
        }


        try {
            for (int i = 1; i <= totalDiets; i++) {
                query = "select * from Diet where diet_id = ?";
                ok = true;
                ps = connection.prepareStatement(query);
                ps.setString(1, String.valueOf(i));
                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    if((fatsTotal / servings) > Float.parseFloat(rs.getString("diet_max_fats"))){
                        ok = false;
                    }
                    if((carbsTotal / servings) > Float.parseFloat(rs.getString("diet_max_carbs"))){
                        ok = false;
                    }
                    if((proteinTotal / servings) > Float.parseFloat(rs.getString("diet_max_protein"))){
                        ok = false;
                    }
                    if(ok == true){
                        query = "insert into DietRecipeLink values (?,?)";
                        ps = connection.prepareStatement(query);
                        ps.setString(1, rs.getString("diet_id"));
                        ps.setString(2, idRecipe);
                        ps.execute();
                    }
                }

            }
        }
        catch(Exception e){
            Log.e("ERROR DB: ", e.getMessage());
        }
    }
}
