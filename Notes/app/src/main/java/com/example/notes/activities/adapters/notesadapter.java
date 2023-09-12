package com.example.notes.activities.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.R;
import com.example.notes.activities.entities.note;
import com.makeramen.roundedimageview.RoundedImageView;
import com.example.notes.activities.listeners.noteslistener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class notesadapter extends RecyclerView.Adapter<notesadapter.noteviewholder>{

    private List<note> notes;

    private noteslistener noteslistener;

    private Timer timer;
    private List<note> notesource;

    public notesadapter(List<note> notes, noteslistener noteslistener) {
        this.notes = notes;
        this.noteslistener = noteslistener;
        this.notesource = notes;
    }

    @NonNull
    @Override
    public noteviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new noteviewholder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull noteviewholder holder, @SuppressLint("RecyclerView") final int position) {
        holder.setnote(notes.get(position));
        holder.layoutnote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteslistener.onNoteClicked(notes.get(position), position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class noteviewholder extends RecyclerView.ViewHolder{

        TextView texttitle, textsubtitle, textdatetime;
        LinearLayout layoutnote;
        RoundedImageView imagenote;

         noteviewholder(@NonNull View itemView) {
            super(itemView);
            texttitle=itemView.findViewById(R.id.texttitle);
            textsubtitle=itemView.findViewById(R.id.textsubtitle);
            textdatetime=itemView.findViewById(R.id.textdatetime);
            layoutnote=itemView.findViewById(R.id.layoutnote);
            imagenote=itemView.findViewById(R.id.imagenote);
        }
        void setnote(note note) {
             texttitle.setText(note.getTitle());
             if (note.getSubtitle().trim().isEmpty()){
                 textsubtitle.setVisibility(View.GONE);
             }else {
                 textsubtitle.setText(note.getSubtitle());
             }
             textdatetime.setText(note.getDatetime());

            GradientDrawable gradientDrawable=(GradientDrawable) layoutnote.getBackground();
            if(note.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }else{
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }
            if (note.getImagepath() != null){
                imagenote.setImageBitmap(BitmapFactory.decodeFile(note.getImagepath()));
                imagenote.setVisibility(View.VISIBLE);
            }else{
                imagenote.setVisibility(View.GONE);
            }
        }
    }
    public void searchnotes(final String searchkeyword) {
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchkeyword.trim().isEmpty()){
                    notes = notesource;
                }else{
                    ArrayList<note> temp = new ArrayList<>();
                    for (note note : notesource) {
                        if (note.getTitle().toLowerCase().contains(searchkeyword.toLowerCase())
                                || note.getSubtitle().toLowerCase().contains(searchkeyword.toLowerCase())
                                || note.getNotetext().toLowerCase().contains(searchkeyword.toLowerCase())) {
                            temp.add(note);
                        }
                        notes=temp;
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        }, 500);
    }
    public void canceltimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
