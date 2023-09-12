package com.example.notes.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notes.R;
import com.example.notes.activities.database.notedatabase;
import com.example.notes.activities.entities.note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.regex.Pattern;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText inputnotetitle,inputnotesubtitle,inputnotetext;
    private TextView textdatetime;

    private String selectnotecolor;

    private View viewsubtitleindecator;

    private ImageView imagenote;

    private String selectimagepath;

    private TextView textweburl;

    private LinearLayout layoutweburl;

    private static final int REQUEST_CODE_STORAGE_PERMISSION =1;
    private static final int REQUEST_CODE_SELECT_IMAGE=2;

    private AlertDialog dialogaddurl;
    private AlertDialog dialogdeletenote;
    private note alreadyavailable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        ImageView imageback = findViewById(R.id.imageback);
        imageback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        inputnotetitle=findViewById(R.id.inputnotetitle);
        inputnotesubtitle=findViewById(R.id.inputnotesubtitle);
        inputnotetext=findViewById(R.id.inputnote);
        viewsubtitleindecator=findViewById(R.id.viewsubtitleindicator);
        imagenote=findViewById(R.id.imagenote);
        textweburl=findViewById(R.id.textweburl);
        layoutweburl=findViewById(R.id.layoutweburl);
        // Find and initialize the textdatetime TextView
        textdatetime = findViewById(R.id.textdatetime);

        // Set the formatted date and time to the textdatetime TextView
        textdatetime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date())
        );

        ImageView imagesave=findViewById(R.id.imagesave);
        imagesave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savenote();
            }
        });

        selectnotecolor="#333333";
        selectimagepath="";
        initiatemiscellaneous();
        setsubtitleindicatorcolor();

        if(getIntent().getBooleanExtra("isview_or_update", false)) {
            alreadyavailable = (note) getIntent().getSerializableExtra("note");
            viewOrUpdateNote();
        }

        findViewById(R.id.imageremovewebURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textweburl.setText(null);
                layoutweburl.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.imageremoveimage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagenote.setImageBitmap(null);
                imagenote.setVisibility(View.GONE);
                findViewById(R.id.imageremoveimage).setVisibility(View.GONE);
                selectimagepath= "";
            }
        });

        if(getIntent().getBooleanExtra("isFromQuickActions", false)){
            String type = getIntent().getStringExtra("quickActionType");
            if(type != null) {
                if(type.equals("image")) {
                    selectimagepath = getIntent().getStringExtra("imagepath");
                    imagenote.setImageBitmap(BitmapFactory.decodeFile(selectimagepath));
                    imagenote.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageremoveimage).setVisibility(View.VISIBLE);
                } else if (type.equals("url")) {
                    textweburl.setText(getIntent().getStringExtra("url"));
                    layoutweburl.setVisibility(View.VISIBLE);
                }
            }
        }

        initiatemiscellaneous();
        setsubtitleindicatorcolor();

    }

    private void viewOrUpdateNote(){
        inputnotetitle.setText(alreadyavailable.getTitle());
        inputnotesubtitle.setText(alreadyavailable.getSubtitle());
        inputnotetext.setText(alreadyavailable.getNotetext());
        textdatetime.setText(alreadyavailable.getDatetime());

        if(alreadyavailable.getImagepath() != null && !alreadyavailable.getImagepath().trim().isEmpty()){
            imagenote.setImageBitmap(BitmapFactory.decodeFile(alreadyavailable.getImagepath()));
            imagenote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageremoveimage).setVisibility(View.VISIBLE);
            selectimagepath = alreadyavailable.getImagepath();
        }
        if(alreadyavailable.getWeb_link() != null && !alreadyavailable.getWeb_link().trim().isEmpty()){
            textweburl.setText(alreadyavailable.getWeb_link());
            layoutweburl.setVisibility(View.VISIBLE);
        }
    }
    private void savenote() {
        if (inputnotetitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note title cannot be empty..!!", Toast.LENGTH_SHORT).show();
            return;
        } else if (inputnotesubtitle.getText().toString().trim().isEmpty() && inputnotetext.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note cannot be empty..!!", Toast.LENGTH_SHORT).show();
            return;
        }

        final note note = new note();
        note.setTitle(inputnotetitle.getText().toString());
        note.setSubtitle(inputnotesubtitle.getText().toString());
        note.setNotetext(inputnotetext.getText().toString());
        note.setDatetime(textdatetime.getText().toString());
       // if (textdatetime != null) {
         //   String datetime = textdatetime.getText().toString();
           // note.setDatetime(datetime);
       // } else {
            // Handle the case where note or textdatetime is null
       // }
        note.setColor(selectnotecolor);
        note.setImagepath(selectimagepath);

        if(layoutweburl.getVisibility() == View.VISIBLE){
            note.setWeb_link(textweburl.getText().toString());
        }

        if(alreadyavailable != null) {
            note.setId(alreadyavailable.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class savenotetask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                notedatabase.getNotedatabase(getApplicationContext()).notedao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent=new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }
        new savenotetask().execute();
    }
    private void initiatemiscellaneous(){
        final LinearLayout layoutmeiscellaneous = findViewById(R.id.layoutmiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior= BottomSheetBehavior.from(layoutmeiscellaneous);
        layoutmeiscellaneous.findViewById(R.id.textmiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        final ImageView imagecolor= layoutmeiscellaneous.findViewById(R.id.imagecolor);
        final ImageView imagecolor2= layoutmeiscellaneous.findViewById(R.id.imagecolor2);
        final ImageView imagecolor3= layoutmeiscellaneous.findViewById(R.id.imagecolor3);
        final ImageView imagecolor4= layoutmeiscellaneous.findViewById(R.id.imagecolor4);
        final ImageView imagecolor5= layoutmeiscellaneous.findViewById(R.id.imagecolor5);

        layoutmeiscellaneous.findViewById(R.id.viewcolor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectnotecolor="#333333";
                imagecolor.setImageResource(R.drawable.done);
                imagecolor2.setImageResource(0);
                imagecolor3.setImageResource(0);
                imagecolor4.setImageResource(0);
                imagecolor5.setImageResource(0);
                setsubtitleindicatorcolor();
            }
        });

        layoutmeiscellaneous.findViewById(R.id.viewcolor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectnotecolor="#FDBE3b";
                imagecolor.setImageResource(0);
                imagecolor2.setImageResource(R.drawable.done);
                imagecolor3.setImageResource(0);
                imagecolor4.setImageResource(0);
                imagecolor5.setImageResource(0);
                setsubtitleindicatorcolor();
            }
        });

        layoutmeiscellaneous.findViewById(R.id.viewcolor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectnotecolor="#FF4842";
                imagecolor.setImageResource(0);
                imagecolor2.setImageResource(0);
                imagecolor3.setImageResource(R.drawable.done);
                imagecolor4.setImageResource(0);
                imagecolor5.setImageResource(0);
                setsubtitleindicatorcolor();
            }
        });

        layoutmeiscellaneous.findViewById(R.id.viewcolor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectnotecolor="#3A52Fc";
                imagecolor.setImageResource(0);
                imagecolor2.setImageResource(0);
                imagecolor3.setImageResource(0);
                imagecolor4.setImageResource(R.drawable.done);
                imagecolor5.setImageResource(0);
                setsubtitleindicatorcolor();
            }
        });

        layoutmeiscellaneous.findViewById(R.id.viewcolor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectnotecolor="#000000";
                imagecolor.setImageResource(0);
                imagecolor2.setImageResource(0);
                imagecolor3.setImageResource(0);
                imagecolor4.setImageResource(0);
                imagecolor5.setImageResource(R.drawable.done);
                setsubtitleindicatorcolor();
            }
        });

        if(alreadyavailable != null && alreadyavailable.getColor() != null && !alreadyavailable.getColor().trim().isEmpty()){
            switch (alreadyavailable.getColor()){
                case "#FDBE3b" :
                    layoutmeiscellaneous.findViewById(R.id.viewcolor2).performClick();
                    break;
                case "#FF4842" :
                    layoutmeiscellaneous.findViewById(R.id.viewcolor3).performClick();
                    break;

                case "#3A52Fc" :
                    layoutmeiscellaneous.findViewById(R.id.viewcolor4).performClick();
                    break;

                case "#000000" :
                    layoutmeiscellaneous.findViewById(R.id.viewcolor5).performClick();
                    break;
            }

        }

        layoutmeiscellaneous.findViewById(R.id.layoutaddimage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else {
                    selectimage();
                }
            }
        });

        layoutmeiscellaneous.findViewById(R.id.layoutaddurl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showaddurldialoge();
            }
        });
        if (alreadyavailable != null){
            layoutmeiscellaneous.findViewById(R.id.layoutdeletenote).setVisibility(View.VISIBLE);
            layoutmeiscellaneous.findViewById(R.id.layoutdeletenote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }
    }

    private void showDeleteNoteDialog() {
        if (dialogdeletenote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    findViewById(R.id.layoutdeletenotecontainer)
            );
            builder.setView(view);
            dialogdeletenote = builder.create();
            if (dialogdeletenote.getWindow() != null) {
                dialogdeletenote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textdeletenote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DeleteNoteTask().execute();
                }
            });
            view.findViewById(R.id.textcancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogdeletenote.dismiss();
                }
            });
        }
        dialogdeletenote.show();
    }

    private class DeleteNoteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            notedatabase.getNotedatabase(getApplicationContext()).notedao()
                    .deleteNote(alreadyavailable);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent();
            intent.putExtra("isNoteDeleted", true);
            setResult(RESULT_OK, intent);
            finish();
        }
    }


    /*private void showdeletenotedialouge(){
        if(dialogdeletenote == null){
            AlertDialog.Builder builder= new AlertDialog.Builder(CreateNoteActivity.this);
            View view= LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layoutdeletenotecontainer)
            );
            builder.setView(view);
            dialogdeletenote=builder.create();
            if(dialogdeletenote.getWindow() != null){
                dialogdeletenote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textdeletenote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    @SuppressLint("StaticFieldLeak")
                    class deleteNoteTask extends AsyncTask<Void, Void, Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            notedatabase.getNotedatabase(getApplicationContext()).notedao()
                                    .deleteNote(alreadyavailable);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent= new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    new deleteNoteTask().execute();
                }
            });
            view.findViewById(R.id.textcancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogdeletenote.dismiss();
                }
            });
        }
    }*/
    private void setsubtitleindicatorcolor(){
        GradientDrawable gradientDrawable=(GradientDrawable) viewsubtitleindecator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectnotecolor));
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
        if(requestCode ==REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectimage();
            }else{
                Toast.makeText(this,"Permission Denied..!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            try {
                Uri selectImageUri = data.getData();
                if (selectImageUri != null) {
                    ContentResolver contentResolver = getContentResolver();
                    InputStream inputStream = contentResolver.openInputStream(selectImageUri);
                    if (inputStream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        if (bitmap != null) {
                            imagenote.setImageBitmap(bitmap);
                            imagenote.setVisibility(View.VISIBLE);
                            findViewById(R.id.imageremoveimage).setVisibility(View.VISIBLE);
                            selectimagepath = getpathfromuri(selectImageUri);
                        } else {
                            Toast.makeText(this, "Failed to decode bitmap", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to open input stream", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Selected image URI is null", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception exception) {
                Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_SELECT_IMAGE && resultCode==RESULT_OK){
            if (data != null){
                Uri selectimageuri = data.getData();
                if(selectimageuri != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectimageuri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imagenote.setImageBitmap(bitmap);
                        imagenote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageremoveimage).setVisibility(View.VISIBLE);
                        selectimagepath=getpathfromuri(selectimageuri);
                    }catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }*/
   /* private String getpathfromuri(Uri contentUri){
        String filepath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null , null , null , null );
        if (cursor == null){
            filepath= contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("data");
            filepath= cursor.getString(index);
            cursor.close();
        }
        return filepath;
    }*/

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


    private void showaddurldialoge(){
        if(dialogaddurl==null){
            AlertDialog.Builder builder=new AlertDialog.Builder( CreateNoteActivity.this);
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
                        Toast.makeText(CreateNoteActivity.this, "Enter URL...", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputurl.getText().toString()).matches()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter Valid URL..", Toast.LENGTH_SHORT).show();
                    }else {
                        textweburl.setText(inputurl.getText().toString());
                        layoutweburl.setVisibility(View.VISIBLE);
                        dialogaddurl.dismiss();
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