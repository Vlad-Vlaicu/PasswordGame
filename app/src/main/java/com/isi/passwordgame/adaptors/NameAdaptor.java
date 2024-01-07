package com.isi.passwordgame.adaptors;

// StringAdapter.java
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.isi.passwordgame.R;
import com.isi.passwordgame.ui.NameViewHolder;

import java.util.List;

public class NameAdaptor extends RecyclerView.Adapter<NameViewHolder> {
    private List<String> dataList;
    private Context context;

    public NameAdaptor(List<String> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    @NonNull
    @Override
    public NameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
        return new NameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NameViewHolder holder, int position) {
        String data = dataList.get(position);
        holder.textView.setText(data);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateData(List<String> newData) {
        dataList = newData;
        notifyDataSetChanged();

    }
}