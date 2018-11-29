package com.usth.wikipedia.fragment.search;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.usth.wikipedia.model.ArticleRepo;
import com.usth.wikipedia.DetailArticleActivity;
import com.usth.wikipedia.R;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    Context mContext;
    LayoutInflater mInflate;
    ArrayList<ArticleRepo> mArticleRepo;
    ArrayList<ArticleRepo> mArrayList;

    public ListViewAdapter(Context context, ArrayList<ArticleRepo> articleRepo) {
        mContext = context;
        mInflate = LayoutInflater.from(context);
        mArticleRepo = articleRepo;
        mArrayList = new ArrayList<>();
        mArrayList.addAll(articleRepo);
    }


    @Override
    public int getCount() {
        return mArticleRepo.size();
    }

    @Override
    public ArticleRepo getItem(int position) {
        return mArticleRepo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        TextView name;
        TextView description;
        ImageView image;
        LinearLayout item;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflate.inflate(R.layout.article_list, parent, false);
            holder.name = convertView.findViewById(R.id.list_title);
            holder.description = convertView.findViewById(R.id.list_description);
            holder.image = convertView.findViewById(R.id.list_image);
            holder.item = convertView.findViewById(R.id.list_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(mArticleRepo.get(position).getName());
        holder.description.setText(mArticleRepo.get(position).getDescription());
        holder.image.setImageBitmap(mArticleRepo.get(position).getImage());
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DetailArticleActivity.class);
                intent.putExtra(DetailArticleActivity.EXTRA_ARTICLETITLE, holder.name.getText());
                v.getContext().startActivity(intent);
            }
        });

        return convertView;
    }

    //     Filter Class
    public void filter() {
        mArticleRepo.clear();
        notifyDataSetChanged();
    }
}
