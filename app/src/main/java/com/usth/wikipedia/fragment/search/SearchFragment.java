package com.usth.wikipedia.fragment.search;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.usth.wikipedia.R;
import com.usth.wikipedia.model.ArticleRepo;

import org.json.JSONArray;
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

import javax.net.ssl.HttpsURLConnection;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements SearchView.OnQueryTextListener {

    private ListView list;
    private ListViewAdapter adapter;
    private SearchView searchView;
    private Button moreButton;
    private String[] articleNameList;
    private Bitmap[] articleImageList;
    private String[] articleDescriptionList;
    private ArrayList<ArticleRepo> arrayList = new ArrayList<>();

    private RelativeLayout bottomLayout;

    int more, length; // to load more item in list view
    String key_word;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_search, container, false);

        bottomLayout = view.findViewById(R.id.loadItemsLayout_listView);

        // ListView
        list = view.findViewById(R.id.list_article);

        final ArticleRepo example = new ArticleRepo("Example", "Ex", BitmapFactory.decodeResource(getResources(), R.drawable.no_img));
        arrayList.add(example); // Add a temporary item in arrayList to prevent null array

        adapter = new ListViewAdapter(view.getContext(), arrayList);
        adapter.filter(); // Clear arrayList

        // Binds the Adapter to the ListView
        list.setAdapter(adapter);

        searchView = view.findViewById(R.id.search_view);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);

        moreButton = view.findViewById(R.id.more_button);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateListView();
            }
        });


        return view;
    }

    private void updateListView() {
        bottomLayout.setVisibility(View.VISIBLE);
        more += 10;
        length += 10;
        new SearchArticleTask().execute(key_word, String.valueOf(more), String.valueOf(length));

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.filter();
        length = 10;
        more = 0;
        key_word = query; // to update listview
        SearchArticleTask process = new SearchArticleTask(); // Search article in background
        process.execute(query, String.valueOf(more), String.valueOf(length));
        searchView.clearFocus();
        bottomLayout.setVisibility(View.VISIBLE);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter();
        moreButton.setVisibility(View.GONE);
        return true;
    }


    private class SearchArticleTask extends AsyncTask<String, Void, Boolean> {
        private String parseContent;
        private URL originURL;

        private class Info {
            private String desc;
            private Bitmap bmp;

            public Info(Bitmap bmp, String desc) {
                this.desc = desc;
                this.bmp = bmp;
            }

            public Bitmap getBmp() {
                return bmp;
            }

            public String getDesc() {
                return desc;
            }
        }

        private Info parseInfo(String title) {
            Info info;
            String json, desc = "";
            URL originURL;
            URL imageUrl;
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.no_img);
            JSONObject idJSONObject;
            try {
                originURL = new URL("https://en.wikipedia.org/w/api.php?format=json&action=query&prop=pageimages|description&redirects=1&piprop=thumbnail&pithumbsize=100&titles=" + title);
                HttpURLConnection httpURLConnection = (HttpURLConnection) originURL.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader((new InputStreamReader(inputStream)));
                String line = "";
                json = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                    json = json + line;
                }
                JSONObject JO = new JSONObject(json);
                JSONObject query = JO.getJSONObject("query");
                JSONObject pages = query.getJSONObject("pages");
                Iterator<String> keys = pages.keys();
                String id = keys.next();
                idJSONObject = pages.getJSONObject(id);
                if (!idJSONObject.isNull("thumbnail")) {
                    JSONObject thumbnail = idJSONObject.getJSONObject("thumbnail");
                    imageUrl = new URL(thumbnail.getString("source"));
                    image = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
                }

                if (!idJSONObject.isNull("description")) {
                    desc = idJSONObject.getString("description");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            info = new Info(image, desc);
            return info;
        }

        protected void onPreExecute() {
            articleNameList = new String[10];
            articleDescriptionList = new String[10];
            articleImageList = new Bitmap[10];
        }

        protected Boolean doInBackground(String... articles) {
            String articleTitle = articles[0];
            String articleMore = articles[1];
            String articleLength = articles[2];

            int loadLength = Integer.parseInt(articleLength);
            int loadMore = Integer.parseInt(articleMore);
            try {
                originURL = new URL("https://en.wikipedia.org/w/api.php?format=json&action=opensearch&limit=50&namespace=0&profile=engine_autoselect&search=" + articleTitle);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) originURL.openConnection();
//                httpsURLConnection.setRequestMethod("GET");
//                httpsURLConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
                InputStream inputStream = httpsURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader((new InputStreamReader(inputStream)));
                String line = "";
                parseContent = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                    parseContent = parseContent + line;
                }
                JSONArray JO = new JSONArray(parseContent);
                JSONArray title = JO.getJSONArray(1);
                if (loadLength > title.length()) {
                    loadLength = title.length();
                }


                int j = 0;
                for (int i = loadMore; i < loadLength; i++) {
                    articleNameList[j] = title.getString(i);
                    Info info = parseInfo(articleNameList[j]);
                    articleImageList[j] = info.getBmp();
                    articleDescriptionList[j] = info.getDesc();
                    j++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(getActivity(), "No Network", Toast.LENGTH_SHORT).show();
            } else {
                if (articleNameList[0] != null) {
                    int j = 0;
                    while (j < articleNameList.length) {
                        ArticleRepo articleRepo = new ArticleRepo(articleNameList[j], articleDescriptionList[j], articleImageList[j]);
                        //Binds all object into an array
                        arrayList.add(articleRepo);
                        j++;
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                    bottomLayout.setVisibility(View.GONE);
                    moreButton.setVisibility(View.VISIBLE);
                } else {
                    bottomLayout.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "No results", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

