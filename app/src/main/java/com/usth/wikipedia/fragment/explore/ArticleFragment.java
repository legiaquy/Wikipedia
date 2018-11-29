package com.usth.wikipedia.fragment.explore;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.usth.wikipedia.DetailArticleActivity;
import com.usth.wikipedia.R;

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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArticleFragment extends Fragment {
    public final String SHARED_PREFERENCES_NAME_SAVE = "save";
    public final String SHARED_PREFERENCES_NAME_HISTORY = "history";

    private String category;
    private TextView title;
    private TextView content;
    private ImageView image;
    private ImageButton save;
    private String titleTemp;
    private Calendar cal;
    private DateFormat dateFormat;
    private String today[], yesterday[];
    private String parseTitle; // Key exchange

    private CardView cardView;

    public ArticleFragment() {
        // Required empty public constructor
    }

    public static ArticleFragment newInstance(int position, String category) {
        ArticleFragment articleFragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putString("category", category);
        args.putInt("position", position);
        articleFragment.setArguments(args);
        return articleFragment;
    }

    public static ArticleFragment newInstance(int position, String category, String titleTemp) {
        ArticleFragment articleFragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("category", category);
        args.putString("titleTemp", titleTemp);
        articleFragment.setArguments(args);
        return articleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.category = getArguments().getString("category");
        this.titleTemp = getArguments().getString("titleTemp");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_article, container, false);
        cardView = (CardView) view.findViewById(R.id.parent);
        cardView.setMaxCardElevation(cardView.getCardElevation() * ShadowTransformerViewPager.CardAdapter.MAX_ELEVATION_FACTOR);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                String SHARED_PREFERENCES_NAME = "history";
                SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME_HISTORY, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String articlesTitle = sharedPreferences.getString(titleTemp, "");
                if (articlesTitle == "") {
                    editor.putString(titleTemp, titleTemp);
                    editor.apply();
                }

                Log.d("ok", titleTemp);

                Intent intent = new Intent(view.getContext(), DetailArticleActivity.class);
                intent.putExtra(DetailArticleActivity.EXTRA_ARTICLETITLE, parseTitle);
                startActivity(intent);
            }
        });

        title = view.findViewById(R.id.title);
        content = view.findViewById(R.id.content);
        image = view.findViewById(R.id.image);

        ShowArticleTask process = new ShowArticleTask();
        process.execute(category, titleTemp);

        // Handle if user click Save article
        save = view.findViewById(R.id.star_button);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "ERROR saving article!";
//                String SHARED_PREFERENCES_NAME = "save_file";

                /**
                 * Create object type SharedPreferences
                 * SHARED_PREFERENCES_NAME: name of file Shared Preferences
                 * MODE_PRIVATE: this mean only this application can access the file
                 */
                SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME_SAVE, Context.MODE_PRIVATE);
                // Open the file
                SharedPreferences.Editor editor = sharedPreferences.edit();

                String articlesTitle = sharedPreferences.getString(titleTemp, "");

                if (articlesTitle == "") {
                    /**
                     * Input data
                     * editor.putX(String key,value)
                     * X: data's type
                     */
                    editor.putString(titleTemp, titleTemp);
                    save.setBackgroundResource(R.drawable.ic_save);

                    if (editor.commit()) {
                        message = "Saved!";
                    }
                } else {
                    message = "ERROR unsaving article!";
                    if (editor.remove(titleTemp).commit()) {
                        message = "Unsaved!";
                    }
                    save.setBackgroundResource(R.drawable.ic_saved_black_24dp);
                }

                Toast toast = Toast.makeText(getActivity(),
                        message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        });

        return view;
    }

    private class ShowArticleTask extends AsyncTask<String, Void, Boolean> {
        private String parseContent;
        private URL parseImage, originURL;
        private Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.no_img);
        private String[] titleArticle = new String[50];
        private JSONObject[] Article = new JSONObject[50];

        protected void FindTitle() {
            try {
                JSONObject JO = new JSONObject(parseContent);
                JSONArray items = JO.getJSONArray("items");
                JSONObject articlesObject = items.getJSONObject(0);
                JSONArray articlesArray = articlesObject.getJSONArray("articles");
//                JSONObject Article1 = articlesArray.getJSONObject(984);
////                JSONObject Article2 = articlesArray.getJSONObject(985);
////                JSONObject Article3 = articlesArray.getJSONObject(986);
                for (int i = 0; i < 11; i++) {
                    Article[i] = articlesArray.getJSONObject(988 - i);
                    titleArticle[i] = Article[i].getString("article");
                }

//                title1 = Article1.getString("article");
//                title2 = Article2.getString("article");
//                title3 = Article3.getString("article");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

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

        protected Date getDate(int d) {
            cal = Calendar.getInstance();
            cal.add(Calendar.DATE, d);
            return cal.getTime();
        }

        protected void onPreExecute() {
            dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            today = dateFormat.format(getDate(-1)).split("/");
            yesterday = dateFormat.format(getDate(-2)).split("/");
        }

        protected Boolean doInBackground(String... articles) {
            String articleCategory = articles[0];
            String articleTitle = articles[1];

            final String url = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts|pageimages|description|pageterms|categories&exintro&explaintext&redirects=1&piprop=thumbnail&pithumbsize=600&titles=";
            try {
                switch (articleCategory) {
                    case "random":
                        originURL = new URL("https://en.wikipedia.org/w/api.php?%20format=json&action=query&prop=extracts|pageimages|pageterms|categories&exsentences=20&exintro&explaintext=&generator=random&grnnamespace=0&piprop=thumbnail&pithumbsize=600");
                        break;
                    case "today":
                        originURL = new URL("https://wikimedia.org/api/rest_v1/metrics/pageviews/top/en.wikipedia/all-access/" + today[0] + "/" + today[1] + "/" + today[2]);
                        ParseContent(originURL); // parse json content from origin url
                        FindTitle(); // find article's title in json content
                        if (articleTitle.equals("0")) {
                            originURL = new URL(url + titleArticle[0]);
                        } else if (articleTitle.equals("1")) {
                            originURL = new URL(url + titleArticle[1]);
                        } else if (articleTitle.equals("2")) {
                            originURL = new URL(url + titleArticle[2]);
                        } else if (articleTitle.equals("3")) {
                            originURL = new URL(url + titleArticle[3]);
                        } else if (articleTitle.equals("4")) {
                            originURL = new URL(url + titleArticle[4]);
                        } else if (articleTitle.equals("5")) {
                            originURL = new URL(url + titleArticle[5]);
                        } else if (articleTitle.equals("6")) {
                            originURL = new URL(url + titleArticle[6]);
                        } else if (articleTitle.equals("7")) {
                            originURL = new URL(url + titleArticle[7]);
                        } else if (articleTitle.equals("8")) {
                            originURL = new URL(url + titleArticle[8]);
                        } else if (articleTitle.equals("9")) {
                            originURL = new URL(url + titleArticle[9]);
                        } else if (articleTitle.equals("10")) {
                            originURL = new URL(url + titleArticle[10]);
                        }
                        break;
                    case "yesterday":
                        originURL = new URL("https://wikimedia.org/api/rest_v1/metrics/pageviews/top/en.wikipedia/all-access/" + yesterday[0] + "/" + yesterday[1] + "/" + yesterday[2]);
                        ParseContent(originURL); // parse json content from origin url
                        FindTitle(); // find article's title in json content
                        if (articleTitle.equals("0")) {
                            originURL = new URL(url + titleArticle[0]);
                        } else if (articleTitle.equals("1")) {
                            originURL = new URL(url + titleArticle[1]);
                        } else if (articleTitle.equals("2")) {
                            originURL = new URL(url + titleArticle[2]);
                        } else if (articleTitle.equals("3")) {
                            originURL = new URL(url + titleArticle[3]);
                        } else if (articleTitle.equals("4")) {
                            originURL = new URL(url + titleArticle[4]);
                        } else if (articleTitle.equals("5")) {
                            originURL = new URL(url + titleArticle[5]);
                        } else if (articleTitle.equals("6")) {
                            originURL = new URL(url + titleArticle[6]);
                        } else if (articleTitle.equals("7")) {
                            originURL = new URL(url + titleArticle[7]);
                        } else if (articleTitle.equals("8")) {
                            originURL = new URL(url + titleArticle[8]);
                        } else if (articleTitle.equals("9")) {
                            originURL = new URL(url + titleArticle[9]);
                        } else if (articleTitle.equals("10")) {
                            originURL = new URL(url + titleArticle[10]);
                        }
                        break;

                }
                ParseContent(originURL);
                JSONObject JO = new JSONObject(parseContent);
                JSONObject query = JO.getJSONObject("query");
                JSONObject pages = query.getJSONObject("pages");
                Iterator<String> keys = pages.keys();
                String id = keys.next();
                JSONObject idJSONObject = pages.getJSONObject(id);

                if (!idJSONObject.isNull("thumbnail")) {
                    JSONObject thumbnail = idJSONObject.getJSONObject("thumbnail");
                    parseImage = new URL(thumbnail.getString("source"));
                    bmp = BitmapFactory.decodeStream(parseImage.openConnection().getInputStream());
                }

                if (!idJSONObject.isNull("title")) {
                    parseTitle = idJSONObject.getString("title");
                } else {
                    parseTitle = "No title";
                }

                if (!idJSONObject.isNull("extract")) {
                    parseContent = idJSONObject.getString("extract");
                } else {
                    parseContent = "There is no information";
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
                title.setText(parseTitle);
                content.setText(parseContent);
                image.setImageBitmap(bmp);

                titleTemp = parseTitle;
            }
        }
    }

    public CardView getCardView() {
        return cardView;
    }

}
