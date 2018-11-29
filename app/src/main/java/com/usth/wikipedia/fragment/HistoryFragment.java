package com.usth.wikipedia.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.usth.wikipedia.DetailArticleActivity;
import com.usth.wikipedia.R;
import com.usth.wikipedia.fragment.search.ListViewAdapter;
import com.usth.wikipedia.model.ArticleRepo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {
    private ListViewAdapter adapter;
    private ListView list;
    private ImageButton reresh;
    //    private String[] articleNameList;
//    private Bitmap[] articleImageList;
//    private String[] articleDescriptionList;
    private ArrayList<ArticleRepo> arrayList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View view = inflater.inflate(R.layout.fragment_history, container, false);
//        reresh = view.findViewById(R.id.refresh_button);
//        reresh.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getActivity().recreate();
//            }
//        });

        list = view.findViewById(R.id.list_article);
        final ArticleRepo example = new ArticleRepo("Example", "Ex", BitmapFactory.decodeResource(getResources(), R.drawable.no_img));
        arrayList.add(example); // Add a temporary item in arrayList to prevent null array

        adapter = new ListViewAdapter(view.getContext(), arrayList);
        adapter.filter();
        list.setAdapter(adapter);


        SharedPreferences sharedPreferences = getContext().getSharedPreferences("history", Context.MODE_PRIVATE);

        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            new FindArticleTask().execute(entry.getKey());
        }

        return view;
    }

    private class FindArticleTask extends AsyncTask<String, Void, Boolean> {
        private String parseContent, parseDescription, parseTitle;
        private URL parseImage, originURL;
        private Bitmap bmp;

        protected Boolean doInBackground(String... articles) { // "String... articles": Array parameter
            String articleTitle = articles[0];
            try {
                // Connect to wikipedia API
                originURL = new URL("https://en.wikipedia.org/w/api.php?format=json&action=query&prop=pageimages|description&exsectionformat=raw&explaintext&redirects=1&piprop=thumbnail&pithumbsize=150&titles=" + articleTitle);
                HttpURLConnection httpURLConnection = (HttpURLConnection) originURL.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                parseContent = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                    parseContent = parseContent + line;
                }

                JSONObject JO = new JSONObject(parseContent);
                JSONObject query = JO.getJSONObject("query");
                JSONObject pages = query.getJSONObject("pages");
                Iterator<String> keys = pages.keys();
                String id = keys.next();
                JSONObject idJSONObject = pages.getJSONObject(id);

                // Parse content
                if (!idJSONObject.isNull("description")) {
                    parseDescription = idJSONObject.getString("description");
                } else {
                    parseDescription = "";
                }
                parseTitle = idJSONObject.getString("title");

                if (!idJSONObject.isNull("thumbnail")) {
                    JSONObject thumbnail = idJSONObject.getJSONObject("thumbnail");
                    parseImage = new URL(thumbnail.getString("source"));
                    bmp = BitmapFactory.decodeStream(parseImage.openConnection().getInputStream());
                } else {
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.no_img);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(getActivity(), "Error network", Toast.LENGTH_SHORT).show();
            } else {
                ArticleRepo articleRepo = new ArticleRepo(parseTitle, parseDescription, bmp);
                arrayList.add(articleRepo);
                adapter.notifyDataSetChanged();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("history", Context.MODE_PRIVATE);

        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            new FindArticleTask().execute(entry.getKey());
        }
    }
}
