package com.example.foodapp

import android.content.Context
import android.content.DialogInterface
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

object ButtonChecks {
    var boxIngredientAmount: EditText? = null
    fun logoutCheck(context: Context?, dialogClickListener: DialogInterface.OnClickListener?) {
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage("Are you sure you want to log out?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show()
    }

    fun recipeCancelCheck(context: Context?, dialogClickListener: DialogInterface.OnClickListener?) {
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage("Are you sure you want to discard recipe?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show()
    }

    fun ingredientsAmount(context: Context?, dialogClickListener: DialogInterface.OnClickListener?) {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Ingredient amount")
        builder.setMessage("Enter the ingredient amount in grams: ")
        val inputIngredientAmount = EditText(context)
        inputIngredientAmount.inputType = InputType.TYPE_CLASS_NUMBER
        inputIngredientAmount.gravity = Gravity.CENTER
        inputIngredientAmount.width = 100
        inputIngredientAmount.filters = arrayOf<InputFilter>(LengthFilter(4))
        builder.setView(inputIngredientAmount)
        boxIngredientAmount = inputIngredientAmount
        builder.setPositiveButton("Save", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show()
    }

    fun ingredientsAmountEdit(context: Context?, dialogClickListener: DialogInterface.OnClickListener?, amount: Int) {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Edit ingredient amount")
        builder.setMessage("Enter the ingredient amount in grams: ")
        val inputIngredientAmount = EditText(context)
        inputIngredientAmount.inputType = InputType.TYPE_CLASS_NUMBER
        inputIngredientAmount.gravity = Gravity.CENTER
        inputIngredientAmount.width = 100
        inputIngredientAmount.filters = arrayOf<InputFilter>(LengthFilter(4))
        inputIngredientAmount.setText(Integer.toString(amount))
        builder.setView(inputIngredientAmount)
        boxIngredientAmount = inputIngredientAmount
        builder.setPositiveButton("Save", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show()
    }

    fun deleteCookbookCheck(context: Context?, dialogClickListener: DialogInterface.OnClickListener?) {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Delete Cookbook")
        builder.setMessage("Are you sure you want to delete cookbook?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show()
    }
}