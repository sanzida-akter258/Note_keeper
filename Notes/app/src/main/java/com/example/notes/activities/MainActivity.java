package com.example.notes.activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.notes.R;
import com.example.notes.activities.adapters.notesadapter;
import com.example.notes.activities.entities.note;
import com.example.notes.activities.database.notedatabase;
import com.example.notes.activities.listeners.noteslistener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements noteslistener {

    public static final int REQUEST_CODE_ADD_NOTE =1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;

    public static final int REQUEST_CODE_SHOW_NOTE = 3;
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION=5;

    private RecyclerView notesrecyclerview;
    private notesadapter notesadapter;
    private List<note> noteList;
    private AlertDialog dialogaddurl;

    private int noteclickedposition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageaddnotemain = findViewById(R.id.imageaddnotemain);
        imageaddnotemain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        notesrecyclerview=findViewById(R.id.notesrecyclerview);
        notesrecyclerview.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        noteList= new ArrayList<>();
        notesadapter=new notesadapter(noteList, this);
        notesrecyclerview.setAdapter(notesadapter);

        getnote(REQUEST_CODE_SHOW_NOTE, false);

        EditText inputsearch = findViewById(R.id.inputsearch);
        inputsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesadapter.canceltimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteList.size() != 0){
                    notesadapter.searchnotes(s.toString());
                }
            }
        });
        findViewById(R.id.immageaddnote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });
        findViewById(R.id.immageaddimage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else {
                    selectimage();
                }
            }
        });

        findViewById(R.id.immageaddlink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showaddurldialoge();
            }
        });
    }

    private void selectimage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode ==REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectimage();
            } else {
                Toast.makeText(this, "Permission Denied..!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getpathfromuri(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        String filepath;
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            filepath = cursor.getString(columnIndex);
            cursor.close();
        } else {
            filepath = contentUri.getPath();
        }
        return filepath;
    }

    @Override
    public void onNoteClicked(note note, int position) {
        noteclickedposition= position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isview_or_update", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void getnote(final int requestCode, final boolean isNoteDeleted) {

        @SuppressLint("StaticFieldLeak")
        class getnotestask extends AsyncTask<Void, Void, List<note>> {
            @Override
            protected List<note> doInBackground(Void... voids){
                return notedatabase
                        .getNotedatabase(getApplicationContext())
                        .notedao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<note> notes) {
                super.onPostExecute(notes);
                if(requestCode == REQUEST_CODE_SHOW_NOTE){
                    noteList.addAll(notes);
                    notesadapter.notifyDataSetChanged();
                }else if (requestCode == REQUEST_CODE_ADD_NOTE){
                    noteList.add(0,notes.get(0));
                    notesadapter.notifyItemInserted(0);
                    notesrecyclerview.smoothScrollToPosition(0);
                } else if (requestCode==REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteclickedposition);
                    if(isNoteDeleted){
                        notesadapter.notifyItemRemoved(noteclickedposition);
                    }else {
                        noteList.add(noteclickedposition,notes.get(noteclickedposition));
                        notesadapter.notifyItemChanged(noteclickedposition);
                    }
                }
            }

        }
        new getnotestask().execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getnote(REQUEST_CODE_ADD_NOTE,false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            if (data != null){
                getnote(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if(data != null) {
                Uri selectedimageuri = data.getData();
                if(selectedimageuri != null) {
                    try {
                        String selectedimagepath = getpathfromuri(selectedimageuri);
                        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagepath", selectedimagepath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void showaddurldialoge(){
        if(dialogaddurl==null){
            AlertDialog.Builder builder=new AlertDialog.Builder( MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutaddurlcontainer)
            );
            builder.setView(view);
            dialogaddurl=builder.create();
            if (dialogaddurl.getWindow() != null){
                dialogaddurl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputurl = view.findViewById(R.id.inputurl);
            inputurl.requestFocus();

            view.findViewById(R.id.textadd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(inputurl.getText().toString().trim().isEmpty()){
                        Toast.makeText(MainActivity.this, "Enter URL...", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputurl.getText().toString()).matches()) {
                        Toast.makeText(MainActivity.this, "Enter Valid URL..", Toast.LENGTH_SHORT).show();
                    }else {
                        dialogaddurl.dismiss();
                        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "url");
                        intent.putExtra("url", inputurl.getText().toString());
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    }
                }
            });

            view.findViewById(R.id.textcancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogaddurl.dismiss();
                }
            });
        }
        dialogaddurl.show();
    }
}