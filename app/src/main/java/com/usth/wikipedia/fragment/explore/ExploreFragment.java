package com.usth.wikipedia.fragment.explore;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.usth.wikipedia.DetailArticleActivity;
import com.usth.wikipedia.FeatureListActivity;
import com.usth.wikipedia.R;
import com.usth.wikipedia.model.ArticleRepo;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;


public class ExploreFragment extends Fragment {

    //View pager declaration
    private WrapContentViewPager viewPager1, viewPager2, viewPager3; // wrap_content viewpager
    private ArticlePagerAdapter adapter1, adapter2, adapter3; // Card pager adapter
    private Button todayButton, yesterdayButton, randomButton; // feature list button

    // Search view declaration
    private String[] articleNameList;
    private ArrayList<ArticleRepo> arrayList = new ArrayList<>();
    private SearchView searchView;
    private SearchView.SearchAutoComplete searchAutoComplete; // dropdown autocomplete suggestion
    private SearchAdapter searchAdapter;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.fragment_explore, container, false);

        // Button
        todayButton = layout.findViewById(R.id.today_list);
        yesterdayButton = layout.findViewById(R.id.yesterday_list);
        randomButton = layout.findViewById(R.id.random_list);

        // Viewpager
        viewPager3 = layout.findViewById(R.id.viewpager3);
        viewPager2 = layout.findViewById(R.id.viewpager2);
        viewPager1 = layout.findViewById(R.id.viewpager1);

        // Viewpager's adapter
        adapter1 = new ArticlePagerAdapter(getActivity().getSupportFragmentManager(), "today", dpToPixels(2, layout.getContext()));
        adapter2 = new ArticlePagerAdapter(getActivity().getSupportFragmentManager(), "yesterday", dpToPixels(2, layout.getContext()));
        adapter3 = new ArticlePagerAdapter(getActivity().getSupportFragmentManager(), "random", dpToPixels(2, layout.getContext()));

        // Add onPageScroll listener to viewpager
        ShadowTransformerViewPager fragmentArticleShadowTransformerViewPager1 = new ShadowTransformerViewPager(viewPager1, adapter1);
        fragmentArticleShadowTransformerViewPager1.enableScaling(true);

        ShadowTransformerViewPager fragmentArticleShadowTransformerViewPager2 = new ShadowTransformerViewPager(viewPager2, adapter2);
        fragmentArticleShadowTransformerViewPager2.enableScaling(true);

        ShadowTransformerViewPager fragmentArticleShadowTransformerViewPager3 = new ShadowTransformerViewPager(viewPager3, adapter3);
        fragmentArticleShadowTransformerViewPager3.enableScaling(true);

        // SearchView
        searchView = layout.findViewById(R.id.auto_complete_search);
        searchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setDropDownBackgroundResource(R.drawable.article_list_backgroundcolor);
        searchAutoComplete.setThreshold(1);
        searchAutoComplete.setDropDownAnchor(R.id.auto_complete_search);
        searchAutoComplete.setDropDownWidth(200);

        // Setup Search view
        ArticleRepo temp = new ArticleRepo("");
        arrayList.add(temp); // Prevent null array

        searchAdapter = new SearchAdapter(layout.getContext(), R.layout.explore_search_list, arrayList);

        searchAutoComplete.setAdapter(searchAdapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent intent = new Intent(layout.getContext(), DetailArticleActivity.class);
                ArticleRepo articleRepo = arrayList.get(0);
                intent.putExtra(DetailArticleActivity.EXTRA_ARTICLETITLE, articleRepo.getName());
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                s = s.toLowerCase();
                searchAdapter.filter(s);
                new SearchArticleTask().execute(s);
                return false;
            }
        });

        // Search's item click listener
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), DetailArticleActivity.class);
                ArticleRepo article = (ArticleRepo) parent.getItemAtPosition(position);
                intent.putExtra(DetailArticleActivity.EXTRA_ARTICLETITLE, article.getName());
                startActivity(intent);
            }
        });

        // Limit loaded pages
        viewPager3.setOffscreenPageLimit(2);
        viewPager3.setAdapter(adapter3);

        viewPager1.setOffscreenPageLimit(2);
        viewPager1.setAdapter(adapter1);

        viewPager2.setOffscreenPageLimit(2);
        viewPager2.setAdapter(adapter2);

        // Set click listener to feature list button
        todayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FeatureListActivity.class);
                intent.putExtra(FeatureListActivity.EXTRA_FEATURE, "today");
                startActivity(intent);
            }
        });

        yesterdayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FeatureListActivity.class);
                intent.putExtra(FeatureListActivity.EXTRA_FEATURE, "yesterday");
                startActivity(intent);
            }
        });

        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FeatureListActivity.class);
                intent.putExtra(FeatureListActivity.EXTRA_FEATURE, "random");
                startActivity(intent);
            }
        });
        Toast.makeText(getActivity(), "Ready", Toast.LENGTH_SHORT).show();
        return layout;
    }


    // ViewPager's adapter
    class ArticlePagerAdapter extends FragmentStatePagerAdapter implements ShadowTransformerViewPager.CardAdapter {
        private float baseElevation;
        private String category;
        private List<ArticleFragment> fragments;

        public ArticlePagerAdapter(FragmentManager fm, String category, float baseElevation) {
            super(fm);
            fragments = new ArrayList<>();
            this.baseElevation = baseElevation;
            this.category = category;

            for (int i = 0; i < 11; i++) {
                addArticleFragment(new ArticleFragment());
            }
        }

        @Override
        public float getBaseElevation() {
            return baseElevation;
        }

        @Override
        public CardView getCardViewAt(int position) {
            return fragments.get(position).getCardView();
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(final int position) {
            switch (category) {
                case "random":
                    switch (position) {
                        case 0:
                            return ArticleFragment.newInstance(0, category);
                        case 1:
                            return ArticleFragment.newInstance(1, category);
                        case 2:
                            return ArticleFragment.newInstance(2, category);
                        case 3:
                            return ArticleFragment.newInstance(3, category);
                        case 4:
                            return ArticleFragment.newInstance(4, category);
                        case 5:
                            return ArticleFragment.newInstance(5, category);
                        case 6:
                            return ArticleFragment.newInstance(6, category);
                        case 7:
                            return ArticleFragment.newInstance(7, category);
                        case 8:
                            return ArticleFragment.newInstance(8, category);
                        case 9:
                            return ArticleFragment.newInstance(9, category);
                        case 10:
                            return ArticleFragment.newInstance(10, category);
                        default:
                            return null;
                    }
                case "today":
                    switch (position) {
                        case 0:
                            return ArticleFragment.newInstance(0, category, "0");
                        case 1:
                            return ArticleFragment.newInstance(1, category, "1");
                        case 2:
                            return ArticleFragment.newInstance(2, category, "2");
                        case 3:
                            return ArticleFragment.newInstance(3, category, "3");
                        case 4:
                            return ArticleFragment.newInstance(4, category, "4");
                        case 5:
                            return ArticleFragment.newInstance(5, category, "5");
                        case 6:
                            return ArticleFragment.newInstance(6, category, "6");
                        case 7:
                            return ArticleFragment.newInstance(7, category, "7");
                        case 8:
                            return ArticleFragment.newInstance(8, category, "8");
                        case 9:
                            return ArticleFragment.newInstance(9, category, "9");
                        case 10:
                            return ArticleFragment.newInstance(10, category, "10");
                        default:
                            return null;
                    }
                case "yesterday":
                    switch (position) {
                        case 0:
                            return ArticleFragment.newInstance(0, category, "0");
                        case 1:
                            return ArticleFragment.newInstance(1, category, "1");
                        case 2:
                            return ArticleFragment.newInstance(2, category, "2");
                        case 3:
                            return ArticleFragment.newInstance(3, category, "3");
                        case 4:
                            return ArticleFragment.newInstance(4, category, "4");
                        case 5:
                            return ArticleFragment.newInstance(5, category, "5");
                        case 6:
                            return ArticleFragment.newInstance(6, category, "6");
                        case 7:
                            return ArticleFragment.newInstance(7, category, "7");
                        case 8:
                            return ArticleFragment.newInstance(8, category, "8");
                        case 9:
                            return ArticleFragment.newInstance(9, category, "9");
                        case 10:
                            return ArticleFragment.newInstance(10, category, "10");
                        case 11:
                            return ArticleFragment.newInstance(11, category, "11");
                        default:
                            return null;
                    }
                default:
                    return null;
            }
        }

        // Create fragment at a related position
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object fragment = super.instantiateItem(container, position);
            fragments.set(position, (ArticleFragment) fragment);
            return fragment;
        }

        //
        public void addArticleFragment(ArticleFragment fragment) {
            fragments.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Article" + position;
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    // Search article
    private class SearchArticleTask extends AsyncTask<String, Void, Boolean> {
        private String parseContent;
        private URL originUrl;

        protected void onPreExecute() {
            articleNameList = new String[5];
        }

        protected Boolean doInBackground(String... titles) {
            String articleTitle = titles[0];
            try {
                originUrl = new URL("https://en.wikipedia.org/w/api.php?format=json&action=opensearch&limit=5&namespace=0&profile=engine_autoselect&search=" + articleTitle);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) originUrl.openConnection();
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
                for (int i = 0; i < title.length(); i++) {
                    articleNameList[i] = title.getString(i);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(getActivity(), "No result", Toast.LENGTH_SHORT).show();
            } else {
                for (int i = 0; i < articleNameList.length; i++) {
                    ArticleRepo articleRepo = new ArticleRepo(articleNameList[i]);
                    arrayList.add(articleRepo);
                    searchAdapter.notifyDataSetChanged();
                }
            }

        }
    }

    private class SearchAdapter extends ArrayAdapter<ArticleRepo> {
        private Context mContext;
        private int mResourceId;
        private List<ArticleRepo> mItems;
        private String searchString = "";

        public SearchAdapter(Context context, int resourceId, ArrayList<ArticleRepo> items) {
            super(context, resourceId, items);
            mContext = context;
            mItems = items;
            mResourceId = resourceId;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            try {
                if (convertView == null) {
                    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                    view = inflater.inflate(mResourceId, parent, false);
                }
                final ArticleRepo articleRepo = getItem(position);
                final TextView title = view.findViewById(R.id.explore_search_title);

                // Detect matching query characters
                title.setText(articleRepo.getName());
                String article = articleRepo.getName().toLowerCase(Locale.getDefault());
                if (article.contains(searchString)) {
                    int startPos = article.indexOf(searchString);
                    int endPos = startPos + searchString.length();

                    Spannable spanText = Spannable.Factory.getInstance().newSpannable(title.getText());
                    spanText.setSpan(new ForegroundColorSpan(Color.BLACK), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spanText.setSpan(new UnderlineSpan(), startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    title.setText(spanText, TextView.BufferType.SPANNABLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return view;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public ArticleRepo getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void filter(String searchString) {
            mItems.clear();
            notifyDataSetChanged();
            this.searchString = searchString;
        }
    }

    public static float dpToPixels(int dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().density);
    }


}




