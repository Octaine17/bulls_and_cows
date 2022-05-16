package com.prog.bulls_and_cows;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RatingActivity extends AppCompatActivity {
    class Rating{
        String Login;
        int Points;
    }
    List<Rating> ListRating;
    MyAdapter mAdapter;
    ListView ListRatingGame;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        //убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ListRatingGame=(ListView) findViewById(R.id.listViewRating);
        GetRating();
    }
    //получение списка рейтинга
    private void GetRating(){
        ListRating=new ArrayList<>();
        ProgressDialog pd;
        RequestParams params = new RequestParams();
        params.put("oper", "7");
        pd = new ProgressDialog(RatingActivity.this);
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
                    Rating data;
                    int number;
                    try {
                        JSONObject json=new JSONObject(response);
                        number=json.getInt("number");
                        if(number>0){
                            for(int i=1;i<=number;i++){
                                data=new Rating();
                                data.Login=json.getString("login"+Integer.toString(i));
                                data.Points=json.getInt("points"+Integer.toString(i));
                                ListRating.add(data);
                            }
                            mAdapter = new MyAdapter(getApplicationContext());
                            ListRatingGame.setAdapter(mAdapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Произошла ошибка.", Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
        });
    }
    //Адаптер вывода рейтинга
    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        public MyAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return ListRating.size();
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
            if (convertView == null)
                convertView = mLayoutInflater.inflate(R.layout.list_rating, null);
            TextView TextNumber=(TextView) convertView.findViewById(R.id.textViewRNumber);
            TextView TextLogin=(TextView) convertView.findViewById(R.id.textViewRLogin);
            TextView TextPoints=(TextView) convertView.findViewById(R.id.textViewRPoints);
            Rating data=ListRating.get(position);
            if(data.Login.equals(Environments.Login)){
                Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
                TextNumber.setTypeface(boldTypeface);
                TextLogin.setTypeface(boldTypeface);
                TextPoints.setTypeface(boldTypeface);
            }
            TextNumber.setText(Integer.toString(position+1));
            TextLogin.setText(data.Login);
            TextPoints.setText(Integer.toString(data.Points));
            return convertView;
        }
    }
}
