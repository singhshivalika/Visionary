package com.singh_shivalika.try_try;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.ArraySet;
import android.util.Log;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import static android.provider.ContactsContract.CommonDataKinds.Relation.NAME;

public class SOS {

    private static final int THRESHOLD = 8;
    private static final int TIMEOUT = 2000;

    private Context context;
    private Set<String> starredContacts = new TreeSet<String>();

    @SuppressLint("NewApi")
    SOS(Context context){
        this.context = context;

        ContentResolver cr = context.getContentResolver();
        String[] proj = new String[]{ ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Data.STARRED};
        Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, proj, null, null, null);

        while(cursor.moveToNext()){
            try {
                if(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.STARRED)).equals("1"))
                    addToStarred(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            }catch (Exception e){}
        }
        Log.e("LOL",starredContacts.toString());
    }

    private void addToStarred(String number){
        String temp = "";
        for(char c : number.toCharArray()){
            if((c>='0' && c<='9') || c=='+')
                temp+=String.valueOf(c);
            else if(c==' '||c=='-')
                continue;
            else return;
        }
        temp = temp.substring(temp.length()-10);
        starredContacts.add(temp);
    }

    int SOS_count = 0;
    Timer tt;

    public void activateSOS(){
        SOS_count++;

        int remains = THRESHOLD-SOS_count;
        if(remains!=0)
            ((ThisApplication)((MainActivity) context).getApplication()).voiceClass.speak(String.valueOf(remains + " to go."));

        if(tt!=null)
            tt.cancel();

        tt = new Timer();
        tt.schedule(new ClearSOS(this),TIMEOUT);
        Log.e("SOS",String.valueOf(SOS_count));

        if(SOS_count==THRESHOLD) {
            tt.cancel();
            SOS_count = 0;
            startSOS();
        }
    }

    private void startSOS(){
        ((ThisApplication)((MainActivity) context).getApplication()).voiceClass.speak("SOS activated.");
        cry_for_help();
        SmsManager smsManager = SmsManager.getDefault();
        for(String i : starredContacts) {
            smsManager.sendTextMessage(i, null, "I am in distress right now. Need help !!!", null, null);
        }
    }


    public void resetSOSCounter(){
        this.SOS_count = 0;
        ((ThisApplication)((MainActivity) context).getApplication()).voiceClass.speak("SOS cancelled.");
    }


    private void cry_for_help() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0);

        MediaPlayer player = MediaPlayer.create(context, R.raw.alarm);
        player.setLooping(true);
        player.start();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.e("COMPLETED","LOL");
            }
        });
    }
}

//Message bhi jayega... so be careful....
//result ???
class ClearSOS extends TimerTask{
    SOS sos;
    ClearSOS(SOS sos){
        this.sos = sos;
    }

    @Override
    public void run() {
        sos.resetSOSCounter();
        Log.e("RESET",String.valueOf(sos.SOS_count));
    }
}
