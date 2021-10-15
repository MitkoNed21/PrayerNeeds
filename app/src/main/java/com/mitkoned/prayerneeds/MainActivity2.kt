package com.mitkoned.prayerneeds

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri.parse
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.style.TtsSpan
import android.util.JsonReader
import android.util.JsonWriter
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONStringer
import org.json.JSONTokener
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Collections.sort
import java.util.Date.parse
import java.util.logging.Level.parse
import kotlin.reflect.typeOf

class MainActivity2 : AppCompatActivity() {
    private lateinit var addNameDialog : AlertDialog
    private lateinit var etName_AddNameDialog : EditText
    private lateinit var rvAddedNames_AddNameDialog : RecyclerView
    private lateinit var namesAddedTodayFile: File

    private val names = mutableListOf<String>()
    private val namesAddedToday = mutableListOf<String>()
    private lateinit var dateFormat: DateFormat

    val items = mutableMapOf<String, MutableList<PrayerNeed>>()

/*
    private val Илия = mutableListOf(
        PrayerNeed("Една негова баба да повярва", "2021", "2 Apr", false),

        PrayerNeed("Да бъдем по-близо до Бог", "2021", "16 Apr", false),
        PrayerNeed("За матурите", "2021", "16 Apr", false),
        PrayerNeed("Да търси повече Бог", "2021", "16 Apr", false),
        PrayerNeed("Негови роднини са отпаднали, да започнат отново да ходят на църква", "2021", "16 Apr", false),

        PrayerNeed("Да му помага за предстоящото състезание и Бог да му дава сили там", "2021", "14 May", false)
    )
    private val Пацо = mutableListOf<PrayerNeed>()
    private val Тошко = mutableListOf<PrayerNeed>()
    private val Пепи = mutableListOf(
            PrayerNeed("Да се оправят нещата свързани с един сайт", "2021", "2 Apr", false),

            PrayerNeed("За библейското, Бог да помогне за подготовката на отбора", "2021", "9 Apr", false),

            PrayerNeed("Духовна нужда", "2021", "23 Apr", false),
            PrayerNeed("Бог да му помага, ако е Неговата воля, да учи немски език, за да отиде в Германия", "2021", "23 Apr", false),
            PrayerNeed("За Библейското състезание", "2021", "23 Apr", false),
            PrayerNeed("Това което учат за състезанието, да им помага в живота", "2021", "23 Apr", false),


            PrayerNeed("Бог да го използва пред съучениците му", "2021", "14 May", false),
            PrayerNeed("за подготовката с немския език", "2021", "14 May", false)
    )
    private val Несито = mutableListOf(
            PrayerNeed("Да бъде напълно възстановена", "2021", "26 Mar", false),
            PrayerNeed("Да бъде напълно премахната болка в стъпалото", "2021", "2 Apr", false),

            PrayerNeed("За дипломната ѝ работа и държавния ѝ изпит", "2021", "9 Apr", false),
            // Дипломната е завършена

            PrayerNeed("За защита на дипломната и за държавния изпит", "2021", "23 Apr", false),


            PrayerNeed("За здравословното състояние на Габито", "2021", "14 May", false),
            PrayerNeed("Нужда, свързана със защитата на дипломната ѝ работа и изпита ѝ за университета", "2021", "14 May", false)
    )
    private val Марто_Петков = mutableListOf(
            PrayerNeed("Планира да кандидатства в университет, ако е Божията воля да бъде приет", "2021", "26 Mar", false)
            ,
            PrayerNeed("Габито по-бързо да премине през болките свързани с нейното физическо състояние", "2021", "2 Apr", false)
    ,PrayerNeed("Да дава сили и мъдрост на него и Несито, да им помага", "2021", "2 Apr", false)




    )
    private val Емили = mutableListOf(
            PrayerNeed("Лична нужда", "2021", "26 Mar", false),



            PrayerNeed("Нужда от здравословен характер", "2021", "14 May", false)

    )
    private val Жужка = mutableListOf(
            PrayerNeed("За Ева", "2021", "26 Mar", false),
            PrayerNeed("Бог да й помогне да не се случи нещо което е преживяла отново", "2021", "2 Apr", false),

            PrayerNeed("Бог да даде спокойствие и мир на брат й да успее да се подготви за утре", "2021", "9 Apr", false),
            PrayerNeed("Бог да й помага да се бори с мислите си", "2021", "9 Apr", false),
            PrayerNeed("За библейското", "2021", "9 Apr", false),

            PrayerNeed("Майка й има проблем свързан с главата", "2021", "16 Apr", false),



            PrayerNeed("Лична нужда за промяна в самата нея", "2021", "14 May", false)
    )
    private val Миленка = mutableListOf(
            PrayerNeed("Две здравословни нужди за баща й", "2021", "26 Mar", false),
            PrayerNeed("За Йоана Койнарска, която е претърпяла операция за премахване на апандисит", "2021", "2 Apr", false),

            PrayerNeed("Лична нужда от духовен характер", "2021", "9 Apr", false),

            PrayerNeed("Баща й има болки в кръста", "2021", "16 Apr", false),
            PrayerNeed("Нужда от битов характер", "2021", "16 Apr", false) // за интернтета, разрешена?




    )
    private val Йони_Михайлова = mutableListOf(
            PrayerNeed("Университета, натоварени три седмици", "2021", "26 Mar", false),
            PrayerNeed("Духовна нужда", "2021", "26 Mar", false),
            PrayerNeed("Здравословна нужда", "2021", "26 Mar", false),

            PrayerNeed("Бог да е в контрол по време на операцията на баща й", "2021", "9 Apr", false),

            PrayerNeed("За възстановяване на баща й", "2021", "16 Apr", false),
            PrayerNeed("За следващата седмица Бог да й дава мъдрост как са си разпределя времето", "2021", "16 Apr", false),

            PrayerNeed("За изпита й в сряда, за който са й казали в последния момент", "2021", "23 Apr", false),



            PrayerNeed("Нужда от духовен характер", "2021", "14 May", false),
            PrayerNeed("За болките в крака на майка й", "2021", "14 May", false),
            PrayerNeed("За изпитите и контролните в университета", "2021", "14 May", false),

            PrayerNeed("Миро Вачев е болен и от 2 седмици приема антибиотици, не е сигурно дали е Ковид, но се влошава", "2021", "21 May", false)
    )
    private val Борито = mutableListOf(PrayerNeed("Здравословна нужда", "2021", "26 Mar", false))
    private val Сара = mutableListOf(
            PrayerNeed("Здравословна нужда", "2021", "26 Mar", false),
            PrayerNeed("Да бъде по-отговорна в ученето, да има мотивация", "2021", "2 Apr", false),

            PrayerNeed("Предстои й изпит, Бог да й помогне да мине добре всичко и да бъде по-отговорна в подготвянето", "2021", "16 Apr", false),



            PrayerNeed("За предстоящата сесия в университета и правилно разпределение на времето й", "2021", "14 May", false)
    )
    private val Илиян = mutableListOf(
            PrayerNeed("За решение дали да бъде тук или не за цяла година", "2021", "26 Mar", false),

            PrayerNeed("Да вземе правилното решение дали да отсъства за цяла година", "2021", "2 Apr", false),

            PrayerNeed("Да вземе правилното решение дали да отсъства за цяла година", "2021", "9 Apr", false),


            PrayerNeed("Да бъде изцелен напълно", "2021", "23 Apr", false),
            PrayerNeed("Да бъде Божията воля относно пътуването и престоя му в Англия", "2021", "23 Apr", false)




    )
    private val Еми_Лискова = mutableListOf(
            PrayerNeed("Да бъде по-отговорна в училище", "2021", "2 Apr", false),

            PrayerNeed("Бог да й помогне за изпита по български език", "2021", "23 Apr", false),
            PrayerNeed("Лична нужда", "2021", "23 Apr", false)




    )
    private val Нати = mutableListOf(
            PrayerNeed("За здраве на семейството", "2021", "26 Mar", false),

            PrayerNeed("Да се възстанови баба й", "2021", "9 Apr", false),
            PrayerNeed("Бог да бъде с нея и да й дава сили", "2021", "9 Apr", false)




    )
    private val Танчето = mutableListOf(
            PrayerNeed("Семейството й да повярва, Бог да докосне сърцата им и да ги обърне към Себе Си", "2021", "26 Mar", false),
            PrayerNeed("Бог да я насочи към най-правилното място на работа", "2021", "26 Mar", false)




    )
    private val Емчето = mutableListOf(
            PrayerNeed("Бабата на нейна приятелка да бъде напълно изцелена", "2021", "26 Mar", false),
            PrayerNeed("Баща й да е изцелен", "2021", "26 Mar", false),
            PrayerNeed("Неповярвалите от семейството й да повярват", "2021", "26 Mar", false),

            PrayerNeed("Баща й да се възстанови съвършено и да повярва", "2021", "2 Apr", false),
            PrayerNeed("Бабата на нейна приятелка, която беше представена миналия път, да повярва", "2021", "2 Apr", false),

            PrayerNeed("Бог да я изцели съвършенно", "2021", "16 Apr", false),
            PrayerNeed("Бог да им дава мъдрост и сили на нея и Йоската", "2021", "16 Apr", false),
            PrayerNeed("За невярващите хора около нея, Бог да ги срещне и да бъдат спасени", "2021", "23 Apr", false),

            PrayerNeed("Намерили са на дъщерята на нейна колежка злокачествено образувание на млечната жлеза", "2021", "12 May", false),

            PrayerNeed("Дъщерята на нейна колежка е претърпяла операция + за бъдещите резултати от изследванията на момичето", "2021", "14 May", false),
            PrayerNeed("близки и приятели, които не са познали Бог", "2021", "14 May", false)
    )
    private val Йоската = mutableListOf(
            PrayerNeed("Лична нужда свързана с избор, Бог да му даде мъдрост за правилното решение", "2021", "26 Mar", false)
            ,

            PrayerNeed("Баба му, която е на село е сама, Бог да я подкрепя и да бъде спасена", "2021", "2 Apr", false)
            ,PrayerNeed("Лична нужда", "2021", "2 Apr", false),

            PrayerNeed("Лична нужда", "2021", "16 Apr", false),
            PrayerNeed("Лична нужда", "2021", "23 Apr", false), // същата



            PrayerNeed("изпитва дискомфорт в крака и кръста", "2021", "14 May", false),
            PrayerNeed("Лична нужда", "2021", "14 May", false)
    )
    private val Дидо_Киров = mutableListOf(
            PrayerNeed("Родителите на негов приятел болни от Ковид, за бъдат изцелени", "2021", "26 Mar", false),
            PrayerNeed("Да върши работата си като за пред Бог", "2021", "26 Mar", false),
            PrayerNeed("Лична нужда", "2021", "26 Mar", false),


                    PrayerNeed("За майката на Стефан", "2021", "2 Apr", false),
                    PrayerNeed("Да се наредят нещата свързани с Кения, ако е Божията воля да отидат", "2021", "2 Apr", false),

            PrayerNeed("Бог да промисли за нещата свързани с лекциите", "2021", "9 Apr", false),
            PrayerNeed("За баща му, пътува за Австрия", "2021", "9 Apr", false),

            PrayerNeed("Бог да му помага да бъде по-организитан", "2021", "16 Apr", false),
            PrayerNeed("По-малко да закъснява", "2021", "16 Apr", false), //ture?
            PrayerNeed("За университета", "2021", "16 Apr", false)
    )
    private val Митко = mutableListOf(
            PrayerNeed("Лична нужда от духовен характер", "2021", "26 Mar", false),
            PrayerNeed("Един дядо от квартала му, да повярва, ако е Божията воля", "2021", "26 Mar", false),

            PrayerNeed("Да бъде по отговорен през третия триместър", "2021", "2 Apr", false),

            PrayerNeed("Да бъде отговорен в нещата свързани с вярата", "2021", "9 Apr", false),

            PrayerNeed("Да не бъдем безчувствени към греха", "2021", "16 Apr", false),

            PrayerNeed("Да бъде Божията воля относно дали да продължи обучението му онлайн", "2021", "23 Apr", false),
            PrayerNeed("Да може да си разпределя времето правилно", "2021", "23 Apr", false),
            PrayerNeed("Сара утре има изпит", "2021", "23 Apr", false),
            PrayerNeed("Лична нужда", "2021", "23 Apr", false)




    )
    private val Мишо_Стоянов = mutableListOf(PrayerNeed("Лична нужда", "2021", "26 Mar", false))
    private val Гошката = mutableListOf(
            PrayerNeed("Лични нужди", "2021", "9 Apr", false),
            PrayerNeed("Лични нужди", "2021", "16 Apr", false),


            PrayerNeed("Лична нужда", "2021", "14 May", false)
    )
    private val Филката = mutableListOf(
            PrayerNeed("Бог да пази семейството му", "2021", "26 Mar", false),
            PrayerNeed("Бащата на Стефан не е добре", "2021", "26 Mar", true),
            PrayerNeed("Леля Краси от църква я боли кръста", "2021", "26 Mar", false), //probably answered
            PrayerNeed("За Германия, Бог да нареди нещата със заминаването му", "2021", "26 Mar", false)
    )
    private val Еми_Досева = mutableListOf<PrayerNeed>()
    private val Ивайло = mutableListOf(
            PrayerNeed("Да не продължава локдауна", "2021", "26 Mar", false),
            PrayerNeed("Баба му е претърпяла операция на полянка става, Бог да я възстанови напълно", "2021", "26 Mar", false),

            PrayerNeed("За баба му, била е оперирана, да се възстанови успешно", "2021", "16 Apr", false)
    )
    private val Ева = mutableListOf(
            PrayerNeed("Има хрема", "2021", "26 Mar", false), // Не е била на молитвено
            PrayerNeed("Много я боли крака", "2021", "26 Mar", false), // Не е била на молитвено

            PrayerNeed("На сестра й й предстои важен разговор с шефа й", "2021", "2 Apr", false),
            PrayerNeed("Бог да им дава сили в семейството да се молят за нужди", "2021", "2 Apr", false),
            PrayerNeed("Да бъде изцелено кучето й", "2021", "2 Apr", false),
            PrayerNeed("Бог да нареди нещата свързани с пътуване до Италия", "2021", "2 Apr", false),
            PrayerNeed("Бог да бъде с вуйчо й и бъдещата му съпруга", "2021", "2 Apr", false),
            PrayerNeed("Родителите на Виктория Громова да не направят проблем на сватбата й", "2021", "2 Apr", false),
            PrayerNeed("Да повярват родителите на Виктория", "2021", "2 Apr", false),
            PrayerNeed("За сестрата на Виктория", "2021", "2 Apr", false),

            PrayerNeed("Нужди", "2021", "9 Apr", false),
            PrayerNeed("За Ива", "2021", "9 Apr", false),

            PrayerNeed("За здравословното й състояние", "2021", "16 Apr", false),
            PrayerNeed("Сестра й да се възстанови от травмата, от претърпяна катастрофа", "2021", "16 Apr", false),
            PrayerNeed("Хората от другата кола да са добре", "2021", "16 Apr", false),

            PrayerNeed("Майка й не е добре здравословно, има проблеми със съня", "2021", "16 Apr", false),
            PrayerNeed("За нейното здравословно състояние", "2021", "23 Apr", false),
            PrayerNeed("Жена, казва се Роси, е в депресия, да се възстанови", "2021", "23 Apr", false)





    )
    private val Денито = mutableListOf(
            PrayerNeed("За държавните изпити, които й предстоят", "2021", "9 Apr", false),
            PrayerNeed("Бог да й помага да си разпределя времето по най-правилния начин\n", "2021", "9 Apr", false)
    )
    private val Йони_Славчева = mutableListOf<PrayerNeed>()
    private val Мишо_Александров = mutableListOf(
            PrayerNeed("Твърде много си уморява очите", "2021", "16 Apr", false),
            PrayerNeed("Замайва се от време на време, има болки в горната част на врата", "2021", "16 Apr", false),

            PrayerNeed("Другата седмица ще му бъде натоварена", "2021", "23 Apr", false)



    )
    private val Верчето = mutableListOf(PrayerNeed("Нужда от духовен характер", "2021", "16 Apr", false))
    private val Стефко = mutableListOf(
            PrayerNeed("Дядо му има положителен тест за Ковид", "2021", "16 Apr", false),
            PrayerNeed("За баща му, да се възстанови", "2021", "16 Apr", false),
            PrayerNeed("Да бъде Божията воля относно дадени взаимоотношения", "2021", "16 Apr", false),
            PrayerNeed("Да вземе правилно решение относно нещо", "2021", "16 Apr", false),

            PrayerNeed("Дидо има нужда, свързана с работата му", "2021", "23 Apr", false),


            PrayerNeed("Бог да дава мъдрост на Дидо относно работата", "2021", "14 May", false),
            PrayerNeed("Да вземе правилно решение къде да работи през лятото", "2021", "14 May", false)
    )
    private val Самчо = mutableListOf(
            PrayerNeed("Баба ми не е добре здравословно, Бог да я подкрепи", "2021", "23 Apr", false),
            PrayerNeed("На момче му предстои операция, роднина на негов колега, на който е говорел за Бог", "2021", "23 Apr", false)



    )
    private val Инка = mutableListOf(PrayerNeed("Илиян е с Ковид, в тежко състояние, от днес е преместен в реанимация", "2021", "2 May", false))
    private val Общи = mutableListOf(
            PrayerNeed("Бог да благослови вярващите с мъдрост", "2021", "26 Mar", false),
            PrayerNeed("За заседналия кораб в Суетския канал", "2021", "26 Mar", false),
            PrayerNeed("За психичното здраве на хората", "2021", "26 Mar", false),
            PrayerNeed("За евреите, да повярват", "2021", "26 Mar", false),
            PrayerNeed("За правилно разбиране на Божието Слово", "2021", "26 Mar", false),

            PrayerNeed("За страхуващите се от разболяване да идват на църква", "2021", "2 Apr", false),
            PrayerNeed("За отпадналите", "2021", "2 Apr", false),
            PrayerNeed("За изборите", "2021", "2 Apr", false),
            PrayerNeed("За Израел", "2021", "2 Apr", false),


            PrayerNeed("За близките на DMX и за феновете му", "2021", "9 Apr", false),
            PrayerNeed("За организирането на библейското състезание, Бог да дава мъдрост на организаторите", "2021", "9 Apr", false),
            PrayerNeed("Да се отпуши Суетския канал", "2021", "9 Apr", false), //??
            PrayerNeed("За дисциплина в църквата за тези, на които им липсва", "2021", "9 Apr", false),
            PrayerNeed("За тези, които ги няма, но по принцип идват", "2021", "9 Apr", false),
            PrayerNeed("За невярвашите", "2021", "9 Apr", false),
            PrayerNeed("За Израел", "2021", "9 Apr", false),
            PrayerNeed("За последните дни", "2021", "9 Apr", false),
            PrayerNeed("За бежанците, мисионерите", "2021", "9 Apr", false),

            PrayerNeed("За времето, в което живеем", "2021", "16 Apr", false),
            PrayerNeed("Да бъдем готови, когато Бог дойде, да бъдем заедно с Него на небето", "2021", "16 Apr", false),
            PrayerNeed("За управниците", "2021", "16 Apr", false),
            PrayerNeed("Да имаме тих и спокоен живот", "2021", "16 Apr", false),

            PrayerNeed("За тези, които са болни, да поздравят", "2021", "23 Apr", false),
            PrayerNeed("За тези, които са боледували от Ковид, да бъдат съвършено възстановени", "2021", "23 Apr", false),
            PrayerNeed("За държавата ни", "2021", "23 Apr", false),
            PrayerNeed("За чичо Веско", "2021", "23 Apr", true),

            PrayerNeed("За пълното възстановяване на Илиянчо", "2021", "14 May", false),
            PrayerNeed("За Израел", "2021", "14 May", false),
            PrayerNeed("За бъдещите избори и решенията на сегашното служебно правителство", "2021", "14 May", false)
    )
*/

    fun prayerNeedToJson(prayerNeed: PrayerNeed) : JSONObject {
        val result = JSONObject().apply {
            put("date", prayerNeed.date)
            put("year", prayerNeed.year)
            put("description", prayerNeed.description)
            put("answered", prayerNeed.answered)
        }

        return result
    }

    private fun saveNamesAddedTodayFile() {
        if (namesAddedToday.size == 0) {
            return
        }

        lateinit var writer: FileWriter
        try {
            namesAddedTodayFile.createNewFile()
            writer = FileWriter(namesAddedTodayFile.path, false)

            val result = StringBuilder()
            result.append(dateFormat.format(Calendar.getInstance()/*.apply{ this.set(Calendar.DAY_OF_MONTH, 26) }*/.time))
            result.append("|")
            result.append(namesAddedToday.joinToString(", "))

            writer.write(result.toString())
        } catch (e: Exception) {
            Toast.makeText(this, e.stackTrace.joinToString { "\n" }, Toast.LENGTH_LONG).show()
        } finally {
            try {
                writer.flush()
                writer.close()
            } catch (e: Exception) {
                Toast.makeText(this, e.stackTrace.joinToString { "\n" }, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun switchDataDirectories(s: String) {
        try {
            val testFolderName = "/bbbbb"
            val folderName = "/Names2"

            var switchTo = ""
            var switchFrom = ""
            if (s == "test") {
                switchTo = testFolderName
                switchFrom = folderName
            } else {
                switchTo = folderName
                switchFrom = testFolderName
            }

            val dir = File(filesDir.path + switchFrom)
            dir.mkdir()

            filesDir.listFiles { file -> file.isFile }?.forEach {
                if (!(it.name.contains("bbbb") || it.name.contains("Name"))) {
                    it.copyTo(File(dir.path + "/" + it.name), true)
                    it.delete()
                }
            }

            File(filesDir.path + switchTo).listFiles { file -> file.isFile }?.forEach {
                it.copyTo(File(filesDir.path + "/" + it.name))
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

/*
        switchDataDirectories("")

        Toast.makeText(this, "/ "  +File(filesDir.path).listFiles()?.size.toString(), Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "/<files> "  +File(filesDir.path).listFiles { file -> file.isFile }?.size.toString(), Toast.LENGTH_SHORT).show()

        Toast.makeText(this, "/Names2 " +File(filesDir.path + "/Names2").listFiles()?.size.toString(), Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "/bbbbb " +File(filesDir.path + "/bbbbb").listFiles()?.size.toString(), Toast.LENGTH_SHORT).show()

        return
        // */

        dateFormat = SimpleDateFormat(getString(R.string.dateFormat), Locale.US)

        lateinit var reader: FileReader
        try {
            namesAddedTodayFile = File(filesDir.path + "/today.txt")

            if (!namesAddedTodayFile.exists()) {
                namesAddedTodayFile.createNewFile()

                Toast.makeText(this, "FILE NOT FOUND", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "FILE FOUND", Toast.LENGTH_SHORT).show()
                reader = FileReader(namesAddedTodayFile.path)
                val input = reader.readText()
                val inputParts = input.split('|')
                if (input != "")
                {
                    val dateString = inputParts[0]
                    val names = inputParts[1].split(", ")
                    val currentDateString = dateFormat.format(Calendar.getInstance().time)

                    if (dateString == currentDateString) {
                        //namesAddedTodayFile.delete()
                        namesAddedToday.addAll(0, names)
                    } else {
                        namesAddedTodayFile.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.stackTrace.joinToString { "\n" }, Toast.LENGTH_SHORT).show()
        } finally {
            try {
                //if (reader != null && reader.ready())
                    reader.close()
            } catch (e: Exception) {
                Toast.makeText(this, e.stackTrace.joinToString { "\n" }, Toast.LENGTH_SHORT).show()
            }
        }
        //val testFile = filesDir.path + "/test.json";
        //val contents = testList.joinToString("\n") {
        //    "${it.date} ${it.year};${it.description};${it.answered}"
        //}

        //val file = ""
        //read file
        // parse to json object
//        val ob = JSONObject()
//        ob.put("prayerNeeds", JSONArray().apply {
//            for (el in testList) {
//                put(prayerNeedToJson(el))
//            }
//        })
//        Toast.makeText(this, ob.toString(), Toast.LENGTH_LONG).show()
//
//
//        val firstNeedJson = prayerNeedToJson(testList[0])
//        ob.getJSONArray("prayerNeeds")
//            .put(firstNeedJson)

//        lateinit var writer: FileWriter
//        try {
//            writer = FileWriter(testFile, false)
//            writer.write(ob.toString())
//        } catch (e: Exception) {
//            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
//        } finally {
//            try {
//                writer.flush()
//                writer.close()
//            } catch (e: Exception) {
//                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
//            }
//        }

        //val r = FileReader(testFile)
        //val result = r.readText()
        //Toast.makeText(this, result, Toast.LENGTH_LONG).show()
        //r.close()
        //return

        //val testString = "{ \"prayerNeeds\": [ {\"test\": [ { \"test\": [ { \"i\": 1 }, { \"i\": 2 } ] } ] } ] }"
        //val fileInput = ""
        //val s = JSONObject(testString)
        //val allNeeds = s.getJSONArray("prayerNeeds")
        //allNeeds.put(JSONObject().apply {
        //    put("year", 2021)
        //    put("date", "21 May")
        //    put("description", "test")
        //    put("answered", false)
        //})


        //Toast.makeText(this, allNeeds.toString(), Toast.LENGTH_SHORT).show()
        //return
        //s

//        val toString = JSONStringer()
//            .`object`()
//                .key("prayerNeeds").array()
//                    .`object`().key("2021").array()
//                        .`object`().key("25 May").array()
//                            .`object`()
//                                .key("description").value("test")
//                                .key("answered").value(false)
//                            .endObject()
//                            .`object`()
//                                .key("description").value("test3")
//                                .key("answered").value(true)
//                            .endObject()
//                        .endArray().endObject()
//                        .`object`().key("24 May").array()
//                            .`object`()
//                                .key("description").value("test2")
//                                .key("answered").value(true)
//                            .endObject()
//                        .endArray().endObject()
//                    .endArray().endObject()
//                .endArray()
//            .endObject()
//            .toString()

        //val parse = dateFormat.parse(testList[0].date + ";" + testList[0].year)
        //testList.sortBy() { el -> el.date }
        //testList.sortWith(kotlin.Comparator(fun (first, second): Int {
        //    val firstDate = dateFormat.parse(first.date + ";" + first.year)
        //    val secondDate = dateFormat.parse(second.date + ";" + second.year)

        //    return firstDate!!.compareTo(secondDate)
        //}))
        //val parse = testList[1].date//.toString()

        //TODO: maybe use date in PrayerNeed class
        //Toast.makeText(this, parse, Toast.LENGTH_LONG).show()

        //Toast.makeText(this, toString, Toast.LENGTH_LONG).show()
        //return

        val dialogView = LayoutInflater.from(this).inflate(R.layout.addname_dialog, null)
        val rvNamesAddedToday = findViewById<RecyclerView>(R.id.rv_names)
        rvNamesAddedToday.adapter = AddedNameTodayElementAdapter(namesAddedToday)
        rvNamesAddedToday.layoutManager = LinearLayoutManager(this)

        //val dialogView = View.inflate(this, R.layout.addname_dialog, null)
        //TODO: use MultiAutoCompleteTextView

        //TODO: have checkbox next to name when adding so can select
        // multiple names and add them all at once, maybe then sort

        addNameDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add name")
            .setPositiveButton("Add") { _, _ ->
               val selectedName = etName_AddNameDialog.text.toString()
                if (!namesAddedToday.contains(selectedName) && selectedName != "") {
                    namesAddedToday.add(selectedName)
                    rvNamesAddedToday.adapter?.notifyItemInserted(namesAddedToday.size - 1)
                    rvNamesAddedToday.adapter?.notifyItemChanged(namesAddedToday.size - 2)

                    val fileName = filesDir.path + "/" + selectedName + ".json"
                    try {
                        File(fileName).createNewFile()
                    } catch (e: Exception) {
                        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

                saveNamesAddedTodayFile()
                //val directories = filesDir.listFiles(FileFilter { pathname -> pathname.isDirectory })
//                if (ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//
//                }

                //TODO: if app is quit or switching to another activity
                // (see if switching to another activity and going back
                // saves the names in the list) save today's added names
                // unless list is empty,
                // if person has no prayer needs offered, keep name
                // save also date, upon opening, if date is different
                // than saved to file (or maybe last accessed)
                // (maybe move list of names to history) clear list

                //TODO: second activity could have a button to
                // show/hide prayer needs that are answered

                //TODO: add a way to see all prayer needs ever offered
                // it could be done like the main screen (list of names
                // in alphabetical order, which when clicked launch
                // another activity (could reuse MainActivity) with
                // all the prayer needs offered by the person)
            }
            .setOnDismissListener {
                Toast.makeText(this, etName_AddNameDialog.text.toString(), Toast.LENGTH_SHORT).show()
                //TODO: Search in files for file with text in EditText
                //

                etName_AddNameDialog.setText("")
            }
            .create()

        etName_AddNameDialog = dialogView.findViewById(R.id.et_name)

        rvAddedNames_AddNameDialog = dialogView.findViewById(R.id.rv_addedNames)
        val adapter = AddNameDialogElementAdapter(names, addNameDialog)
        rvAddedNames_AddNameDialog.adapter = adapter
        rvAddedNames_AddNameDialog.layoutManager = LinearLayoutManager(addNameDialog.context)

    }

    private fun getPrayerNeedsFromFile(file: File): MutableList<PrayerNeed> {
        val result = mutableListOf<PrayerNeed>()
        lateinit var reader: FileReader
        lateinit var inputJsonString: String

        try {
            reader = FileReader(file)
            inputJsonString = reader.readText()
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        } finally {
            try {
                reader.close()
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }
        }

//        if (JsonReader(StringReader(inputJsonString)).hasNext())
        if (inputJsonString != "") {
            try {
                val jsonObject = JSONObject(inputJsonString)

                if (jsonObject.has("prayerNeeds")) {
                    val items = jsonObject.getJSONArray("prayerNeeds")
                    val itemsLength = items.length()

                    for (index in (0 until itemsLength)) {
                        val currentObject = items.getJSONObject(index)

                        val date = currentObject.getString("date")
                        val year = currentObject.getString("year")
                        val description = currentObject.getString("description")
                        val answered = currentObject.getBoolean("answered")

                        result.add(
                            PrayerNeed(
                                description = description,
                                date = date,
                                year = year,
                                answered = answered
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }
        }

        return result
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {

            R.id.addName_mi -> {
                val files = filesDir.listFiles()
                { file ->
                    file.extension == "json"
                }

                if (files != null) {
                    val fileNames = files
                            .map { f -> f.nameWithoutExtension }
                            .filter { fn -> !namesAddedToday.contains(fn) }
                            .sortedBy { fn -> fn }

                    names.clear()
                    rvAddedNames_AddNameDialog.adapter?.notifyItemRangeRemoved(0, rvAddedNames_AddNameDialog.childCount)

                    for (fn in fileNames) {
                        names.add(fn)

                        rvAddedNames_AddNameDialog.adapter?.notifyItemInserted(names.size - 1)
                        rvAddedNames_AddNameDialog.adapter?.notifyItemChanged(names.size - 2)
                    }
                }

                addNameDialog.show()
            }
            R.id.showPrayerNeedsEnteredToday_mi -> {
                try {
                    val intent = Intent(this, ViewTodaysPrayerNeedsFullscreenActivity::class.java).apply {
                        putExtra("namesAddedToday", namesAddedToday.toTypedArray())
                    }

                    this.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, e.stackTrace.joinToString("\n"), Toast.LENGTH_LONG).show()
                }
            }
            R.id.viewAllNames_mi -> {
                this.startActivity(Intent(this, MainActivity3::class.java))
            }
        }

        return true
    }
}