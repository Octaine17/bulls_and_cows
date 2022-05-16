package com.prog.bulls_and_cows;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class NumberSymbolsActivity extends AppCompatActivity {
    ImageView ImageNumber;
    Spinner SpinnerNumberSymbols;
    Button ButtonStartGame;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_symbols);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        //убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ImageNumber=(ImageView) findViewById(R.id.imageUp1);
        ButtonStartGame=(Button) findViewById(R.id.buttonStartGame);
        //масштабируем верхнее изображение в виде быка и коровы, что бы вписалось в экран
        Bitmap bm= BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.image_main);
        double proc, new_width=0, new_height=0;
        double raz;
        if(bm.getWidth()>Environments.ScreenWidth){
            raz=bm.getWidth()-Environments.ScreenWidth;
            proc=(int)(raz/bm.getWidth()*100.0);
            new_width=bm.getWidth()-(bm.getWidth()/100.0*proc);
            new_height=bm.getHeight()-(bm.getHeight()/100.0*proc);
        }else{
            raz=Environments.ScreenWidth-bm.getWidth();
            proc=(int)(raz/Environments.ScreenWidth*100.0);
            new_width=bm.getWidth()+(Environments.ScreenWidth/100.0*proc);
            new_height=bm.getHeight()+(Environments.ScreenWidth/100.0*proc);
        }
        Environments cl=new Environments();
        bm=cl.GetResizedBitmap(bm, (int)new_width, (int)new_height);
        //изменяем размеры ImageView под новый размер картинки в виде быка и коровы
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) ImageNumber.getLayoutParams();//получаем параметры
        params.width=(int)new_width;
        params.height = (int)new_height;
        ImageNumber.setLayoutParams(params);
        ImageNumber.setImageBitmap(bm);
        SpinnerNumberSymbols=(Spinner) findViewById(R.id.spinnerNumberSymbols);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.list_symbols));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerNumberSymbols.setAdapter(adapter);
        SpinnerNumberSymbols.setSelection(Environments.SettingsGame.NumberSymbols-2);
        //нажата кнопка 'старт игры'
        ButtonStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Environments.SettingsGame.NumberSymbols=SpinnerNumberSymbols.getSelectedItemPosition()+2;
                SaveSetting("NumberSymbols", Integer.toString(Environments.SettingsGame.NumberSymbols));
                Intent intent = new Intent(NumberSymbolsActivity.this, GameActivity.class);
                startActivity(intent);
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
}
