package cat.dejocoarts.femelpas

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val ink = Color.rgb(35, 49, 60)
    private val accent = Color.rgb(32, 105, 106)
    private val bg = Color.rgb(246, 248, 246)
    private lateinit var root: LinearLayout
    private val prefs by lazy { getSharedPreferences("local_private_v1", MODE_PRIVATE) }
    private val titles = listOf("Aturar-me i calmar-me", "Entendre què ha passat", "Prendre jo la iniciativa", "Escoltar abans de defensar-me", "Reparar i concretar", "Fer seguiment")
    private val prompts = listOf(
        "Si estic alterat, faig una pausa i dic quan reprendré la conversa. No continuo cridant.",
        "Què he fet jo? Què he sentit? Puc sentir-me ferit sense justificar una mala resposta.",
        "No espero que l'altra persona comenci. Proposo parlar-ne, sense forçar-la.",
        "Pregunto com s'ha sentit. Escolto sense interrompre ni començar amb un «però».",
        "Demano perdó pel fet concret i identifico una acció observable per a la pròxima vegada.",
        "Comprovo si queden temes pendents. Si no hem pogut parlar, ho torno a proposar jo."
    )
    private val phrases = listOf(
        "Ara estic nerviós i no vull parlar-te així. Necessito una estona; després en parlem.",
        "Puc estar enfadat i reconèixer que no he actuat bé.",
        "M'agradaria parlar del que ha passat. No vull deixar-ho penjat.",
        "Entenc que t'hagi fet mal. M'agradaria escoltar com ho has viscut.",
        "No hauria d'haver-te contestat així. La pròxima vegada faré una pausa abans.",
        "Hi ha alguna cosa que ens hagi quedat pendent de parlar?"
    )
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); window.statusBarColor=accent; home() }
    private fun base(title: String): LinearLayout {
        val scroll=ScrollView(this).apply { setBackgroundColor(bg); fillViewport=true }
        root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(26,28,26,38) }
        scroll.addView(root); setContentView(scroll)
        text(title,26,true); return root
    }
    private fun text(s:String, size:Int=16, bold:Boolean=false):TextView {
        val v=TextView(this).apply { text=s; textSize=size.toFloat(); setTextColor(ink); if(bold) setTypeface(null,Typeface.BOLD); setPadding(0,9,0,10) }
        root.addView(v); return v
    }
    private fun button(label:String, action:()->Unit) {
        val b=Button(this).apply { text=label; isAllCaps=false; setTextColor(Color.WHITE); setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent)); setOnClickListener { action() } }
        root.addView(b,LinearLayout.LayoutParams(-1,-2).apply { topMargin=10 })
    }
    private fun field(hint:String, content:String=""):EditText {
        text(hint,14,true)
        val e=EditText(this).apply { setText(content); minLines=2; maxLines=5; gravity=Gravity.TOP; textSize=16f; setBackgroundColor(Color.WHITE); setPadding(15,12,15,12) }
        root.addView(e,LinearLayout.LayoutParams(-1,-2)); return e
    }
    private fun entries():JSONArray = try { JSONArray(prefs.getString("entries","[]")) } catch(_:Exception){JSONArray()}
    private fun saveEntries(a:JSONArray){prefs.edit().putString("entries",a.toString()).apply()}
    private fun date()=SimpleDateFormat("dd/MM/yyyy HH:mm",Locale("ca","ES")).format(Date())
    private fun home(){
        base("Fem el Pas")
        text("Una ajuda per no deixar els conflictes sense parlar.",17)
        val open=(0 until entries().length()).count { entries().optJSONObject(it)?.optBoolean("closed")==false }
        text("Converses pendents: $open",19,true)
        button("Hem discutit · començar") { newConflict() }
        button("Veure el protocol de 6 passos") { protocol() }
        button("Converses pendents i historial") { history() }
        button("Reflexió personal") { reflection() }
        text("Recordatori: assumir la meva part no implica exigir que l'altra persona parli o em perdoni.",14)
        text("Privat: aquesta versió desa les dades només al dispositiu. Sense internet ni compte.",13)
    }
    private fun protocol(){
        base("Protocol després d'una discussió")
        titles.forEachIndexed { i,t ->
            text("${i+1}. $t",20,true); text(prompts[i]); text("Puc dir: «${phrases[i]}»",15)
        }
        button("Tornar a l'inici"){home()}
    }
    private fun newConflict(){
        base("Començar una reparació")
        text("Primer, atura't. Si encara esteu alterats, no forcis una conversa.")
        val what=field("Què ha passat? (fets, sense culpabilitzar)")
        val felt=field("Què he sentit i com he reaccionat?")
        val own=field("Quina part de responsabilitat assumeixo?")
        val action=field("Quan i com proposaré reprendre la conversa?")
        button("Guardar com a conversa pendent") {
            val obj=JSONObject().put("date",date()).put("what",what.text.toString()).put("felt",felt.text.toString()).put("own",own.text.toString()).put("action",action.text.toString()).put("closed",false).put("repair","")
            val a=entries(); a.put(obj); saveEntries(a); history()
        }
        button("Consultar el protocol") { protocol() }
    }
    private fun history(){
        base("Converses i seguiment")
        val a=entries()
        if(a.length()==0) text("Encara no hi ha cap anotació.")
        for(i in a.length()-1 downTo 0){
            val o=a.optJSONObject(i)?:continue
            text("${if(o.optBoolean("closed")) "✓ Tancada" else "● Pendent"} · ${o.optString("date")}",18,true)
            text(o.optString("what").ifBlank { "Sense descripció" },15)
            button("Obrir aquesta conversa") { detail(i) }
        }
        button("Tornar a l'inici") { home() }
    }
    private fun detail(i:Int){
        val a=entries(); val o=a.optJSONObject(i)?:return
        base("Conversa · ${o.optString("date")}")
        listOf("Què ha passat" to "what", "Com m'he sentit" to "felt", "La meva responsabilitat" to "own", "Pla per reprendre-la" to "action").forEach { (label,key)->text(label,17,true);text(o.optString(key).ifBlank { "No anotat" }) }
        val repair=field("Què he fet per escoltar, reparar o fer seguiment?",o.optString("repair"))
        button("Guardar el seguiment") { o.put("repair",repair.text.toString()); saveEntries(a); detail(i) }
        if(!o.optBoolean("closed")) button("Marcar com a parlada / sense seguiment pendent") {
            AlertDialog.Builder(this).setTitle("Marcar com a tancada?").setMessage("Fes-ho només si no queda cap acció pendent per part teva. No cal que l'altra persona et perdoni.")
                .setNegativeButton("Cancel·lar",null).setPositiveButton("Confirmar") { _,_->o.put("closed",true);o.put("repair",repair.text.toString());saveEntries(a);history()}.show()
        }
        button("Tornar a l'historial"){history()}
    }
    private fun reflection(){
        base("Reflexió personal")
        text("No cal esperar a una discussió per identificar patrons.")
        val a=field("Què em sol activar?")
        val b=field("Què faig quan em sento injustament tractat?")
        val c=field("Què puc fer diferent la pròxima vegada?")
        button("Desar la reflexió") {
            val o=JSONObject().put("date",date()).put("what","Reflexió personal: ${a.text}").put("felt",b.text.toString()).put("own",c.text.toString()).put("action","Revisar el compromís abans del pròxim conflicte").put("closed",true).put("repair","")
            val list=entries();list.put(o);saveEntries(list);history()
        }
        button("Tornar a l'inici") { home() }
    }
    @Deprecated("Deprecated in Java") override fun onBackPressed(){home()}
}