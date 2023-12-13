package com.example.foodapp;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

public class ButtonChecks {
    public static EditText boxIngredientAmount;
    public static void logoutCheck(Context context, DialogInterface.OnClickListener dialogClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to log out?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
    public static void recipeCancelCheck(Context context, DialogInterface.OnClickListener dialogClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to discard recipe?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
    public static void ingredientsAmount(Context context, DialogInterface.OnClickListener dialogClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Ingredient amount");
        builder.setMessage("Enter the ingredient amount in grams: ");
        EditText inputIngredientAmount = new EditText(context);
        inputIngredientAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputIngredientAmount.setGravity(Gravity.CENTER);
        inputIngredientAmount.setWidth(100);
        inputIngredientAmount.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        builder.setView(inputIngredientAmount);
        boxIngredientAmount = inputIngredientAmount;
        builder.setPositiveButton("Save", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

    public static void ingredientsAmountEdit(Context context, DialogInterface.OnClickListener dialogClickListener, int amount){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit ingredient amount");
        builder.setMessage("Enter the ingredient amount in grams: ");
        EditText inputIngredientAmount = new EditText(context);
        inputIngredientAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputIngredientAmount.setGravity(Gravity.CENTER);
        inputIngredientAmount.setWidth(100);
        inputIngredientAmount.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        inputIngredientAmount.setText(Integer.toString(amount));
        builder.setView(inputIngredientAmount);
        boxIngredientAmount = inputIngredientAmount;
        builder.setPositiveButton("Save", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

    public static void deleteCookbookCheck(Context context, DialogInterface.OnClickListener dialogClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Cookbook");
        builder.setMessage("Are you sure you want to delete cookbook?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}
