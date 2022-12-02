package projects.vaid.myNotes.fragments


import android.app.Activity.RESULT_OK
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import projects.vaid.myNotes.Note
import projects.vaid.myNotes.R
import projects.vaid.myNotes.enums.CustomColors
import java.util.*


private const val ARGUMENT_ID = "selectedId"


class NoteDetailFragment : Fragment(){

    private lateinit var note: Note
    private lateinit var title: EditText
    private lateinit var text: EditText
    private lateinit var toolbar: Toolbar
    private lateinit var scrollView: ScrollView


    //ViewModel
    private val noteDetailViewModel: NoteDetailViewModel by lazy  {
        ViewModelProvider(this)[NoteDetailViewModel::class.java]
    }

    //настройки
    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("SettingPreferences", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        note = Note()
        val selectedNoteId = arguments?.getSerializable(ARGUMENT_ID) as UUID //получаем id из аргументов фрагмента
        noteDetailViewModel.setId(selectedNoteId)  //отправляем во ViewModel
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_note_detail, container, false)
        title = view.findViewById(R.id.titleEditText)
        text = view.findViewById(R.id.noteEditText)
        toolbar = view.findViewById(R.id.toolbar_detail)
        scrollView = view.findViewById(R.id.scrollView)

        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)  //устанавливаем toolbar
        activity.supportActionBar?.title = null
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true) //кнопка назад

        //устанавливаем размер текста из настроек
        val textSize = sharedPreferences.getInt("textSizeSetting", 17)
        text.textSize = textSize.toFloat()


        toolbar.title = null
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener {
            title.onEditorAction(EditorInfo.IME_ACTION_DONE) //скрыть клавиатуру перед закрытием
            text.onEditorAction(EditorInfo.IME_ACTION_DONE)
            activity.onBackPressed()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        noteDetailViewModel.note.observe(viewLifecycleOwner) { note ->
            note?.let {
                this.note = note
                title.setText(note.title)
                text.setText(note.text)
                if (note.color.isNotEmpty())
                setToolbarColor(Color.parseColor(note.color))

            }
        }
    }


    //создание меню
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.share ->      //отправка текста
                if (note.title.isNotEmpty() or note.text.isNotEmpty())
                   sendNote()

            R.id.colorPicker ->
                pickColor(note)  //выбор цвета

            R.id.upload->
                if (note.title.isNotEmpty() or note.text.isNotEmpty())
                   uploadAsTxt(note)
        }
        return super.onOptionsItemSelected(item)
    }


    //выбор цвета
    private fun pickColor(note: Note){
        //color picker builder
        MaterialColorPickerDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_color))
            .setColorShape(ColorShape.CIRCLE)
            .setColorSwatch(ColorSwatch._300)
            .setColors(CustomColors.customColorsAsList())
            .setDefaultColor(note.color)
            .setColorListener { color, colorHex ->
                setToolbarColor(color)
                note.color = colorHex
            }.show()
    }

    //отправка текста
    private fun sendNote(){
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Title: ${note.title}\n\nText: ${note.text}")
            val chooserIntent =
                Intent.createChooser(this, getString(R.string.send_note))
            startActivity(chooserIntent)
        }
    }

    //сохранение заметки txt
    private fun uploadAsTxt(note: Note){
        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "${note.id}.txt")
            writeToFile.launch(this)
        }
    }

    //возвращение результата
    private val writeToFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result?.data?.data!!
            val outputStream = requireContext().contentResolver.openOutputStream(uri)
            outputStream?.let {
                it.write("${note.title}\n\n${note.text}".toByteArray(Charsets.UTF_16))
                it.close()
            }
            Toast.makeText(requireContext(), getString(R.string.note_exported), Toast.LENGTH_SHORT).show()
        }
    }


    //установка цветов toolbar и status bar
    private fun setToolbarColor(color: Int) {

        val fillOptions = sharedPreferences.getInt("colorFillOption", 1)
        val window = activity?.window

        fun fillToolbar() {
            toolbar.setBackgroundColor(color)
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor = color
        }
        when (fillOptions) {
            0 -> return
            1 -> fillToolbar()
            2 -> {
                fillToolbar()
                scrollView.setBackgroundColor(color)
                window?.navigationBarColor = color  //добавить цвет в navigationBar
            }
        }
    }


    override fun onStart() {
        super.onStart()

        //слушатель для изменения текста
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
               note.title = p0.toString()
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                note.text = p0.toString()
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        }

        title.addTextChangedListener(titleWatcher)
        text.addTextChangedListener(textWatcher)
    }

    override fun onStop() {
        super.onStop()
            noteDetailViewModel.updateNote(note) //сохраняем заметку при закрыти
    }


    companion object{
        fun newInstance(id: UUID) : NoteDetailFragment{
            val args =
                Bundle().apply { putSerializable(ARGUMENT_ID, id) } //помещаем id в объект Bundle

            return NoteDetailFragment().apply {
                arguments = args    //присваеиваем объект в качестве аргумента фрагмента
            }
        }
    }

}