package com.example.notes

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.notes.databinding.ActivityAddNoteBinding


class AddNoteActivity : AppCompatActivity() {
    private var id = 0
    private lateinit var binding: ActivityAddNoteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        try {
            val bundle: Bundle? = intent.extras
            id = bundle!!.getInt("id", 0)
            if (id!=0){
                binding.titleEt.setText(bundle.getString("name"))
                binding.descEt.setText(bundle.getString("des"))
            }
        }catch (ex:Exception){}
    }
    fun addNote(view: View){
        val dbManager = DbManager(this)
        val values = ContentValues()
        values.put("Title", binding.titleEt.text.toString())
        values.put("Description", binding.descEt.text.toString())
        if (id ==0){
            val noteID = dbManager.insert(values)
            if (noteID>0){
                Toast.makeText(this, "Note is added", Toast.LENGTH_SHORT).show()
                finish()
            }
            else{
                Toast.makeText(this, "Error adding note...",
                    Toast.LENGTH_SHORT).show()
            }
        }
        else{
            val selectionArgs = arrayOf(id.toString())
            val noteID = dbManager.update(values, "ID=?", selectionArgs)
            if (noteID>0){
                Toast.makeText(this, "Note is added", Toast.LENGTH_SHORT).show()
                finish()
            }
            else{
                Toast.makeText(this, "Error adding note...",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}