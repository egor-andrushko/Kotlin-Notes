package com.example.notes

import android.app.SearchManager
import android.content.*
import android.os.Bundle
import android.view.*
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.notes.databinding.ActivityMainBinding
import com.example.notes.databinding.RowBinding


class MainActivity : AppCompatActivity() {
    private var listNotes = ArrayList<Note>()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //Load from DB
        loadQuery("%")
    }
    override fun onResume() {
        super.onResume()
        loadQuery("%")
    }
    private fun loadQuery(title: String) {
        val dbManager = DbManager(this)
        //dbManager
        val projections = arrayOf("ID", "Title", "Description", "Pin")
        val selectionArgs = arrayOf(title)
        val cursor = dbManager.query(projections, "Title like ?", selectionArgs,
            "Title")
        listNotes.clear()
        if (cursor.moveToFirst()) {
            do {
                val noteID = cursor.getInt(cursor.getColumnIndexOrThrow("ID"))
                val noteTitle = cursor.getString(cursor.getColumnIndexOrThrow("Title"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("Description"))
                val pin = cursor.getInt(cursor.getColumnIndexOrThrow("Pin")) != 0
                listNotes.add(Note(noteID, noteTitle, description, pin))
            } while (cursor.moveToNext())
        }
        listNotes.sortWith(compareByDescending<Note> {it.nodePin}.thenBy {it.nodeID})
        //adapter
        val myNotesAdapter = MyNotesAdapter(this, listNotes)
        //set adapter
        binding.notesLv.adapter = myNotesAdapter
        //get total number of tasks from ListView
        val total = binding.notesLv.count
        //actionbar
        val mActionBar = supportActionBar
        if (mActionBar != null) {
            //set to actionbar as subtitle of actionbar
            mActionBar.subtitle = "You have $total note(s) in list..."
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        //searchView
        val sv: SearchView = menu!!.findItem(R.id.app_bar_search).actionView as SearchView
        val sm = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        sv.setSearchableInfo(sm.getSearchableInfo(componentName))
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                loadQuery("%$query%")
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                loadQuery("%$newText%")
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }
    private var toast: Toast? = null
    private fun createToast(text: String)
    {
        toast?.cancel()
        toast = Toast.makeText(this@MainActivity, "$text",
            Toast.LENGTH_SHORT)
        toast?.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addNote -> {
                startActivity(Intent(this, AddNoteActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
    inner class MyNotesAdapter(context: Context, private var listNotesAdapter: ArrayList<Note>) :
        BaseAdapter() {
        private var context: Context = context
        override fun getView(position: Int, convertView: View?, parent:
        ViewGroup?): View {
            //inflate layout row.xml
            var binding = RowBinding.inflate(layoutInflater)
            val myNote = listNotesAdapter[position]
            binding.titleTv.text = myNote.nodeName
            binding.descTv.text = myNote.nodeDes
            binding.pinBtn.rotation = if (myNote.nodePin) 0F else 45F
            //delete button click
            binding.deleteBtn.setOnClickListener {
                val dbManager = DbManager(this.context!!)
                val selectionArgs = arrayOf(myNote.nodeID.toString())
                dbManager.delete("ID=?", selectionArgs)
                loadQuery("%")
            }
            //edit//update button click
            binding.root.setOnClickListener { goToUpdateFun(myNote) }
            //pin btn click
            binding.pinBtn.setOnClickListener {
                var text: String
                var pinned = false
                if (myNote.nodePin){
                    binding.pinBtn.rotation = 45F
                    pinned = false
                    text = "unpinned"
                }
                else
                {
                    binding.pinBtn.rotation = 0F
                    pinned = true
                    text = "pinned"
                }
                myNote.nodePin = pinned
                createToast("$text...")

                val dbManager = DbManager(context)
                val values = ContentValues()
                values.put("Pin", pinned)
                val selectionArgs = arrayOf(myNote.nodeID.toString())
                dbManager.update(values, "ID=?", selectionArgs)
                (context as MainActivity).onResume()
            }
            //copy btn click
            binding.copyBtn.setOnClickListener {
                //get title
                val title = binding.titleTv.text.toString()
                //get description
                val desc = binding.descTv.text.toString()
                //concat
                val s = title + "\n" + desc
                val cb = getSystemService(Context.CLIPBOARD_SERVICE) as
                        ClipboardManager
                val clip = ClipData.newPlainText("", s)
                cb.setPrimaryClip(clip)
                Toast.makeText(this@MainActivity, "Copied...",
                    Toast.LENGTH_SHORT).show()
            }
            //share btn click
            binding.shareBtn.setOnClickListener {
                //get title
                val title = binding.titleTv.text.toString()
                //get description
                val desc = binding.descTv.text.toString()
                //concatenate
                val s = title + "\n" + desc
                //share intent
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, s)
                startActivity(Intent.createChooser(shareIntent, s))
            }
            return binding.root
        }
        override fun getItem(position: Int): Any {
            return listNotesAdapter[position]
        }
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
        override fun getCount(): Int {
            return listNotesAdapter.size
        }
    }
    private fun goToUpdateFun(myNote: Note) {
        val intent = Intent(this, AddNoteActivity::class.java)
        intent.putExtra("id", myNote.nodeID) //put id
        intent.putExtra("name", myNote.nodeName) //ut name
        intent.putExtra("des", myNote.nodeDes) //put description
        startActivity(intent) //start activity
    }
}
