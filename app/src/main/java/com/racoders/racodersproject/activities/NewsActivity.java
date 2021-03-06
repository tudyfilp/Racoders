package com.racoders.racodersproject.activities;

import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.racoders.racodersproject.AppGlideModule.GlideApp;
import com.racoders.racodersproject.R;
import com.racoders.racodersproject.classes.News;
import com.racoders.racodersproject.fragments.AdminNewsFragment;
import com.racoders.racodersproject.fragments.AdminProfileFragment;

import java.util.ArrayList;

public class NewsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView title;
    private TextView viewsNumber;
    private TextView description;
    private ImageView newsImage;
    private int position;
    private News news;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        toolbar = findViewById(R.id.Toolbar);
        viewsNumber = findViewById(R.id.viewsNumber);
        description = findViewById(R.id.newsDescription);
        title = findViewById(R.id.newsTitle);
        newsImage = findViewById(R.id.newsImage);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        position = getIntent().getIntExtra("position", 0);
        final String reference = getIntent().getStringExtra("reference");
        final String admin = getIntent().getStringExtra("admin");

        if(admin != null && admin.equals("admin")){
            toolbar.setBackgroundColor(getResources().getColor(R.color.LightBlue));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.LightBlueText));
                title.setTextColor(getResources().getColor(R.color.LightBlue));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        }

        DatabaseReference dbref = FirebaseDatabase.getInstance().getReferenceFromUrl(reference);
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    news = dataSnapshot.getValue(News.class);
                    toolbar.setTitle("News");
                    viewsNumber.setText(news.getViewsNumber()+1 +" views");
                    title.setText(news.getTitle());
                    /**
                     * use hmtl preformatted text
                     */

                    Spanned sp = Html.fromHtml(news.getDescription());
                    description.setText(sp);
                    /**
                     * download and load image from storage into the imageView
                     */
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" +
                            news.getId() + ".jpeg");
                    GlideApp.with(getApplicationContext()).load(storageRef).into(newsImage);

                    news.increaseViewsNumber();
                    DatabaseReference saveViewsDbRef = FirebaseDatabase.getInstance().getReferenceFromUrl(reference)
                            .child("viewsNumber");
                    saveViewsDbRef.setValue(news.getViewsNumber(), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError != null){
                                finish();
                            }
                        }
                    });

                }else{
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        else if(item.getItemId() == R.id.delete_news)
            attemptToDeleteNews();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        final Menu myMenu = menu;

        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else
                    getMenuInflater().inflate(R.menu.menu_admin_news, myMenu);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        return true;
    }

    public void attemptToDeleteNews(){

        if(news != null){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Are you sure? This action cannot be reverted");
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseDatabase.getInstance().getReferenceFromUrl(news.getReference()).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError==null){

                                AdminNewsFragment.getmList().remove(position);
                                ArrayList<News> newArray = new ArrayList<>(AdminNewsFragment.getmList());
                                AdminNewsFragment.setAdapter(newArray);
                                AdminProfileFragment.getPostsNumber().setText(Integer.toString(newArray.size()));
                                finish();
                            }
                        }
                    });
                    StorageReference delPhoto = FirebaseStorage.getInstance().getReference().child("images/" + news.getId() + ".jpeg");
                    delPhoto.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NewsActivity.this, "Image could not be deleted", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });

            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }else {
            Toast.makeText(this, "News hasn'y yet loaded", Toast.LENGTH_SHORT).show();
        }
    }


}
