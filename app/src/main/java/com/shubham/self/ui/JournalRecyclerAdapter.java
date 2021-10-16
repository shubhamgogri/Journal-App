package com.shubham.self.ui;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shubham.self.PostJournal;
import com.shubham.self.R;
import com.shubham.self.UpdateJournalActivity;
import com.shubham.self.model.Journal;
import com.shubham.self.util.JournalApi;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<Journal> journalList;
    LayoutInflater inflater;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;


    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.journal_row, viewGroup, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerAdapter.ViewHolder viewHolder, int position) {

        Journal journal = journalList.get(position);
        String imageUrl;

        viewHolder.title.setText(journal.getTitle());
        viewHolder.thoughts.setText(journal.getThought());
        viewHolder.name.setText(journal.getUserName());
        imageUrl = journal.getImageUrl();
        //1 hour ago..
        //Source: https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal
                .getTimeAdded()
                .getSeconds() * 1000);
        viewHolder.dateAdded.setText(timeAgo);

        /*
         Use Picasso library to download and show image
         */

//        Bitmap bitmap = viewHolder.loadImage( imageUrl);

//        if (bitmap == null){
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.image_three)
                .fit()
                .into(viewHolder.image)
                ;
//        }else {
//            viewHolder.image.setImageBitmap(bitmap);
//        }


    }



    @Override
    public int getItemCount() {
        return journalList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView
                title,
                thoughts,
                dateAdded,
                name;
        public ImageView image;
        public ImageButton shareButton;
        String userId;
        String username;
        Journal journal;
        EditText description_et;
        EditText title_et;
        ImageView imageView;
        TextView username_tv;
        Button close;
        Button update;
        Button delete;
        TextView Date_tv;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;

            title = itemView.findViewById(R.id.journal_title_list);
            thoughts = itemView.findViewById(R.id.journal_thought_list);
            dateAdded = itemView.findViewById(R.id.journal_timestamp_list);
            image = itemView.findViewById(R.id.journal_image_list);
            name = itemView.findViewById(R.id.journal_row_username);

            shareButton = itemView.findViewById(R.id.journal_row_share_button);


            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position;
                    position = getAdapterPosition();
                    journal = journalList.get(position);

                    Picasso.get().load(journal.getImageUrl())
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                     = Bitmap.createBitmap(bitmap);
                                    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),bitmap,"imagae", null);
                                    Log.d("ShareButton", "onBitmapLoaded: " + path);
                                    //image_fromurl = Bitmap.createScaledBitmap(bitmap,1280,720,false);
                                    String picture_text = "Title: " + journal.getTitle() +".\n" +
                                            "Thought: " + journal.getThought() + ".";
                                    Intent shareIntent = new Intent();
                                    shareIntent.setAction(Intent.ACTION_SEND);
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, picture_text);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                                    shareIntent.setType("image/jpeg");
                                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    context.startActivity(shareIntent);
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JournalApi.getInstance().setJ(journalList.get(getAdapterPosition()));
                    Intent intent = new Intent(context, UpdateJournalActivity.class);
                    context.startActivity(intent);
//                    alertDialog();
                }
            });

        }

//        private void alertDialog() {
//            int position = getAdapterPosition();
//            journal = journalList.get(position);
//
//            builder = new AlertDialog.Builder(context);
//            inflater = LayoutInflater.from(context);
//            View view = inflater.inflate(R.layout.alert_daillog_show_item, null);
//
//
//            description_et = view .findViewById(R.id.alert_dailog_description_et);
//            title_et = view.findViewById(R.id.alertTitle);
//            imageView = view.findViewById(R.id.alert_dailog_imageView);
//            username_tv = view.findViewById(R.id.alert_username);
//            close = view.findViewById(R.id.alertClose);
//            update = view.findViewById(R.id.alert_update);
//            delete = view.findViewById(R.id.alert_delete);
//            Date_tv = view.findViewById(R.id.alert_date);
//
//            username_tv.setText(JournalApi.getInstance().getUsername());
//            description_et.setText(journal.getThought());
//            title_et.setText(journal.getTitle());
//
//            Picasso.get()
//                    .load(journal.getImageUrl())
//                    .placeholder(R.drawable.image_three)
//                    .fit()
//                    .into(imageView)
//            ;
//
//            Timestamp time = journal.getTimeAdded();
//            Date date = time.toDate();
//            Date_tv.setText(date.toString());
//
//            builder.setView(view);
//            alertDialog = builder.create();
//            alertDialog.show();
//
//            imageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                    galleryIntent.setType("image/*");
//                    context.startActivityForResult(galleryIntent, GALLERY_CODE);
//
//                }
//            });
//            update.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    saveJournal(view);
//                    Log.d("PostJournalActivity", "onClick: updateOnclick");
//                }
//            });
//
//            close.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    alertDialog.dismiss();
//                }
//            });
//        }


    }
}

