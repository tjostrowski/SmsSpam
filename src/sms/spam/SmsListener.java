/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.spam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.provider.Telephony;
//import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 *
 * @author tomek
 */
public class SmsListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
//            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
//                String messageBody = smsMessage.getMessageBody();
//                Toast.makeText(context, messageBody, Toast.LENGTH_LONG).show();
//            }
//        }
    }
}
