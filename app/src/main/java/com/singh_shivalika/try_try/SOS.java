package com.singh_shivalika.try_try;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
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

        TelephonyManager mngr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        ContentResolver cr = context.getContentResolver();
        String[] proj = new String[]{ ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, proj, null, null, null);

        while(cursor.moveToNext()){
            try {
                String s = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if(s==null)continue;

                if(isValidPhone(s))
                    Log.e("NUMBER",s);


            }catch (Exception e){ Log.e("EXCEPT",e.toString()); }

        }

        if(PhoneNumberUtils.isLocalEmergencyNumber(context,"989-128-1703"))
            Log.e("EMERGENCY","LOL");





        /*Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);

        phones.moveToFirst();

        while (phones.moveToNext()) {
            String number = phones.getString(0);
            Log.e("LOL",number);
        }
        phones.close();*/


        cursor.close();
    }

    private boolean isValidPhone(String str){
        for(char s : str.toCharArray()){
            if((s>='0' && s<='9') || s == '-' || s=='+' || s==' ');
            else
                return false;
        }
        return true;
    }
}
