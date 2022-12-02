package projects.vaid.myNotes.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import projects.vaid.myNotes.enums.CustomColors
import projects.vaid.myNotes.Note
import projects.vaid.myNotes.R
import projects.vaid.myNotes.interfaces.Callback


class NotesListFragment : Fragment(){

    //view-model
    private val noteListViewModel : NotesListViewModel by lazy {
        ViewModelProvider(this)[NotesListViewModel::class.java]
    }
    private lateinit var recyclerView : RecyclerView   //recycler view
    private var recyclerAdapter : RecyclerAdapter = RecyclerAdapter()  //adapter
    private lateinit var fab : FloatingActionButton   //floating button
    private var callback : Callback? = null  //хранит ссылку на экземпляр активити
    private lateinit var emptyTextView: TextView
    private lateinit var toolbar: Toolbar
    private var initSearchListUpdate:Boolean = false
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // устанавливаем меню
        callback = context as Callback  //активити = подкласс context

    }


    override fun onDetach() {
        super.onDetach()
        callback = null
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notes_list, container, false) //файл с recyclerView
        recyclerView = view.findViewById(R.id.recycler)
        fab = view.findViewById(R.id.fab)
        toolbar = view.findViewById(R.id.toolbar)
        emptyTextView = view.findViewById(R.id.emptyView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = recyclerAdapter


        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)

        //при нажатии на кнопку добавить
        fab.setOnClickListener {
            val note = Note()               //создаем объект
            noteListViewModel.addNote(note) //добавляем его в бд
            callback?.onItemSelected(note.id) //передаем id в другой фрагмент
        }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //наблюдаем за livedata-списком заметок и передаем новый список адаптеру
        noteListViewModel.notesLiveData.observe(viewLifecycleOwner) { notesList ->
            
            notesList.forEach{
                if(it.text.isEmpty() and it.title.isEmpty())
                    noteListViewModel.deleteNote(it)
            }

            notesList?.let {
                recyclerAdapter.submitList(notesList)
                updateUI(notesList)
            }
        }
        //swipe to delete
        val itemTouchHelper = ItemTouchHelper(swipe)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    //выбор элемента меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.menu_settings)
            callback?.onSettingMenuSelected()

        return super.onOptionsItemSelected(item)
    }

    //создание меню
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_screen_menu, menu)

        val searchView = menu.findItem(R.id.menu_search).actionView as SearchView
        searchView.queryHint = getString(R.string.search)
        searchView.maxWidth = Integer.MAX_VALUE //растянуть на полную ширину
        searchView.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI //заперт доп. UI

        //слушатель при закрытии
        searchView.setOnCloseListener {
            initSearchListUpdate = false
            searchView.isFocusable = false
            searchView.onActionViewCollapsed()
            true
        }


        //слушатель изменения запроса поиска
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) search(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) search(newText)
                initSearchListUpdate = true
                return true
            }

            //поиск и обновление списка
            private fun search(query: String) {
                noteListViewModel.getQueryNote(query).observe(viewLifecycleOwner) { list ->
                    list.let {
                        if (initSearchListUpdate)
                            recyclerAdapter.submitList(list)
                    }
                }
            }
        })
    }

    //для управления перетаскивания и прокрутки
    private val swipe = object: ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean { return true }

        //при прокрутке
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val swipedNote: Note = recyclerAdapter.getNoteAt(viewHolder.adapterPosition)
            noteListViewModel.deleteNote(swipedNote) //удаляем объект из бд
            Snackbar.make(viewHolder.itemView, getString(R.string.note_deleted), Snackbar.LENGTH_SHORT)
                .setAction(getString(R.string.cancel)){ noteListViewModel.addNote(swipedNote) }  //для отмены удаления
                .show()
        }
    }


    //Adapter
   private inner class RecyclerAdapter : ListAdapter<Note, NoteHolder>(ItemComparator) { //сабкласс recyclerAdapter

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
            return NoteHolder(view)
        }

        override fun onBindViewHolder(holder: NoteHolder, position: Int) {
            val note = currentList[position] //currentList - получаем лист из суперкласса
            holder.bind(note)
            //воспроизвести анимацию при соеденении данных с item
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
        }

       //получить объект Note по индексу
       fun getNoteAt(position: Int): Note = currentList[position]
    }

    //обновление UI
    private fun updateUI(notes: List<Note>){
        val emptyList: TextView =view?.findViewById(R.id.emptyView) as TextView

        //установить цвет статус бара прозрачным
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.statusBarColor = resources.getColor(R.color.transparent)
        window?.navigationBarColor = SurfaceColors.SURFACE_0.getColor(requireContext())

        if (notes.isEmpty())
            emptyList.visibility = View.VISIBLE
        else{
            recyclerAdapter.submitList(notes)
            emptyList.visibility = View.INVISIBLE
        }
    }

    //сравнивает различия item'ов
    object ItemComparator : DiffUtil.ItemCallback<Note>(){

        //возвращяет true если два элемента одинаковы по id (не данные элементов)
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        //имеют ли элементы одинаковые данные
        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.title == newItem.title &&
                    oldItem.text == newItem.text &&
                    oldItem.color == newItem.color
        }

    }



    //Holder
   private inner class NoteHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
       private lateinit var note: Note
       private val card: CardView = itemView.findViewById(R.id.cardview)
       private val title: TextView = itemView.findViewById(R.id.title)
       private val text: TextView = itemView.findViewById(R.id.text)

        init {
            itemView.setOnClickListener(this) //устанавливаем слушателя для item
        }

        //функция соеденения данных
       fun bind(note: Note) {
            this.note = note
            title.text = this.note.title
            text.text = this.note.text

            if (note.color.isNotEmpty() and (note.color != CustomColors.TRANSPARENT.color))
                card.setCardBackgroundColor(Color.parseColor(note.color))
            else
                return
       }

        //при нажатии на item
        override fun onClick(p0: View?) {
            callback?.onItemSelected(note.id) //передаем id в другой фрагмент через реализацию в MainActivity
        }

    }


}