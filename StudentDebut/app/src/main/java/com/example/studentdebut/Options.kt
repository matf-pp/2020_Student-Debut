package com.example.studentdebut

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Log.d
import android.view.View
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.studentdebut.Common.HTTPDataHandler
import com.example.studentdebut.Database.JobsViewModel
import com.example.studentdebut.Database.jobItem
import com.example.studentdebut.Model.RSSObject
import com.example.studentdebut.Model.Translator
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_options.*
import kotlinx.coroutines.*


class Options() : AppCompatActivity() {


    //Lista za cuvanje reci po kojima filtriramo
    val filters = mutableListOf<CharSequence>()

    //Funkcija koja cuva u listi filters sve checkboxove koji su obelezeni
    fun addInFilters(niz: List<CheckBox>) {

        for (i in niz)
            if (i.isChecked)
                filters.add(i.text)
    }

    private val RSS_to_JSON_API = "https://api.rss2json.com/v1/api.json?rss_url="
    /*

    "https://startit.rs/poslovi/feed/",
    "https://startit.rs/poslovi/feed/?paged=2",
        "https://startit.rs/poslovi/feed/?paged=3",
        "https://startit.rs/poslovi/feed/?paged=4",
        "https://startit.rs/poslovi/feed/?paged=5",
        "https://www.helloworld.rs/rss/",
        "http://oglasi.matf.bg.ac.rs/?feed=rss2",
        "http://oglasi.matf.bg.ac.rs/?tag=poslovi%26feed=rss2%26paged=2",
        "http://oglasi.matf.bg.ac.rs/?tag=poslovi%26feed=rss2%26paged=3",
        "http://oglasi.matf.bg.ac.rs/?tag=poslovi%26feed=rss2%26paged=4",
        "http://oglasi.matf.bg.ac.rs/?tag=poslovi%26feed=rss2%26paged=5",
        "http://www.sljaka.com/rss/itposlovi/",
        "https://fonis.rs/category/posao/feed/",
        "https://fonis.rs/category/praksa/feed/",
        "https://fonis.rs/category/praksa/feed/?paged=2",
        "http://www.itposlovi.info/rss/programeri/",
        "http://www.itposlovi.info/rss/dizajneri/"




    */

    //TODO dodati za stipendije link kod matfa
    //http://oglasi.matf.bg.ac.rs/?tag=stipendije&feed=rss2
    val rsslinks = mutableListOf(
        "http://oglasi.matf.bg.ac.rs/?feed=rss2"

    )

    //viewModel
    private lateinit var viewModel: JobsViewModel




    private fun makeConnection(link: String): String {

        val result: String
        val http = HTTPDataHandler()
        result = http.GetHTTPDataHandler(link).toString()

        return result
    }


    private suspend fun loadRSS(result: String): RSSObject {


         lateinit var rssObject: RSSObject

        withContext(Dispatchers.Default) {

            try {
                rssObject = Gson().fromJson<RSSObject>(result, RSSObject::class.java)
                //d("nullic","nije")
            }
            catch(e:Exception){

                //d("nullic","jeste")
                e.printStackTrace()
            }

            /*   try {
                    rssObject = Gson().fromJson<RSSObject>(result, RSSObject::class.java)
                }

                catch (e: IllegalStateException){
                 rssObject=null
                }*/
        }

        return rssObject
    }

    lateinit var view : View

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)


        view = findViewById(R.id.myProgressButton)
        Posao_ili_praksa.visibility = View.VISIBLE
        view.setOnClickListener() {
            UbaciFiltereJezici()
            val progressButton = ProgressButton(this@Options, view)
            progressButton.buttonActivated()
            val handler = Handler()
            handler.postDelayed({
                progressButton.buttonFinished()
                val handler1 = Handler()
                handler1.postDelayed({
                    val intent = Intent(this@Options, ListOfJobs::class.java)
                    startActivity(intent)
                },2000)
            },3000)

            //val i = Intent(this, ListOfJobs::class.java)
            //startActivity(i)
        }

        btn_Next_page1.setOnClickListener() {
            UbaciFilterePocetna()
        }
        btn_Next_pagePozicije.setOnClickListener() {
            UbaciFilterePozicija()
        }
        btn_Next_pageDestinacija.setOnClickListener() {

            UbaciFiltereMesto()
        }


        val uiScope = CoroutineScope(Dispatchers.IO)

        uiScope.launch {


            var rssObject: RSSObject


            rsslinks.forEach {

                val url_get_data = StringBuilder(RSS_to_JSON_API)
                url_get_data.append(it)
                //  d("url_get_data",url_get_data.toString())
                val result = async {
                    makeConnection(url_get_data.toString())
                }.await()


                rssObject = async {

                    loadRSS(result)

                }.await()

                 var link=it
                //iz rss u job item
                if(rssObject.items!=null)
                    d("done",MyApp.done.toString())
                    withContext(Dispatchers.Default) {
                        rssObject.items.forEach {
                            // TODO ova fja filterContent vadi tekst iz html-a
                            // samim tim ovaj kod pod komentarima u sledecem TODO-u vrvt nece biti potreban
                            // ako se odlucimo da fiksiramo duzinu bubble-a
                            d("linkk",link)
                            it.filterContent(link)
                            val positionf=addPosition(it.title)
                            val languagef= addLanguages(it.content)
                            val jobf=addJob(it.title)

                            val ajob = jobItem(
                                0,
                                it.title,
                                it.pubDate,
                                it.link,
                                it.description,
                                it.content,
                                jobf,
                                "",
                                positionf,
                                languagef
                            )
                            d("jobic",ajob.toString())

                            // d("item", ajob.toString())
                            MyApp.ListOfJobItems.add(ajob)

                        }

                    }
                }
            }
            MyApp.done=true

        Log.d(
            "done",
            "DOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOONEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE"
        )

    }


    private fun UbaciFiltereMesto() {

        Destinacija.visibility = View.INVISIBLE

        val layout: ConstraintLayout = findViewById(R.id.Pozicija)
        layout.visibility = View.VISIBLE
        val check_boxes = listOf(
            findViewById<CheckBox>(R.id.cb_Srbija),
            findViewById<CheckBox>(R.id.cb_inostranstvo)
        )

        addInFilters(check_boxes)
        // d("filteri", filters.toString())
    }
    private fun UbaciFilterePocetna() {

        Posao_ili_praksa.visibility = View.INVISIBLE

        val layout: ConstraintLayout = findViewById(R.id.Destinacija)
        layout.visibility = View.VISIBLE
        val check_boxes = listOf(
            findViewById<CheckBox>(R.id.cb_praksa),
            findViewById<CheckBox>(R.id.cb_posao),
            findViewById<CheckBox>(R.id.cb_stipendija)

            )
        addInFilters(check_boxes)
        // d("filteri", filters.toString())
    }
    private fun UbaciFilterePozicija() {


        Pozicija.visibility = View.INVISIBLE

        val layout: ConstraintLayout = findViewById(R.id.Jezici)
        layout.visibility = View.VISIBLE
        val check_boxes = listOf(
            findViewById<CheckBox>(R.id.cb_senior_developer),
            findViewById<CheckBox>(R.id.cb_junior_developer),
            findViewById<CheckBox>(R.id.cb_developer),
            findViewById<CheckBox>(R.id.cb_analyst),
            findViewById<CheckBox>(R.id.cb_graphic_designer),
            findViewById<CheckBox>(R.id.cb_web_designer),
            findViewById<CheckBox>(R.id.cb_tutor),
            findViewById<CheckBox>(R.id.cb_technical_lead),
            findViewById<CheckBox>(R.id.cb_backend_engineer),
            findViewById<CheckBox>(R.id.cb_frontend_engineer),
            findViewById<CheckBox>(R.id.cb_mobile_engineer),
            findViewById<CheckBox>(R.id.cb_software_engineer),
            findViewById<CheckBox>(R.id.cb_marketing),
            findViewById<CheckBox>(R.id.cb_ostalo)
        )
        addInFilters(check_boxes)
        //  d("filteri", filters.toString())


    }


    private fun UbaciFiltereJezici() {
        Jezici.visibility = View.INVISIBLE


        val check_boxes = listOf(
            findViewById<CheckBox>(R.id.cb_javascript),
            findViewById<CheckBox>(R.id.cb_net),
            findViewById<CheckBox>(R.id.cb_python),
            findViewById<CheckBox>(R.id.cb_mysql),
            findViewById<CheckBox>(R.id.cb_angular),
            findViewById<CheckBox>(R.id.cb_vue),
            findViewById<CheckBox>(R.id.cb_jquery),
            findViewById<CheckBox>(R.id.cb_wordpress),
            findViewById<CheckBox>(R.id.cb_c),
            findViewById<CheckBox>(R.id.cb_csharp),
            findViewById<CheckBox>(R.id.cb_nodejs),
            findViewById<CheckBox>(R.id.cb_kotlin),
            findViewById<CheckBox>(R.id.cb_htmlcss),
            findViewById<CheckBox>(R.id.cb_scala),
            findViewById<CheckBox>(R.id.cb_java),
            findViewById<CheckBox>(R.id.cb_php),
            findViewById<CheckBox>(R.id.cb_XML),
            findViewById<CheckBox>(R.id.cb_bash),
            findViewById<CheckBox>(R.id.cb_ostalo2)
        )
        addInFilters(check_boxes)


    }
    fun containsWord(inputString: String, items: List<String?>): String {
        var found = false
        var thing:String=" "
        for (item in items) {
            if (inputString.contains(item!!,true)) {
                thing=item
            }
        }
        return thing
    }
    fun containsWordsForLanguages(inputString: String, items: List<String?>): MutableList<String> {
        var found = false
        val yourArray: List<String> = inputString.split(" ")
        val things = mutableListOf<String>()
        for (item in items) {
            if (yourArray.contains(item!!)) {
                things.add(item)
            }
        }
        return things
    }
    private fun addPosition(input:String):String{

        val listofpositions= mutableListOf<String>(cb_senior_developer.text.toString(),cb_junior_developer.text.toString(),cb_developer.text.toString(),cb_analyst.text.toString(),
                                                  cb_graphic_designer.text.toString(),cb_web_designer.text.toString(),cb_tutor.text.toString(),cb_technical_lead.text.toString(),
                                                   cb_backend_engineer.text.toString(),cb_frontend_engineer.text.toString(),cb_mobile_engineer.text.toString(),cb_marketing.text.toString()
            )
        var position=containsWord(input,listofpositions)
      return position
    }
    private fun addLanguages(input:String):MutableList<String>{

        val listoflanguages= mutableListOf<String>(cb_javascript.text.toString(),cb_net.text.toString(),cb_python.text.toString(),  cb_mysql.text.toString(), cb_mysql.text.toString(),
            cb_vue.text.toString(),cb_jquery.text.toString(),cb_wordpress.text.toString(), cb_c.text.toString(), cb_csharp.text.toString(),
            cb_nodejs.text.toString() , cb_kotlin.text.toString(), cb_htmlcss.text.toString() , cb_htmlcss.text.toString(), cb_scala.text.toString(), cb_java.text.toString(),
            cb_php.text.toString(), cb_XML.text.toString(), cb_bash.text.toString()
        )
        var languages=containsWordsForLanguages(input,listoflanguages)
        return languages
    }
    private fun addJob(input:String):String{

        val listofpositions= mutableListOf<String>(cb_praksa.text.toString(),cb_posao.text.toString(),cb_stipendija.text.toString())
        var job=containsWord(input,listofpositions)
        return job
    }





}