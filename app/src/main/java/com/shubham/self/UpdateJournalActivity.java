package com.shubham.self;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.EmotionOptions;
import com.ibm.watson.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;
import com.ibm.watson.natural_language_understanding.v1.model.SentimentOptions;
import com.ibm.watson.natural_language_understanding.v1.model.SyntaxOptions;
import com.ibm.watson.natural_language_understanding.v1.model.SyntaxOptionsTokens;
import com.ibm.watson.natural_language_understanding.v1.model.TokenResult;
import com.shubham.self.model.Journal;
import com.shubham.self.model.ThoughtAnalysisModel;
import com.shubham.self.util.JournalApi;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateJournalActivity extends AppCompatActivity {

    private IamAuthenticator authenticator = new IamAuthenticator("FvOQ_IgxmQ3Sc2rcTK_iKWQ2fLUrENZ5JZbjDv0grlCE");
    private NaturalLanguageUnderstanding naturalLanguageUnderstanding =  new
            NaturalLanguageUnderstanding("2021-08-01", authenticator);
    private List<String> targets = new ArrayList<>();
    private Journal journal;
    private TextView description_et;
    private TextView title_et;
    private ImageView imageView;
    private TextView username_tv;
    private Button close;
    private TextView Date_tv;
    private static final String TAG = "UpdateJournalActivity";

    private ProgressBar progressBar;
    private TextView sentiment_text, emotion_text, entity_text;
    private ListView list_partsOfSpeech;
//    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //    CollectionReference collectionReference = db.collection("Journal/").
//            document(JournalApi.getInstance().getUsername())
//            .collection("docs");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_journal);

        naturalLanguageUnderstanding.setServiceUrl(getString(R.string.text_classification_url));

        journal = JournalApi.getInstance().getJ();
        Log.d(TAG, "onCreate: "+ journal.getTitle());


        Date_tv = findViewById(R.id.update_date);
        description_et = findViewById(R.id.update_description_et);
        title_et = findViewById(R.id.updateTitle);
        imageView = findViewById(R.id.update_dailog_imageView);
        username_tv = findViewById(R.id.update_username);
        close = findViewById(R.id.updateClose);
        sentiment_text = findViewById(R.id.sentiment_text);
        emotion_text = findViewById(R.id.emotion_text);
        entity_text = findViewById(R.id.entity_text);
        list_partsOfSpeech = findViewById(R.id.list_partsOfSpeech);

        ClassifyText classifyText = new ClassifyText();
        classifyText.execute(journal.getThought());

        String str[] = journal.getThought().split(" ");
        targets.addAll(Arrays.asList(str));



        username_tv.setText(JournalApi.getInstance().getUsername());
        description_et.setText(journal.getThought());
        title_et.setText(journal.getTitle());


        Picasso.get()
                .load(journal.getImageUrl())
                .placeholder(R.drawable.image_three)
                .fit()
                .into(imageView)
        ;

        Timestamp time = journal.getTimeAdded();
        Date date = time.toDate();
        Date_tv.setText(date.toString());

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                go back to activity
                Intent intent = new Intent(UpdateJournalActivity.this, JournalList.class);
                startActivity(intent);
                finish();
            }
        });


    }

    private class ClassifyText extends android.os.AsyncTask<String, String, ThoughtAnalysisModel>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = findViewById(R.id.update_progressbar);
            progressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected ThoughtAnalysisModel doInBackground(String... strings) {
            ThoughtAnalysisModel thoughtAnalysisModel;
            EntitiesOptions entitiesOptions;
            SentimentOptions sentiment;
            EmotionOptions emotions;
            SyntaxOptions syntax;
            Features features;

            entitiesOptions = new EntitiesOptions.Builder()
                    .sentiment(true)
                    .limit(1L)
                    .build();

            sentiment = new SentimentOptions.Builder()
                    .targets(targets)
                    .build();

            emotions = new EmotionOptions.Builder()
                    .targets(targets)
                    .build();
            syntax = new SyntaxOptions.Builder()
                    .sentences(true)
                    .tokens(new SyntaxOptionsTokens.Builder()
                            .partOfSpeech(true)
                            .build()
                    )
                    .build();
            features = new Features.Builder()
                    .entities(entitiesOptions)
                    .sentiment(sentiment)
                    .emotion(emotions)
                    .syntax(syntax)
                    .build();

            AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                    .text(strings[0])
                    .features(features)
                    .build();

            AnalysisResults result = UpdateJournalActivity.this.naturalLanguageUnderstanding
                    .analyze(parameters)
                    .execute()
                    .getResult();

            String entity_string = "";
            if (!result.getEntities().isEmpty()){
                entity_string = entity_string + "Entities: \nText: ";
                entity_string = entity_string + result.getEntities().get(0).getText();
                entity_string = entity_string + "\nType: " + result.getEntities().get(0).getType();
            }else{
                entity_string  = "No Entities in this Sentence.";
            }

            List<TokenResult> tokens = result.getSyntax().getTokens();
            List<String> partsofspeech;
            partsofspeech = new ArrayList<>();

            for (TokenResult speech : tokens) {
                partsofspeech.add(speech.getPartOfSpeech() + ": " + speech.getText() + ".\n");
            }
            Collections.sort(partsofspeech);
            thoughtAnalysisModel = new ThoughtAnalysisModel("Sentiment: "+ result.getSentiment().getDocument().getLabel(),
                    getEmotion(result), partsofspeech, entity_string);

            Log.d("TAG", "run: Sentiment " + thoughtAnalysisModel.getSentiment());
            Log.d("TAG", "run: Entities " + thoughtAnalysisModel.getEntity());
            Log.d("TAG", "run: Emotion " + thoughtAnalysisModel.getEmotion());
            Log.d("TAG", "run: Parts of Speech " + thoughtAnalysisModel.getParts_of_speech());

            Log.d("TAG", "run: " + result.getEntities());
            Log.d("TAG", "run: " + result.getSentiment().getDocument().getLabel());
            Log.d("TAG", "run: " + result.getSyntax());
            Log.d("TAG", "run: " + result.getEmotion().getDocument().getEmotion());


            return thoughtAnalysisModel;
        }

        @Override
        protected void onPostExecute(ThoughtAnalysisModel thoughtAnalysisModel) {
            super.onPostExecute(thoughtAnalysisModel);

            progressBar.setVisibility(View.INVISIBLE);
            assert thoughtAnalysisModel != null;
            ArrayAdapter<String> arr = new ArrayAdapter<String>(UpdateJournalActivity.this,
                    R.layout.support_simple_spinner_dropdown_item,
                    thoughtAnalysisModel.getParts_of_speech());
            list_partsOfSpeech.setAdapter(arr);
            emotion_text.setText(thoughtAnalysisModel.getEmotion());
            entity_text.setText(thoughtAnalysisModel.getEntity());
            sentiment_text.setText(thoughtAnalysisModel.getSentiment());


        }
        private String getEmotion(AnalysisResults result) {

            Double anger = result.getEmotion().getDocument().getEmotion().getAnger();
            Double disgust = result.getEmotion().getDocument().getEmotion().getDisgust();
            Double fear = result.getEmotion().getDocument().getEmotion().getFear();
            Double joy = result.getEmotion().getDocument().getEmotion().getJoy();
            Double sadness = result.getEmotion().getDocument().getEmotion().getSadness();

            HashMap<String, Double> max = new HashMap<>();
            max.put("Anger", anger);
            max.put("Disgust", disgust);
            max.put("Fear", fear);
            max.put("Joy", joy);
            max.put("Sadness", sadness);

            Map.Entry<String, Double> maxEntry = Collections.max(max.entrySet(), new Comparator<Map.Entry<String, Double>>() {
                @Override
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
            return "Emotion: " + maxEntry.getKey();
        }
    }

//    private void deleteJournal() {
//
//        collectionReference.whereEqualTo("title",journal.getTitle())
//                .whereEqualTo("userId",JournalApi.getInstance().getUserId())
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        assert queryDocumentSnapshots != null;
//                        if(!queryDocumentSnapshots.isEmpty()){
//                            List<DocumentReference> documentReference = new ArrayList<>();
//
//                            for (QueryDocumentSnapshot q: queryDocumentSnapshots) {
//                                documentReference.add(q.getDocumentReference(JournalApi.getInstance().getUserId()));
//                            }
//
//                            if (!documentReference.isEmpty()){
//                                for (DocumentReference d:documentReference) {
//                                    d.delete()
//                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    Log.d(TAG, "onSuccess: File deleted in cloud");
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Log.d(TAG, "onSuccess: File deletion failed in cloud. "+ e.getMessage());
//                                                }
//                                            });
//                                    deleteImage();
//                                }
//                            }else{
//                                Log.d(TAG, "onSuccess: document not found.");
//                            }
//                        }else {
//                            Log.d(TAG, "onEvent: Snapshots Query Failed. ");
//                        }
//
//                    }
//                })
//        .addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d(TAG, "onFailure: Failed to Query" + e.getMessage());
//            }
//        })
//                ;
//    }
//
//    private void deleteImage() {
//        FirebaseStorage storage = FirebaseStorage.getInstance("gs://self-33c07.appspot.com/");
//        storageReference = storage.getReference();
////            gs://self-33c07.appspot.com/journal_images/please/Image1633943666
//
//        StorageReference deletepath = storageReference
//                .child(JournalApi.getInstance().getUsername() + "/" + journal.getImage_name());
////                    .child(JournalApi.getInstance().getUsername())
////                    .child("/Image" + journal.getTimeAdded());
//
////            gs://self-33c07.appspot.com/journal_images/please/Image1633898247
//        deletepath
//                .delete()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "onSuccess: Image File Deleted.");
//                        Intent intent = new Intent(UpdateJournalActivity.this, JournalList.class);
////                        JournalApi.getInstance().setJ(null);
//                        startActivity(intent);
//                        finish();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "onSuccess: Image File Deletion failed." + e.getMessage());
//                    }
//                });
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
//            if (data != null) {
//                imageUri = data.getData(); // we have the actual path to the image
//                imageView.setImageURI(imageUri);//show image
//                updateImage(imageUri);
//                update.setEnabled(false);
//            }
//        }
//    }
//
//    private void updateImage(Uri imageUri) {
//            progressBar.setVisibility(View.VISIBLE);
//        if (imageUri!=null) {
//            FirebaseStorage storage = FirebaseStorage.getInstance("gs://self-33c07.appspot.com/");
//            storageReference = storage.getReference();
////            gs://self-33c07.appspot.com/journal_images/please/Image1633943666
//
//            StorageReference deletepreviouspath = storageReference
//                    .child(JournalApi.getInstance().getUsername()+"/"+ journal.getImage_name());
////                    .child(JournalApi.getInstance().getUsername())
////                    .child("/Image" + journal.getTimeAdded());
//
////            gs://self-33c07.appspot.com/journal_images/please/Image1633898247
//            deletepreviouspath
//                    .delete()
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.d(TAG, "onSuccess: File Deleted." );
//                            final StorageReference filepath = storageReference //.../journal_images/our_image.jpeg
////                                    .child("journal_images")
//                                    .child(JournalApi.getInstance().getUsername())
//                                    .child("Image" + Timestamp.now().getSeconds()); // my_image_887474737
//
//                            filepath.putFile(imageUri)
//                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                        @Override
//                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                                @Override
//                                                public void onSuccess(Uri uri) {
//                                                    Log.d(TAG, "onSuccess: New filepath. " + uri);
//                                                    journal.setImageUrl(uri.toString());
//                                                    String image_name = "Image" + Timestamp.now().getSeconds();
//                                                    journal.setImage_name(image_name);
//
//                                                    saveJournal();
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Log.d(TAG, "onFailure: Failed to get uri. " + e.getMessage());
//                                                }
//                                            });
//
//                                        }
//                                    })
//                                    .addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Toast.makeText(UpdateJournalActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
//                                            Log.d(TAG, "onFailure: Failed to upload image. " + e.getMessage());
//                                        }
//                                    })
//                            ;
//
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.d(TAG, "onFailure: Failed to delete." + e.getMessage());
//                        }
//                    });
//
//
//
//        }else{
//            Toast.makeText(this, "Image can't be empty.", Toast.LENGTH_SHORT).show();
//        }
//
//
//    }
//
//    private void saveJournal() {
//        progressBar.setVisibility(View.VISIBLE);
//        final String title = title_et.getText().toString().trim();
//        final String thoughts = description_et.getText().toString().trim();
//
////            progressBar.setVisibility(View.VISIBLE);
//
//        if (!TextUtils.isEmpty(title) &&
//                !TextUtils.isEmpty(thoughts)) {
//
//            //Todo: create a Journal Object - model
//
////            DocumentReference documentReference = db.collection("Journal/").
////                    document(JournalApi.getInstance().getUsername())
////                    .collection("docs")
////                    .document(JournalApi.getInstance().getUsername() + "_" + journal.getTitle());
//
//            DocumentReference documentReference = db.collection(JournalApi.getInstance().getUsername()+"/")
//                    .document(JournalApi.getInstance().getUsername() + "_" + journal.getTitle());
//
//            collectionReference.whereEqualTo("title",journal.getTitle())
//                    .whereEqualTo("userId",JournalApi.getInstance().getUserId())
//                    .get()
//                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                        @Override
//                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                            assert queryDocumentSnapshots != null;
//                            if(!queryDocumentSnapshots.isEmpty()){
//                                List<DocumentReference> documentReference = new ArrayList<>();
//                                journal.setTitle(title);
//                                journal.setThought(thoughts);
//                                journal.setTimeAdded(new Timestamp(new Date()));
//
//                                Map<String, Object> upd= new HashMap<>();
//                                upd.put("title", journal.getTitle());
//                                upd.put("thought", journal.getThought());
//                                upd.put("timeAdded", journal.getTimeAdded());
//                                upd.put("imageUrl", journal.getImageUrl());
//                                upd.put("image_name", journal.getImage_name());
//
//                                for (QueryDocumentSnapshot q: queryDocumentSnapshots) {
//                                    documentReference.add(q.getDocumentReference(JournalApi.getInstance().getUserId()));
//                                }
//
//                                if (!documentReference.isEmpty()){
//                                    for (DocumentReference d:documentReference) {
//                                            d
//                                            .update(upd)
//                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    Log.d(TAG, "onSuccess: updated"  );
//                                                    startActivity(new Intent(UpdateJournalActivity.this,
//                                                            JournalList.class));
//                                                    finish();
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Log.d(TAG, "onFailure: Update Failed" + e.getMessage());
//                                                    progressBar.setVisibility(View.INVISIBLE);
//
//                                                }
//                                            });
//
//                                    }
//                                }else{
//                                    Log.d(TAG, "onSuccess: document not found.");
//                                }
//                            }else {
//                                Log.d(TAG, "onEvent: Snapshots Query Failed. ");
//                            }
//
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.d(TAG, "onEvent: Update Failed . " +e.getMessage() );
//                        }
//                    });
//
//            progressBar.setVisibility(View.INVISIBLE);
//
//        }
//    }

}