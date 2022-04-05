package kr.co.si312.exchangerate

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.text.Editable
import android.util.Log
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    val country = arrayOf("한국 (KRW)","일본 (JPY)","필리핀 (PHP)")
    var countryRate = arrayOf(0.0,0.0,0.0)
    var nowSel = " KRW "
    private val TAG = "MainActivity"
    val deciaml = DecimalFormat("#,###.##")

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //picker 설정
        selType.minValue = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selType.textSize = 100F
        }
        selType.maxValue = country.size - 1
        selType.displayedValues = country
        selType.wrapSelectorWheel = false
        selType.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        //초기화
        nowTime()
        getRate()
        exRate.text = "0"
    }
    override fun onResume() {
        super.onResume()

        //국가변경시 수취국가, 환율 변경
        selType.setOnValueChangedListener { _, _, i2 ->
            when (i2) {
                0 -> {
                    getCountry.text = country[0]
                    exRate.text = deciaml.format(countryRate[0])
                    rateCountry.text = " KRW / USD"
                    nowSel = " KRW "
                    }
                1 -> {
                    getCountry.text = country[1]
                    exRate.text = deciaml.format(countryRate[1])
                    rateCountry.text = " JPY / USD"
                    nowSel = " JPY "
                    }
                2 -> {
                    getCountry.text = country[2]
                    exRate.text = deciaml.format(countryRate[2])
                    rateCountry.text = " PHP / USD"
                    nowSel = " PHP "
                }
            }
            getTotalMoney()
        }

        moneyRate.addTextChangedListener {
            getTotalMoney()
        }

    }

    //조회시간 가져오기
    private fun nowTime(){
        val sdf = SimpleDateFormat("yyyy/MM/dd  hh:mm")
        val currentDate = sdf.format(Date())
        getTime.text = currentDate
    }

    //환율 가져오기
    private fun getRate(){
        thread(start = true) {
            try {
                val url =
                    URL("http://api.currencylayer.com/live?access_key=86b289b8574d9c310947aef6f70060b5")
                val netConn = url.openConnection() as HttpURLConnection
                if (netConn.responseCode == HttpURLConnection.HTTP_OK) {
                    val streamReader = InputStreamReader(netConn.inputStream)
                    val buffered = BufferedReader(streamReader)

                    val content = StringBuilder()
                    while (true) {
                        val line = buffered.readLine() ?: break
                        content.append(line)
                    }
                    buffered.close()
                    netConn.disconnect()
                    runOnUiThread {
                        Log.d(TAG,content.toString())
                        var usdRate = JSONObject(content.toString()).getJSONObject("quotes")
                        countryRate[0] = usdRate.getDouble("USDKRW")
                        countryRate[1] = usdRate.getDouble("USDJPY")
                        countryRate[2] = usdRate.getDouble("USDPHP")
                        //파싱완료시 초기값 입력
                        exRate.text = countryRate[0].toString()
                    }
                }
            } catch (e: Exception) {e.printStackTrace()}
        }
    }

    //수취액 가져오기
    private fun getTotalMoney(){
        if (moneyRate.text.toString() == ""){
            totalMoney.text = "수취금액은 0"+ nowSel + "입니다."
        } else {
            when (nowSel) {
                " KRW " -> totalMoney.text = "수취금액은 " + deciaml.format(moneyRate.text.toString().toInt() * countryRate[0]) + nowSel + "입니다."
                " JPY " -> totalMoney.text = "수취금액은 " + deciaml.format(moneyRate.text.toString().toInt() * countryRate[1]) + nowSel + "입니다."
                " PHP " -> totalMoney.text = "수취금액은 " + deciaml.format(moneyRate.text.toString().toInt() * countryRate[2]) + nowSel + "입니다."
            }
        }
    }
}