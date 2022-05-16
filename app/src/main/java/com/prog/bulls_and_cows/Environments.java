package com.prog.bulls_and_cows;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaPlayer;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Environments {
    public static String UrlServer="http://82.146.55.10";
    public static int ScreenWidth, ScreenHeight;//ширина и высота экрана
    //НАСТРОЙКИ ИГРЫ
    public static boolean Sound;//включить/выключить звуки
    public static SettingsGameCl SettingsGame;
    public static String Login;//логин пользователя
    public static int Points;//общее число очков у пользователя
    //класс структуры настроек игры
    public static class SettingsGameCl{
        public int ViewGame;//тип игры, 1- числа, 2- буквы
        public int NumberSymbols;//кол-во символов в игре
        public boolean NumberNullFirst;//первая цифра может быть 0
        public boolean RepeatSymbols;//повторяющиеся символы да true, нет false
    }
    //масштабирование изображения
    public Bitmap GetResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
}
