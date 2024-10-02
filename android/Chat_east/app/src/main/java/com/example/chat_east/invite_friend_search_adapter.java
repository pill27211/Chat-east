package com.example.chat_east;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.Objects;

public class invite_friend_search_adapter extends RecyclerView.Adapter<invite_friend_search_adapter.friendviewholder> {

    @NonNull
    @Override
    public friendviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewtype) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_friend_search, parent, false);
        return new friendviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull friendviewholder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class friendviewholder extends RecyclerView.ViewHolder {

        public friendviewholder(@NonNull View itemview) {
            super(itemview);
        }
    }
}
