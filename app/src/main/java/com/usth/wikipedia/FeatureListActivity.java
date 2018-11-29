package com.usth.wikipedia;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// This Activity displays all articles's title in a given featured category
public class FeatureListActivity extends ListActivity {
    private String[] articleNameList; // Store articles' title
    private List<String> articleList; // Store articles' title object
    private ArrayAdapter<String> listDataAdapter; // Article list adapter
    private TextView header; // Feature's name
    public static final String EXTRA_FEATURE = "feature"; // Intent's key


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_list);

        // Set header
        header = findViewById(R.id.feature_name);
        header.setText(getIntent().getExtras().get(EXTRA_FEATURE).toString().toUpperCase(Locale.getDefault()));

        // Bind article item's row layout with this activity
        articleList = new ArrayList<>();
        String temp = "";
        articleList.add(temp); // Prevent null list
        listDataAdapter = new ArrayAdapter<>(this, R.layout.feature_item_list, R.id.feature_item_name, articleList);
        this.setListAdapter(listDataAdapter);

        // Background's thread to find articles given feature's name
        new FindArticleTask().execute(getIntent().getExtras().get(EXTRA_FEATURE).toString());
    }


    // This method is called when user click item's row
    @Override
    protected void onListItemClick(ListView listView, View v, int position, long id) {
        ListAdapter listAdapter = listView.getAdapter(); // get article list adapter
        Object selectItemObj = listAdapter.getItem(position); // get clicked item

        // Intent to read detail article
        Intent intent = new Intent(FeatureListActivity.this, DetailArticleActivity.class);
        intent.putExtra(DetailArticleActivity.EXTRA_ARTICLETITLE, selectItemObj.toString());
        startActivity(intent);
    }


    // Get today | yesterday | random top articles
    private class FindArticleTask extends AsyncTask<String, Void, Boolean> {
        String parseContent; // JSON's content
        URL url; // api url

        // Configure time to get feature article
        Calendar cal;
        DateFormat dateFormat;
        String today[], yesterday[];

        // Method to get JSON's content
        protected void ParseContent(URL url) {
            try {
                URLConnection httpURLConnection = url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                parseContent = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                    parseContent = parseContent + line;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Method to get required date
        protected Date getDate(int d) {
            cal = Calendar.getInstance();
            cal.add(Calendar.DATE, d);
            return cal.getTime();
        }


        protected void onPreExecute() {
            dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            today = dateFormat.format(getDate(-1)).split("/");
            yesterday = dateFormat.format(getDate(-2)).split("/");
            articleNameList = new String[100]; // 100 article's title
            articleList.clear(); // Clear temp
        }

        protected Boolean doInBackground(String... articles) {
            String articleFeature = articles[0];

            try {
                // Switch URL to get required API
                switch (articleFeature) {
                    case "random":
                        url = new URL("https://en.wikipedia.org/w/api.php?format=json&action=query&list=random&rnlimit=100&rnnamespace=0");
                        break;
                    case "today":
                        url = new URL("https://wikimedia.org/api/rest_v1/metrics/pageviews/top/en.wikipedia/all-access/" + today[0] + "/" + today[1] + "/" + today[2]);
                        break;
                    case "yesterday":
                        url = new URL("https://wikimedia.org/api/rest_v1/metrics/pageviews/top/en.wikipedia/all-access/" + yesterday[0] + "/" + yesterday[1] + "/" + yesterday[2]);
                        break;
                }

                // Parse JSON content from API
                ParseContent(url);

                // Get top 100 article in a feature category
                if (articleFeature.equals("random")) {
                    JSONObject JO = new JSONObject(parseContent);
                    JSONObject query = JO.getJSONObject("query");
                    JSONArray random = query.getJSONArray("random");
                    for (int i = 0; i < 100; i++) {
                        JSONObject temp = random.getJSONObject(i);
                        articleNameList[i] = temp.getString("title");
                    }
                } else {
                    JSONObject JO = new JSONObject(parseContent);
                    JSONArray items = JO.getJSONArray("items");
                    JSONObject list = items.getJSONObject(0);
                    JSONArray articlesArray = list.getJSONArray("articles");
                    for (int i = 0; i < 100; i++) {
                        JSONObject temp = articlesArray.getJSONObject(988 - i);
                        articleNameList[i] = temp.getString("article");
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        // Add founded articles to the array list then notify
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(FeatureListActivity.this, "Error Network", Toast.LENGTH_LONG).show();
            } else {
                for (int i = 0; i < 100; i++) {
                    articleList.add(articleNameList[i].replace('_', ' '));
                }
                listDataAdapter.notifyDataSetChanged();
            }
        }
    }
}
