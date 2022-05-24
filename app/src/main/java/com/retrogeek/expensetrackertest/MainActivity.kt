package com.retrogeek.expensetrackertest

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
//    lateinit var smsTextView: TextView
    lateinit var smsListView: ListView
    lateinit var debitTextView: TextView
    lateinit var creditTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        smsTextView = findViewById(R.id.textView)
        smsListView = findViewById(R.id.listView)
        debitTextView = findViewById(R.id.debitTextView)
        creditTextView = findViewById(R.id.creditTextView)

        ActivityCompat.requestPermissions(this, Array<String>(1) {Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED)
    }

    fun readSMS (view: View) {
        Log.i("readSms", "read sms called");
        val smsList: MutableList<String> = mutableListOf()
        val validSendersList: MutableList<String> = mutableListOf("AX-HDFCBK", "VK-HDFCBK", "VM-HDFCBK", "VD-HDFCBK")

        var debitAmount = 0f
        var creditAmount = 0f

        val cursor: Cursor? = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        cursor?.moveToFirst();
        cursor.use { cursor ->
            while (cursor?.moveToNext() == true) {
                var sender = cursor.getString(2)
                var body = cursor.getString(12)

//                Log.i("readSms", sender)

                //if (sender.contains("HDFCBK", ignoreCase = true)) {
                if (
                    validSendersList.contains(sender) && (
                        body.contains("debit", ignoreCase = true) ||
                        body.contains("credit", ignoreCase = true) ||
                        body.contains("spent", ignoreCase = true)
                    )
                ) {
                    smsList.add("($sender) $body")
                    var smsTransactionAmount = getAmountFromSmsBody(body)
                    if (body.contains("debit", ignoreCase = true) || body.contains("spent", ignoreCase = true)) {
                        debitAmount += smsTransactionAmount
                    } else {
                        creditAmount += smsTransactionAmount
                    }
                }
            }
        }
        Log.i("readSms", "$debitAmount -- $creditAmount")
        //debitCreditTextView.text = "Debit Amount: " + debitAmount + " and Credit Amount: " + creditAmount
        debitTextView.text = getString(R.string.debitMessage, creditAmount.toString())
        creditTextView.text = getString(R.string.creditMessage, debitAmount.toString())
        var smsAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsList)
        smsListView.adapter = smsAdapter
    }

    fun getAmountFromSmsBody(body:String): Float {
        val regex = """(?i)(?:(?:RS|INR|MRP)\.?\s?)(\d+(:?\,\d+)?(\,\d+)?(\.\d{1,2})?)""".toRegex()
        var transactionAmount = regex.findAll(body)
        Log.i("readSms", body)
        if (transactionAmount.count() > 0) {
            //for (amount in transactionAmount) {
            //    Log.i("readSms", amount.value)
            //}
            return formatAmountStringToFloat(transactionAmount.elementAt(0).value)
        }
        return 0f
    }

    fun formatAmountStringToFloat(amountString:String): Float {
        var formattedAmount = amountString
            .replace("Rs.","")
            .replace("Rs ","")
            .replace("Rs","")
            .replace("INR ", "")
            .replace("INR", "")
            .replace(",","")
        Log.i("readSms", formattedAmount)
        return formattedAmount.toFloat()
    }
}