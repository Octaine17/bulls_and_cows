package com.prog.bulls_and_cows;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class RegEnterActivity extends AppCompatActivity {
    ImageView ImageUp;
    EditText EditTextLogin, EditTextPas1, EditTextPas2;
    Button ButtonSelEnt, ButtonSelReg, ButtonRegEnter;
    TableRow TableRowRegText, TableRowRegEdit;
    ProgressDialog pd;
    int Oper;//1- вход, 2- регистрация
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_enter);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        //убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ImageUp=(ImageView) findViewById(R.id.imageUp2);
        EditTextLogin=(EditText) findViewById(R.id.editTextLogin);
        EditTextPas1=(EditText) findViewById(R.id.editTextPas1);
        EditTextPas2=(EditText) findViewById(R.id.editTextPas2);
        ButtonSelEnt=(Button) findViewById(R.id.buttonSelEnt);
        ButtonSelReg=(Button) findViewById(R.id.buttonSelReg);
        ButtonRegEnter=(Button) findViewById(R.id.buttonRegEnter);
        TableRowRegText=(TableRow) findViewById(R.id.tableRowRegText);
        TableRowRegEdit=(TableRow) findViewById(R.id.tableRowRegEdit);
        Oper=1;
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
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) ImageUp.getLayoutParams();//получаем параметры
        params.width=(int)new_width;
        params.height = (int)new_height;
        ImageUp.setLayoutParams(params);
        ImageUp.setImageBitmap(bm);
        ButtonSelEnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Oper=1;
                SetButtonSelect();
            }
        });
        ButtonSelReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Oper=2;
                SetButtonSelect();
            }
        });
        //нажата кнопка входа или регистрации пользователя
        ButtonRegEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast;
                //проверка на коректность ввода данных
                if(EditTextLogin.getText().length()<5){
                    toast = Toast.makeText(getApplicationContext(),
                            "Логин должен быть от 5 и более символов.", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if(EditTextPas1.getText().length()<5){
                    toast = Toast.makeText(getApplicationContext(),
                            "Пароль должен быть от 5 и более символов.", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if(Oper==2){
                    if(!EditTextPas1.getText().toString().equals(EditTextPas2.getText().toString())){
                        toast = Toast.makeText(getApplicationContext(),
                                "Пароли не совпадают.", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }
                }
                if(Oper==1)SignIn();
                if(Oper==2)RegNewUser();
            }
        });
        SetButtonSelect();
    }
    //вход пользователя
    private void SignIn(){
        String login, password;
        login=EditTextLogin.getText().toString();
        password=EditTextPas1.getText().toString();
        RequestParams params = new RequestParams();
        params.put("oper", "2");
        params.put("login", login);
        params.put("password", password);
        pd = new ProgressDialog(this);
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
                switch(response){
                    case "Error":
                        toast = Toast.makeText(getApplicationContext(),
                                "Произошла ошибка.", Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    case "NotOk":
                        toast = Toast.makeText(getApplicationContext(),
                                "Неправильный логин или пароль.", Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    default:
                        toast = Toast.makeText(getApplicationContext(),
                                "Вход прошел успешно.", Toast.LENGTH_SHORT);
                        toast.show();
                        Environments.Login=login;
                        Environments.Points=Integer.parseInt(response);
                        SaveSetting("Login", login);
                        finish();
                        break;
                }
                pd.dismiss();
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Произошла ошибка.", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
    //регистрация нового пользователя
    private void RegNewUser() {
        String login, password;
        login=EditTextLogin.getText().toString();
        password=EditTextPas1.getText().toString();
        RequestParams params = new RequestParams();
        params.put("oper", "1");
        params.put("login", login);
        params.put("password", password);
        pd = new ProgressDialog(this);
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
                switch(response){
                    case "Error":
                        toast = Toast.makeText(getApplicationContext(),
                                "Произошла ошибка.", Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    case "UserHave":
                        toast = Toast.makeText(getApplicationContext(),
                                "Пользователь с таким логином уже существует.", Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    case "Ok":
                        toast = Toast.makeText(getApplicationContext(),
                                "Регистрация прошла успешно.", Toast.LENGTH_SHORT);
                        toast.show();
                        Environments.Login=login;
                        SaveSetting("Login", login);
                        finish();
                        break;
                }
                pd.dismiss();
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                System.out.println("ERROR="+statusCode+" "+error.toString());
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Произошла ошибка.", Toast.LENGTH_SHORT);
                toast.show();
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
    //установка кнопок вход или регистрация
    private void SetButtonSelect(){
        switch (Oper){
            case 1://вход
                ButtonSelEnt.setTextColor(Color.WHITE);
                ButtonSelReg.setTextColor(Color.parseColor("#a3a39d"));
                TableRowRegText.setVisibility(View.INVISIBLE);
                TableRowRegEdit.setVisibility(View.INVISIBLE);
                ButtonRegEnter.setText("Вход");
                break;
            case 2://регистрация
                ButtonSelReg.setTextColor(Color.WHITE);
                ButtonSelEnt.setTextColor(Color.parseColor("#a3a39d"));
                TableRowRegText.setVisibility(View.VISIBLE);
                TableRowRegEdit.setVisibility(View.VISIBLE);
                ButtonRegEnter.setText("Регистрация");
                break;
        }
        EditTextLogin.setText("");
        EditTextPas1.setText("");
        EditTextPas2.setText("");
    }
}
