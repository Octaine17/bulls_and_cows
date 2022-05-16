package com.prog.bulls_and_cows;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    ProgressDialog pd;
    MediaPlayer mPlayer;
    String [] WordGame;//цифры или слово для отгадывания, задается программой
    String [] PlayerGame;//цифры или слово вводимым игроком
    int Points, Minus;
    int HeightKeyboard=0;//высота клавиатуры
    public static boolean WordDay;//true - значит слово дня
    String Hint;
    Button[] ButtonsSymbols;
    ConstraintLayout ConLayoutBottom;
    Button ButtonCancel, ButtonShowText, ButtonHint, ButtonDel, ButtonEnter;
    TableRow TableRow1, TableRow2, TableRow3, TableRow4;
    TextView TextViewGame, TextViewUserPoints, TextViewMinus, TextViewPoints;
    ListView ListViewGame;
    List<BC> ListGame;//список сколько быков и коров в одном ходе
    MyAdapter mAdapter;
    boolean FirstSign;
    class BC{
        String word;
        String result;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        //убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FirstSign=true;
        ConLayoutBottom=(ConstraintLayout) findViewById(R.id.conLayoutBottom);
        ButtonCancel=(Button) findViewById(R.id.buttonCancel);
        ButtonShowText=(Button) findViewById(R.id.buttonShowText);
        ButtonDel=(Button) findViewById(R.id.buttonDel);
        ButtonEnter=(Button) findViewById(R.id.buttonEnter);
        TableRow1=(TableRow) findViewById(R.id.tableRow1);
        TableRow2=(TableRow) findViewById(R.id.tableRow2);
        TableRow3=(TableRow) findViewById(R.id.tableRow3);
        TableRow4=(TableRow) findViewById(R.id.tableRow4);
        TextViewGame=(TextView) findViewById(R.id.textViewGame);
        TextViewUserPoints=(TextView) findViewById(R.id.textViewUserPoints);
        TextViewMinus=(TextView) findViewById(R.id.textViewMinus);
        TextViewPoints=(TextView) findViewById(R.id.textViewPoints);
        ListViewGame=(ListView) findViewById(R.id.listViewGame);
        PreparingGame();
    }
    //подготовка игры
    private void PreparingGame(){
        ListGame=new ArrayList<>();
        mAdapter = new MyAdapter(getApplicationContext());
        ListViewGame.setAdapter(mAdapter);
        WordGame=new String[Environments.SettingsGame.NumberSymbols];
        PlayerGame=new String[Environments.SettingsGame.NumberSymbols];
        for(int i=0;i<Environments.SettingsGame.NumberSymbols;i++)PlayerGame[i]=null;
        int k;
        if(Environments.SettingsGame.ViewGame==1) {//если игра цифры
            int[] mas_number = new int[Environments.SettingsGame.NumberSymbols];
            //генерация цифр
            if (Environments.SettingsGame.NumberNullFirst == false) {
                mas_number[0] = (int) (Math.random() * 8 + 1);
                k = 1;
            } else k = 0;
            if(Environments.SettingsGame.RepeatSymbols){//если с повторяющимися символами
                for (int i = k; i < Environments.SettingsGame.NumberSymbols; i++)mas_number[i] = (int) (Math.random() * 9);
            }else {//если повторяющиеся символы не разрешены
                for (int i = k; i < Environments.SettingsGame.NumberSymbols; i++) {
                    mas_number[i] = (int) (Math.random() * 9);
                    if (i < 9) {
                        for (int j = 0; j < i; j++)
                            if (mas_number[i] == mas_number[j]) {
                                i--;
                                break;
                            }
                    } else {
                        boolean check;
                        int l;
                        for (int j = 0; j < 10; j++) {
                            check = true;
                            for (l = 0; l < 9; l++)
                                if (j == mas_number[l]) {
                                    check = false;
                                    break;
                                }
                            if (check) {
                                mas_number[9] = l;
                            }
                        }
                    }
                }
            }
            TextViewGame.setText(Integer.toString(Environments.SettingsGame.NumberSymbols)+" цифр");
            for(int i=0;i<WordGame.length;i++){
                WordGame[i]=Integer.toString(mas_number[i]);
                System.out.println(WordGame[i]);
            }
        }else {
            TextViewGame.setText(Integer.toString(Environments.SettingsGame.NumberSymbols)+" букв");
        }
        if(!Environments.Login.equals(""))TextViewUserPoints.setText(Integer.toString(Environments.Points)+" очков");
        else TextViewUserPoints.setText("");
        RequestParams params = new RequestParams();
        params.put("oper", "1");
        params.put("view_game", Environments.SettingsGame.ViewGame);
        params.put("number_symbols", Environments.SettingsGame.NumberSymbols);
        params.put("word_day", Boolean.toString(WordDay));
        params.put("repeat", Boolean.toString(Environments.SettingsGame.RepeatSymbols));
        pd = new ProgressDialog(GameActivity.this);
        pd.setTitle("Сообщение");
        pd.setMessage("Подождите, идет обработка данных.");
        // включаем анимацию ожидания
        pd.setIndeterminate(true);
        //не даем исчезнуть диалогу с сообщением
        pd.setCancelable(false);
        pd.show();
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Environments.UrlServer+"/gameController", params, new AsyncHttpResponseHandler() {
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
                    try {
                        JSONObject json=new JSONObject(response);
                        Points=json.getInt("points");
                        Minus=json.getInt("minus");
                        if(Environments.SettingsGame.ViewGame==2) {
                            String word = json.getString("word");
                            Hint = json.getString("hint");
                            word=word.toUpperCase();
                            for(int i=0;i<word.length();i++)WordGame[i]=word.substring(i, i+1);
                        }
                        TextViewMinus.setText("Ход -"+Integer.toString(Minus)+" очков");
                        TextViewPoints.setText(Integer.toString(Points));
                        if(FirstSign)SetKeyboard();
                        else{
                            for(int i=0;i<ButtonsSymbols.length;i++)
                                ButtonsSymbols[i].setTextColor(Color.WHITE);
                        }
                        //устанавливаем высоту игрового списка
                        ConstraintLayout.LayoutParams params=null;
                        params=(ConstraintLayout.LayoutParams) ListViewGame.getLayoutParams();
                        params.height=Environments.ScreenHeight-HeightKeyboard-20;
                        ListViewGame.setLayoutParams(params);
                    } catch (JSONException e) {
                        toast = Toast.makeText(getApplicationContext(),
                                "Произошла ошибка.", Toast.LENGTH_SHORT);
                        toast.show();
                        finish();
                        e.printStackTrace();
                    }
                }else{
                    toast = Toast.makeText(getApplicationContext(),
                            "Произошла ошибка.", Toast.LENGTH_SHORT);
                    toast.show();
                    finish();
                }
                pd.dismiss();
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Произошла ошибка.", Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
        });
    }
    //установка клавиатуры
    private void SetKeyboard(){
        ButtonShowText.setText("");
        ConstraintLayout.LayoutParams paramsL=null;
        TableRow.LayoutParams params;
        int proc_number;
        //устанавливаем верхние кнопки
        proc_number=(int)(Environments.ScreenWidth/100.0*17.0);
        params=(TableRow.LayoutParams) ButtonCancel.getLayoutParams();
        params.width=proc_number;
        params.height=(int)(Environments.ScreenHeight/100.0*6.0);
        ButtonCancel.setLayoutParams(params);
        proc_number=(int)(Environments.ScreenWidth/100.0*60.0);
        params=(TableRow.LayoutParams) ButtonShowText.getLayoutParams();
        params.width=proc_number;
        params.height=(int)(Environments.ScreenHeight/100.0*6.0);;
        ButtonShowText.setLayoutParams(params);
        proc_number=(int)(Environments.ScreenWidth/100.0*9.0);
        params=(TableRow.LayoutParams) ButtonDel.getLayoutParams();
        params.width=proc_number;
        params.height=(int)(Environments.ScreenHeight/100.0*6.0);;
        ButtonDel.setLayoutParams(params);
        proc_number=(int)(Environments.ScreenWidth/100.0*9.0);
        params=(TableRow.LayoutParams) ButtonEnter.getLayoutParams();
        params.width=proc_number;
        params.height=(int)(Environments.ScreenHeight/100.0*6.0);
        ButtonEnter.setLayoutParams(params);
        Button button;
        switch (Environments.SettingsGame.ViewGame){
            case 1://игра только цифры
                ButtonsSymbols=new Button[10];
                paramsL=(ConstraintLayout.LayoutParams) ConLayoutBottom.getLayoutParams();
                paramsL.height=(int)(Environments.ScreenHeight/100.0*6.0+Environments.ScreenHeight/100.0*4.0+10);
                ConLayoutBottom.setLayoutParams(paramsL);
                TableRow1.setVisibility(View.VISIBLE);
                TableRow2.setVisibility(View.GONE);
                TableRow3.setVisibility(View.GONE);
                TableRow4.setVisibility(View.GONE);
                for(int i=0;i<ButtonsSymbols.length;i++){
                    button=new Button(this);
                    button.setText(Integer.toString(i));
                    button.setTextColor(Color.WHITE);
                    button.setTextSize(10);
                    button.setPadding(0, 0, 0, 0);
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setColor(Color.parseColor("#37474f"));//цвет фона кнопки
                    drawable.setCornerRadius(10);//закругляем кнопку
                    button.setBackground(drawable);
                    TableRow1.addView(button);
                    params=(TableRow.LayoutParams) button.getLayoutParams();
                    params.width=(int)(Environments.ScreenWidth/10.0-4);
                    params.height=(int)(Environments.ScreenHeight/100.0*4.0);
                    params.leftMargin=3;
                    button.setLayoutParams(params);
                    ButtonsSymbols[i]=button;
                }
                break;
            case 2://игра только буквы
                String Alf="АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
                TableRow2.setVisibility(View.VISIBLE);
                TableRow3.setVisibility(View.VISIBLE);
                TableRow4.setVisibility(View.VISIBLE);
                ButtonsSymbols=new Button[33];
                paramsL=(ConstraintLayout.LayoutParams) ConLayoutBottom.getLayoutParams();
                paramsL.height=(int)(Environments.ScreenHeight/100.0*6.0+Environments.ScreenHeight/100.0*4.0*4.0+20);
                ConLayoutBottom.setLayoutParams(paramsL);
                for(int i=0;i<ButtonsSymbols.length;i++){
                    button=new Button(this);
                    button.setText(Alf.substring(i, i+1));
                    button.setTextColor(Color.WHITE);
                    button.setTextSize(10);
                    button.setPadding(0, 0, 0, 0);
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setColor(Color.parseColor("#37474f"));//цвет фона кнопки
                    drawable.setCornerRadius(10);//закругляем кнопку
                    button.setBackground(drawable);
                    if(i>=0 && i<=9)TableRow1.addView(button);
                    if(i>=10 && i<=19)TableRow2.addView(button);
                    if(i>=20 && i<=29)TableRow3.addView(button);
                    if(i>=30 && i<=32)TableRow4.addView(button);
                    params=(TableRow.LayoutParams) button.getLayoutParams();
                    params.width=(int)(Environments.ScreenWidth/10.0-4);
                    params.height=(int)(Environments.ScreenHeight/100.0*4.0);
                    params.leftMargin=3;params.bottomMargin=3;
                    button.setLayoutParams(params);
                    ButtonsSymbols[i]=button;
                    //кнопка подсказки
                    if(i==32){
                        button=new Button(this);
                        button.setTextSize(10);
                        button.setText("ПОДСКАЗКА");
                        button.setTextColor(Color.WHITE);
                        button.setPadding(0, 0, 0, 0);
                        drawable = new GradientDrawable();
                        drawable.setColor(Color.parseColor("#1e88e5"));//цвет фона кнопки
                        drawable.setCornerRadius(10);//закругляем кнопку
                        button.setBackground(drawable);
                        TableRow4.addView(button);
                        params=(TableRow.LayoutParams) button.getLayoutParams();
                        params.height=(int)(Environments.ScreenHeight/100.0*4.0);
                        params.leftMargin=3;params.bottomMargin=3;
                        params.span=3;
                        button.setLayoutParams(params);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        Hint, Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }
                }
                break;
        }
        HeightKeyboard=paramsL.height;
        //нажата кнопка 'Сдаться'
        ButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirstSign=false;
                ButtonShowText.setText("");
                String pr_word="";
                for(int i=0;i<WordGame.length;i++)pr_word+=WordGame[i];
                EndGame(2, pr_word);
            }
        });
        //нажата кнопка 'D' удаление правого символы
        ButtonDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=PlayerGame.length-1;i>=0;i--)
                    if(PlayerGame[i]!=null){
                        PlayerGame[i]=null;
                        ShowText();
                        break;
                    }
            }
        });
        //нажата кнопка 'E' ввод слова
        ButtonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BC res=new BC();
                res.word="";
                res.result="";
                String pr_word="";
                //проверка что все символы введены
                for(int i=0;i<PlayerGame.length;i++)
                    if(PlayerGame[i]==null)return;
                int counter_bulls=0, counter_cows=0;
                //формирование результата от ввода символов
                for(int i=0;i<PlayerGame.length;i++){
                    res.word+=PlayerGame[i];
                    if(PlayerGame[i].equals(WordGame[i]))counter_bulls++;
                    else{
                        for(int j=0;j<PlayerGame.length;j++)
                            if(PlayerGame[i].equals(WordGame[j]))counter_cows++;
                    }
                }
                //определяем сколько быков и коров
                for(int i=0;i<counter_bulls;i++)res.result+="Б";
                for(int i=0;i<counter_cows;i++)res.result+="К";
                ListGame.add(res);
                mAdapter.notifyDataSetChanged();
                ListViewGame.setSelection(ListViewGame.getAdapter().getCount()-1);//прокрутка списка вниз
                boolean check_victory=true;
                //если в настройках установлена подсказка символов
                /*if(Environments.SettingsGame.BlockButtons){
                    for(int i=0;i<PlayerGame.length;i++){
                        for(int j=0;j<PlayerGame.length;j++)
                            if(PlayerGame[i].equals(WordGame[j]))
                                for(int k=0;k<ButtonsSymbols.length;k++)
                                    if(PlayerGame[i].equals(ButtonsSymbols[k].getText().toString())){
                                        ButtonsSymbols[k].setTextColor(Color.RED);
                                        break;
                                    }
                    }
                }*/
                //если отгадали
                if(counter_bulls==WordGame.length && Points>0){
                    for(int i=0;i<WordGame.length;i++)pr_word+=WordGame[i];
                    EndGame(1, pr_word);
                }else{//если не отгадали
                    Points-=Minus;
                    if(Points<0)Points=0;
                    TextViewPoints.setText(Integer.toString(Points));
                    if(Points==0){//полный проигрыш
                        for(int i=0;i<WordGame.length;i++)pr_word+=WordGame[i];
                        EndGame(2, pr_word);
                    }else {
                        if (Environments.Sound) ButtonEnter.setEnabled(false);
                        Music(1);
                    }
                }
                ButtonShowText.setText("");
                for(int i=0;i<PlayerGame.length;i++)PlayerGame[i]=null;
            }
        });
        //обработчики события кнопок с цифрами или буквами
        for(int i=0;i<ButtonsSymbols.length;i++){
            ButtonsSymbols[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String s=((Button) view).getText().toString();
                    //проверка что такой символ не введен уже
                    if(WordDay==false)
                        if(Environments.SettingsGame.RepeatSymbols==false)
                            for(int i=0;i<PlayerGame.length;i++){
                                if(PlayerGame[i]==null)break;
                                if(PlayerGame[i].equals(s)){
                                    Toast toast = Toast.makeText(getApplicationContext(), "Символ "+s+" уже был введен.", Toast.LENGTH_SHORT);
                                    toast.show();
                                    return;
                                }
                            }
                    //ввод нового символа
                    for(int i=0;i<PlayerGame.length;i++)
                        if(PlayerGame[i]==null){
                            PlayerGame[i]=((Button) view).getText().toString();
                            ShowText();
                            break;
                        }
                }
            });
        }
    }
    //конец игры
    private void EndGame(int view, String pr_word){//1- выигрыш, 2 проигрыш
        AlertDialog modal_end=null;
        //Получаем вид с файла
        LayoutInflater li = LayoutInflater.from(GameActivity.this);
        View modalView = li.inflate(R.layout.modal_end, null);
        TextView TextEnd1, TextEnd2, TextEnd3;
        ImageView ImageEnd;
        TextEnd1=modalView.findViewById(R.id.textViewEndUp1);
        TextEnd2=modalView.findViewById(R.id.textViewEndUp2);
        TextEnd3=modalView.findViewById(R.id.textViewEndBottom1);
        ImageEnd=modalView.findViewById(R.id.imageViewEnd);
        switch(view){
            case 1:
                FirstSign=false;
                if(WordDay==false){
                    TextEnd1.setText("ПОБЕДА!!!");
                    if(!Environments.Login.equals("")){
                        Environments.Points+=Points;
                        TextViewUserPoints.setText(Integer.toString(Environments.Points)+" очков");
                    }
                }
                else {
                    if(!Environments.Login.equals("")) {
                        TextEnd1.setText("ПОБЕДА!!! СЛОВО ДНЯ ОЧКИ УДВАИВАЮТСЯ.");
                        Points=2*Points;
                        Environments.Points += Points;
                        TextViewUserPoints.setText(Integer.toString(Environments.Points)+" очков");
                    }else TextEnd1.setText("ПОБЕДА!!! СЛОВО ДНЯ.");
                }
                TextEnd2.setText(pr_word+" - правильно");
                ImageEnd.setImageResource(R.drawable.smile_victory);
                TextEnd3.setText("Вы заработали "+Integer.toString(Points)+" очков");
                Music(2);
                break;
            case 2:
                TextEnd1.setText("ПОРАЖЕНИЕ");
                TextEnd2.setText(pr_word+" - правильно");
                ImageEnd.setImageResource(R.drawable.smile_defeat);
                TextEnd3.setVisibility(View.GONE);
                Music(3);
                break;
        }
        //записываем данные, что игра дня сегодня была
        if(WordDay){
            Date dateNow = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy");
            String dateToday=formatForDateNow.format(dateNow);
            SaveSetting("LastWordDayDate", dateToday);
        }
        //Создаем AlertDialog
        AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(GameActivity.this);
        //Настраиваем modal_password.xml для нашего AlertDialog:
        aDialogBuilder.setView(modalView);
        aDialogBuilder.setCancelable(false);
        if(WordDay==false) {
            aDialogBuilder.setPositiveButton("Выход из игры",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
            aDialogBuilder.setNegativeButton("Начать заново",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PreparingGame();
                        }
                    });
        }else{
            aDialogBuilder.setPositiveButton("Выход из игры",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
        }
        //создаем AlertDialog
        modal_end = aDialogBuilder.create();
        //и отображаем его
        modal_end.show();
        modal_end.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));//убираем черный фон
        if(!Environments.Login.equals("") && view==1){
            RequestParams params = new RequestParams();
            params.put("oper", "6");
            params.put("login", Environments.Login);
            params.put("points", Integer.toString(Points));
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
                        TextViewUserPoints.setText(Integer.toString(Environments.Points));
                    }else{
                        toast = Toast.makeText(getApplicationContext(),
                                "Произошла ошибка обновления очков пользователя.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Произошла ошибка обновления очков пользователя.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }
    }
    private void ShowText(){
        ButtonShowText.setText("");
        String text="";
        for(int j=0;j<PlayerGame.length;j++)
            if(PlayerGame[j]!=null)text+=PlayerGame[j];
        ButtonShowText.setText(text);
    }
    //сохранение настройки в файл
    private void SaveSetting(String name, String value){
        SharedPreferences mSettings;
        mSettings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString( name, value );
        editor.commit();
    }
    //Адаптер вывода игры
    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        public MyAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return ListGame.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public void getString(int position) {

        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)convertView = mLayoutInflater.inflate(R.layout.list_game, null);
            BC res=ListGame.get(position);
            ConstraintLayout ConL=(ConstraintLayout) convertView.findViewById(R.id.conL);
            ConL.removeAllViews();//удаление всего с слоя
            TableLayout TableLayoutBK=new TableLayout(GameActivity.this);
            TableRow TableRowBK=new TableRow(GameActivity.this);
            ImageView Images[]=new ImageView[res.result.length()];
            TextView TextKol=new TextView(GameActivity.this);
            //меняем размеры image
            int width, height;
            width=Environments.ScreenWidth/100*7;
            height=Environments.ScreenHeight/100*7;
            TableRowBK.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            int counter_bulls=0, counter_cows=0;
            for(int i=0;i<res.result.length();i++){
                Images[i]=new ImageView(GameActivity.this);
                String s=res.result.substring(i, i+1);
                switch(s){
                    case "Б":
                        Images[i].setImageResource(R.drawable.bull);
                        counter_bulls++;
                        break;
                    case "К":
                        Images[i].setImageResource(R.drawable.cow);
                        counter_cows++;
                        break;
                }
                TableRowBK.addView(Images[i]);
                TableRow.LayoutParams params = (TableRow.LayoutParams) Images[i].getLayoutParams();
                params.width=width;
                params.height = height;
                Images[i].setLayoutParams(params);
                Images[i].setPadding(0, 0, 3, 0);
            }
            String result=res.word+" Быков-"+Integer.toString(counter_bulls)+" Коров-"+Integer.toString(counter_cows);
            TextKol.setTextSize(16);
            TextKol.setTextColor(Color.BLACK);
            TextKol.setPadding(5, 0, 0, 0);
            TextKol.setText(result);
            ConL.addView(TextKol);
            TableLayoutBK.setPadding(5, 20, 0, 0);
            TableLayoutBK.addView(TableRowBK);
            ConL.addView(TableLayoutBK);
            return convertView;
        }
    }
    //звук
    private void Music(int num){
        if(Environments.Sound) {
            final Handler h = new Handler() {//слушатель ответа
                public void handleMessage(android.os.Message msg) {
                    ButtonEnter.setEnabled(true);
                };
            };
            Thread t = new Thread(new Runnable() {
                public void run() {
                    mPlayer = null;
                    switch (num) {
                        case 1://звук если не правильный ответ
                            mPlayer = MediaPlayer.create(GameActivity.this, R.raw.next);
                            break;
                        case 2://звук если победа в игре
                            mPlayer = MediaPlayer.create(GameActivity.this, R.raw.victory);
                            break;
                        case 3://звук если проигрыш в игре
                            mPlayer = MediaPlayer.create(GameActivity.this, R.raw.defeat);
                            break;
                    }
                    mPlayer.start();
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            //переключение или конец трека
                            mPlayer.release();//что бы звук не выбивало
                            Message msg=h.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", "");
                            msg.setData(bundle);
                            h.sendMessage(msg);
                        }
                    });
                }
            });
            t.start();
        }
    }
}
