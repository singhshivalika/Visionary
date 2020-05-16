package com.singh_shivalika.try_try;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import static android.provider.ContactsContract.CommonDataKinds.Relation.NAME;

public class SOS {

    Context context;
    List<String> contacts;

    @SuppressLint("NewApi")
    SOS(Context context){
        this.context = context;

        ContentResolver cr = context.getContentResolver();
        String[] proj = new String[]{ ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, proj, null, null, null);

        while(cursor.moveToNext()){
            try {
                String s = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if(PhoneNumberUtils.isEmergencyNumber(s))
                    Log.e("EMERGENCY",s);

            }catch (Exception e){}

        }




        /*Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);

        phones.moveToFirst();

        while (phones.moveToNext()) {
            String number = phones.getString(0);
            Log.e("LOL",number);
        }
        phones.close();*/


        cursor.close();
    }
}
