package com.isi.passwordgame.ui;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.isi.passwordgame.R;

public class NameViewHolder extends RecyclerView.ViewHolder {
    public TextView textView;

    public NameViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.nameCardTextView);
    }
}