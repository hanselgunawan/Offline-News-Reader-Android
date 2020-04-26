package com.hanseltritama.offlinenewsreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.Adapter mAdapter;
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> content = new ArrayList<>();

    SQLiteDatabase articlesDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MainAdapter(titles, content);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        articlesDB = this.openOrCreateDatabase("articles", MODE_PRIVATE, null);

        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId INTEGER, title VARCHAR, content VARCHAR)");

        updateList();

        DownloadTask downloadTask = new DownloadTask();

        try {

            downloadTask.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public void updateList() {

        // Use rawQuery for Cursor
        // Use execSQL for void execution
        Cursor c = articlesDB.rawQuery("SELECT * FROM articles", null);

        int titleIndex = c.getColumnIndex("title");
        int contentIndex = c.getColumnIndex("content");

        if (c.moveToFirst()) {

            titles.clear();
            content.clear();

            do {
                titles.add(c.getString(titleIndex));
                content.add(c.getString(contentIndex));
            } while (c.moveToNext());

            mAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(mAdapter);

        }

    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(strings[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {

                    // copy every single character to result String
                    char current = (char) data;

                    result += current;

                    // continue reading OR go to the next character
                    data = reader.read();

                }

                Log.i("URL Content", result);

                JSONArray jsonArray = new JSONArray(result);

                int number_of_items = 20;

                if (jsonArray.length() < 20) number_of_items = jsonArray.length();

                articlesDB.execSQL("DELETE FROM articles");

                for (int i = 0; i < number_of_items; i++) {

                    String articleId = jsonArray.getString(i);

                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleId + ".json?print=pretty");

                    urlConnection = (HttpURLConnection) url.openConnection();

                    in = urlConnection.getInputStream();

                    reader = new InputStreamReader(in);

                    data = reader.read();

                    String articleInfo = "";

                    while (data != -1) {

                        char current = (char) data;

                        articleInfo += current;

                        data = reader.read();  // go to the next character

                    }

//                    Log.i("Article Info" , articleInfo);

                    JSONObject jsonObject = new JSONObject(articleInfo);

                    Log.i("JSON Object", jsonObject.toString());

                    // What if the title OR url does not exist?
                    if (!jsonObject.isNull("title") && !jsonObject.isNull("url")) {

                        String articleTitle = jsonObject.getString("title");
                        String articleURL = jsonObject.getString("url");

                        url = new URL(articleURL);

                        urlConnection = (HttpURLConnection) url.openConnection();

                        in = urlConnection.getInputStream();

                        reader = new InputStreamReader(in);

                        data = reader.read();

                        String articleContent = "";

                        while (data != -1) {

                            char current = (char) data;

                            articleContent += current;

                            data = reader.read();  // go to the next character

                        }

                        String sql = "INSERT INTO articles(articleId, title, content) "
                                    +"VALUES (?, ?, ?)";

                        SQLiteStatement statement = articlesDB.compileStatement(sql);

                        statement.bindString(1, articleId);
                        statement.bindString(2, articleTitle);
                        statement.bindString(3, articleContent);

                        statement.execute();

                    }
                }

            } catch (MalformedURLException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();

            } catch (JSONException e) {

                e.printStackTrace();

            }

            return null;
        }
    }
}
