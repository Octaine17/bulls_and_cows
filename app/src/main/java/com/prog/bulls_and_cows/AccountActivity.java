package com.prog.bulls_and_cows;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class AccountActivity extends AppCompatActivity {
    ImageView ImageUp;
    TextView TextViewALogin;
    Button ButtonDelAccount, ButtonExitAccount, ButtonNewPassword;
    EditText EditTextNewPassword;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        //убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ImageUp=(ImageView) findViewById(R.id.imageUp3);
        TextViewALogin=(TextView) findViewById(R.id.textViewALogin);
        ButtonDelAccount=(Button) findViewById(R.id.buttonDelAccount);
        ButtonExitAccount=(Button) findViewById(R.id.buttonExitAccount);
        ButtonNewPassword=(Button) findViewById(R.id.buttonNewPassword);
        EditTextNewPassword=(EditText) findViewById(R.id.editTextNewPassword);
        //масштабируем верхнее изображение в виде быка и коровы, что бы вписалось в экран
        Bitmap bm = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.image_main);
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
        TextViewALogin.setText("Логин: "+Environments.Login);
        //нажата кнопка "Удалить аккаунт"
        ButtonDelAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog modal=null;
                //Создаем AlertDialog
                AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(AccountActivity.this);
                //Настраиваем modal
                aDialogBuilder.setCancelable(false);
                aDialogBuilder.setTitle("Сообщение");
                aDialogBuilder.setMessage("Удалить аккаунт?");
                aDialogBuilder.setPositiveButton("Да",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                RequestParams params = new RequestParams();
                                params.put("oper", "3");
                                params.put("login", Environments.Login);
                                pd = new ProgressDialog(AccountActivity.this);
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
                                            case "Ok":
                                                toast = Toast.makeText(getApplicationContext(),
                                                        "Удаление аккаунта прошло успешно.", Toast.LENGTH_SHORT);
                                                toast.show();
                                                Environments.Login="";
                                                SaveSetting("Login", "");
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
                        });
                aDialogBuilder.setNegativeButton("Нет",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                //Создаем AlertDialog:
                modal = aDialogBuilder.create();
                //и отображаем его:
                modal.show();
                modal.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));//убираем черный фон
            }
        });
        //нажата кнопка "Выйти из аккаунта"
        ButtonExitAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Environments.Login="";
                SaveSetting("Login", "");
                finish();
            }
        });
        //нажата кнопка "Изменить пароль"
        ButtonNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast;
                if(EditTextNewPassword.getText().length()>=5){
                    RequestParams params = new RequestParams();
                    params.put("oper", "4");
                    params.put("login", Environments.Login);
                    params.put("password", EditTextNewPassword.getText().toString());
                    pd = new ProgressDialog(AccountActivity.this);
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
                                case "Ok":
                                    toast = Toast.makeText(getApplicationContext(),
                                            "Изменение пароля прошло успешно.", Toast.LENGTH_SHORT);
                                    toast.show();
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
                }else{
                    toast = Toast.makeText(getApplicationContext(),
                            "Новый пароль должен быть юольше или равен 5 символам.", Toast.LENGTH_SHORT);
                    toast.show();
                }
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
