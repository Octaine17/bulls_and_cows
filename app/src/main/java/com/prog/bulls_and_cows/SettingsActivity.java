package com.prog.bulls_and_cows;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    CheckBox CheckBoxMusic, CheckBoxNull, CheckBoxRepeatSymbols;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        //убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        CheckBoxMusic=(CheckBox) findViewById(R.id.checkBoxMusic);
        CheckBoxNull=(CheckBox) findViewById(R.id.checkBoxNull);
        CheckBoxRepeatSymbols=(CheckBox) findViewById(R.id.checkBoxRepeatSymbols);
        CheckBoxMusic.setChecked(Environments.Sound);
        CheckBoxNull.setChecked(Environments.SettingsGame.NumberNullFirst);
        CheckBoxRepeatSymbols.setChecked(Environments.SettingsGame.RepeatSymbols);
        //при изменении настройки звуков
        CheckBoxMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked)SaveSetting("Sound", "yes");
                else SaveSetting("Sound", "no");
                Environments.Sound=checked;
            }
        });
        //при изменении настройки первого нуля
        CheckBoxNull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked)SaveSetting("NumberNullFirst", "yes");
                else SaveSetting("NumberNullFirst", "no");
                Environments.SettingsGame.NumberNullFirst=checked;
            }
        });
        //при изменении настройки блокировки кнопок на игровой клавиатуре
        CheckBoxRepeatSymbols.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked)SaveSetting("BlockButtons", "yes");
                else SaveSetting("BlockButtons", "no");
                Environments.SettingsGame.RepeatSymbols=checked;
            }
        });
    }
    //сохранение настройки в файл
    private void SaveSetting(String name, String value){
        SharedPreferences mSettings;
        mSettings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString( name, value );
        editor.commit();
    }
    //загрузка настройки из файла
    private String LoadSetting(String name){
        String result="";
        SharedPreferences mSettings;
        mSettings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        result=mSettings.getString(name, "");
        return result;
    }
}
