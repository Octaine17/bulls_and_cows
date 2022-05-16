package com.prog.bulls_and_cows;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button ButtonMode1, ButtonMode2, ButtonMode3, ButtonSettings, ButtonRulesGame, ButtonAccount, ButtonRatingGame, ButtonExit;
    TextView TextViewLogin, TextViewRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Environments.SettingsGame=new Environments.SettingsGameCl();
        ImageView ImageMain=(ImageView) findViewById(R.id.imageMain);
        TextViewLogin=(TextView) findViewById(R.id.textViewLogin);
        TextViewRating=(TextView) findViewById(R.id.textViewRating);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        //убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButtonMode1=(Button) findViewById(R.id.buttonMode1);
        ButtonMode2=(Button) findViewById(R.id.buttonMode2);
        ButtonMode3=(Button) findViewById(R.id.buttonMode3);
        ButtonSettings=(Button) findViewById(R.id.buttonSettings);
        ButtonRulesGame=(Button) findViewById(R.id.buttonRulesGame);
        ButtonAccount=(Button) findViewById(R.id.buttonAccount);
        ButtonRatingGame=(Button) findViewById(R.id.buttonRatingGame);
        ButtonExit=(Button) findViewById(R.id.buttonExit);
        ButtonRulesGame=(Button) findViewById(R.id.buttonRulesGame);
        //узнаем размер экрана
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);
        Environments.ScreenWidth=metricsB.widthPixels;
        Environments.ScreenHeight=metricsB.heightPixels;
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
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) ImageMain.getLayoutParams();//получаем параметры
        params.width=(int)new_width;
        params.height = (int)new_height;
        ImageMain.setLayoutParams(params);
        ImageMain.setImageBitmap(bm);
        //Получаем настройки
        //текущий логин пользователя
        Environments.Login=LoadSetting("Login");
        //проверка включен или выключен звук
        String temp=LoadSetting("Sound");
        if(temp.length()==0){
            temp="yes";
            SaveSetting("Sound", "yes");
        }
        if(temp.equals("yes")) Environments.Sound=true;
        else Environments.Sound=false;
        //проверяем по умолчанию кол-во символов в игре
        temp=LoadSetting("NumberSymbols");
        if(temp.length()==0){
            temp="4";
            SaveSetting("NumberSymbols", "4");
        }
        Environments.SettingsGame.NumberSymbols=Integer.parseInt(temp);
        temp=LoadSetting("NumberNullFirst");
        if(temp.length()==0)temp="yes";
        if(temp.equals("yes"))Environments.SettingsGame.NumberNullFirst=true;
        else Environments.SettingsGame.NumberNullFirst=false;
        //повторяющиеся символы
        temp=LoadSetting("RepeatSymbols");
        if(temp.length()==0){
            temp="yes";
            SaveSetting("RepeatSymbols", "yes");
        }
        if(temp.equals("yes")) Environments.SettingsGame.RepeatSymbols=true;
        else Environments.SettingsGame.RepeatSymbols=false;
        //нажата кнопка 'игра только числа'
        ButtonMode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Environments.SettingsGame.ViewGame=1;
                GameActivity.WordDay=false;
                Intent intent = new Intent(MainActivity.this, NumberSymbolsActivity.class);
                startActivity(intent);
            }
        });
        //нажата кнопка 'игра только слова'
        ButtonMode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Environments.SettingsGame.ViewGame=2;
                GameActivity.WordDay=false;
                Intent intent = new Intent(MainActivity.this, NumberSymbolsActivity.class);
                startActivity(intent);
            }
        });
        //нажата кнопка 'Слово дня'
        ButtonMode3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast;
                //проверка может игрок играть в слово дня или нет
                if(Environments.Login.equals("")){
                    toast = Toast.makeText(getApplicationContext(),
                            "Слово дня возможна только для зарегестрированного пользователя.", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                boolean check=false;
                String date=LoadSetting("LastWordDayDate");
                if(date.length()>0){
                    Date dateNow = new Date();
                    SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy");
                    String dateToday=formatForDateNow.format(dateNow);
                    if(!date.equals(dateToday))check=true;
                }else check=true;
                if(check) {
                    Environments.SettingsGame.ViewGame = 2;
                    GameActivity.WordDay = true;
                    Environments.SettingsGame.NumberSymbols = 4;
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    startActivity(intent);
                }else{
                    toast = Toast.makeText(getApplicationContext(),
                            "Новая игра дня будет возможна завтра с 00:00.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        //нажата кнопка 'Настройки'
        ButtonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        //нажата кнопка по аккаунту пользователя
        ButtonAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=null;
                if(Environments.Login.length()==0)intent = new Intent(MainActivity.this, RegEnterActivity.class);
                else intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });
        //нажата кнопка 'Рейтинг'
        ButtonRatingGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RatingActivity.class);
                startActivity(intent);
            }
        });
        //нажата кнопка 'Руководство'
        ButtonRulesGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Environments.UrlServer+"/UserGuide.html"));
                startActivity(intent);
            }
        });
        //нажата кнопка 'Выход'
        ButtonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if(!Environments.Login.equals(""))GetPointsPlayer();
    }
    //событие при активизации активности
    @Override
    protected void onStart(){
        super.onStart();
        if(Environments.Login.length()>0){
            ButtonAccount.setText("Аккаунт");
            TextViewLogin.setText("Вы вошли как "+Environments.Login);
            TextViewRating.setText("всего "+Integer.toString(Environments.Points)+" очков");
        }
        else{
            ButtonAccount.setText("Вход/Регистрация");
            TextViewLogin.setText("Вы вошли как аноним");
            TextViewRating.setText("");
        }
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
    //получение общего числа очков игрока
    private void GetPointsPlayer(){
        ProgressDialog pd;
        RequestParams params = new RequestParams();
        params.put("oper", "5");
        params.put("login", Environments.Login);
        pd = new ProgressDialog(MainActivity.this);
        pd.setTitle("Сообщение");
        pd.setMessage("Подождите, идет обработка данных.");
        // включаем анимацию ожидания
        pd.setIndeterminate(true);
        //не даем исчезнуть диалогу с сообщением
        pd.setCancelable(false);
        pd.show();
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Environments.UrlServer+"/accountController", params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                Toast toast;
                //получение ответа с сервера
                String response=new String(responseBody);
                response=response.trim();
                if(!response.equals("Error")){
                    Environments.Points=Integer.parseInt(response);
                    TextViewRating.setText("всего "+Integer.toString(Environments.Points)+" очков");
                }else{
                    toast = Toast.makeText(getApplicationContext(),
                            "Произошла ошибка.", Toast.LENGTH_SHORT);
                    toast.show();
                }
                pd.dismiss();
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                System.out.println("Error="+statusCode+" "+responseBody.toString());
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Произошла ошибка.", Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
        });
    }
}