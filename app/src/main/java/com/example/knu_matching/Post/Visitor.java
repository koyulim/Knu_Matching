package com.example.knu_matching.Post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.knu_matching.GetSet.CommentItem;
import com.example.knu_matching.GetSet.Post;
import com.example.knu_matching.MainActivity;
import com.example.knu_matching.R;
import com.example.knu_matching.SendNotification;
import com.example.knu_matching.UserAccount;
import com.example.knu_matching.membermanage.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Visitor extends AppCompatActivity {

    Toolbar toolbar;
    Button btn_participate, btn_participate_cancel;
    ImageButton btn_comment;
    TextView tv_count, tv_total, tv_StartDate, tv_EndDate, tv_file, tv_content, tv_title, tv_link;
    RecyclerView rv_comment;
    CommentAdapter commentAdapter = null;
    EditText edt_comment;
    String str_participate_Nickname, str_participate_Major, str_participate_StudentId, str_participate_EmailId, str_participate_Uid;
    String str_owner_uid, str_title, str_Comment_uid, str_count, str_total, str_StartDate, str_EndDate, str_filename, str_content, str_comment, str_email, str_Id, str_time, str_application, str_link, str_uid;
    public String str_Current_Email;
    Intent intent;
    ArrayList<CommentItem> comment_list;
    Context context = this;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    int count = 0;

    LocalDateTime now = LocalDateTime.now();
    String formatedNow = now.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS"));



    // Firebase
    FirebaseAuth mFirebaseAuth=FirebaseAuth.getInstance();
    DatabaseReference mDatabaseRef;
    FirebaseUser user;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    static final Map<String, String> comment_notice= new HashMap<String,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor);
        //TODO: ???????????? ????????????

        init();     // ?????? ????????? ??????
        // ArrayList ?????????
        comment_list = new ArrayList<>();

        // Adapter ?????????
        commentAdapter = new CommentAdapter(comment_list);
        rv_comment.setAdapter(commentAdapter);
        rv_comment.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        tv_title.setText(str_title);            // ??????
        tv_total.setText(str_total);            // ?????? ??????
        tv_StartDate.setText(str_StartDate);    // ?????? ????????????
        tv_EndDate.setText(str_EndDate);        // ?????? ???????????????
        tv_content.setText(str_content);        // ??????
        tv_file.setText(str_application);       // ????????????
        tv_link.setText(str_link);              // ??????

        btn_participate_cancel.setEnabled(false);

        btn_participate_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("Post").document(str_Id).collection("Participate").document(auth.getCurrentUser().getEmail().replace(".", ">")).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "?????????", Toast.LENGTH_SHORT).show();
                            count --;
                            tv_count.setText(count + "");
                            btn_participate_cancel.setEnabled(false);
                            btn_participate.setEnabled(true);
                            Post post = new Post();
                            post.getStr_Id();
                            db.collection("Participate").document(mFirebaseAuth.getCurrentUser().getEmail().replace(".",">")).collection("participate").document(str_Id)
                                    .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent intent = new Intent();
                                        setResult(RESULT_OK, intent);
                                        System.out.println("?????????"+intent.getClass());
                                        finish();
                                    }
                                }
                            });
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "????????? ?????????????????????.\n??????????????? ??????????????? ????????????.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edt_comment.getText().toString().equals("")){
                }
                else{
                    long systemtime = System.currentTimeMillis();
                    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.KOREA);
                    String realtime = timeFormat.format(systemtime);
                    CommentItem commentItem = new CommentItem();
                    commentItem.setStr_Email(auth.getCurrentUser().getEmail());
                    commentItem.setStr_Date(realtime);
                    commentItem.setStr_NickName(((MainActivity) MainActivity.context).strNick);
                    commentItem.setStr_Content(edt_comment.getText().toString());
                    commentItem.setStr_Uid(auth.getCurrentUser().getUid());
                    commentItem.setStr_Post_uid(str_Id);
                    edt_comment.setText("");
                    db.collection("Post").document(str_Id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            Post post = task.getResult().toObject(Post.class);
                            str_owner_uid = post.getStr_uid();
                            System.out.println("????????? uid1 "+str_owner_uid);
                            FirebaseDatabase.getInstance().getReference().child("users").child(str_owner_uid).get()
                                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            UserAccount userAccount = task.getResult().getValue(UserAccount.class);
                                            System.out.println("?????? ??? 2  "+userAccount.getToken());
                                            comment_notice.put(userAccount.getToken(),str_owner_uid);
                                            System.out.println("?????? "+comment_notice.keySet());
                                        }
                                    });

                        }
                    });

                    db.collection("Post").document(str_Id).collection("Comment").get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                        System.out.println("task qwer " + documentSnapshot.getId());

                                        db.collection("Post").document(str_Id).collection("Comment")
                                                .document(documentSnapshot.getId())
                                                .update("str_Comment_uid", documentSnapshot.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(getApplicationContext(), "?????? ???????????????.", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "???????????? ??? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            });

                    db.collection("Post").document(str_Id).collection("Comment").add(commentItem)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    Toast.makeText(Visitor.this, "??????", Toast.LENGTH_SHORT).show();
                                    FirebaseDatabase.getInstance().getReference().child("users").equalTo(str_uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                System.out.println("?????? ???????");


                                                db.collection("Post").document(str_Id).collection("Comment").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        System.out.println("?????? ???????2");
                                                        if (task.isSuccessful()) {
                                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                                Log.d("Visitor", document.getId() + " => " + document.getData());
                                                                String uid;
                                                                uid = document.getData().get("str_Uid").toString();
                                                                System.out.println("uid ??????" + uid);

                                                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).get()
                                                                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                                System.out.println("?????? ??? "+task.getResult());
                                                                                UserAccount userAccount = task.getResult().getValue(UserAccount.class);
                                                                                System.out.println("?????? ??? 2  "+userAccount.getToken());
                                                                                comment_notice.put(userAccount.getToken(),uid);
                                                                                System.out.println("?????? "+comment_notice.keySet());

                                                                            }
                                                                        });
                                                                //comment_notice.put(userAccount.getNickName(), userAccount.getUid());

                                                            }
                                                        } else {
                                                            Log.d("Visitor", "Error getting documents: ", task.getException());
                                                        }

                                                    }
                                                });
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            System.out.println("?????? ???????");
                                        }
                                    });
                                    for(String key : comment_notice.keySet()) {
                                        String value = comment_notice.get(key);
                                        System.out.println("comment_notice key "+key);
                                        System.out.println("comment_notice value "+value);
                                        SendNotification.sendNotification(key, "????????? ???????????????!", str_title);
                                        System.out.println("comment_notice value enddddd");
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Visitor.this, "??????", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        });

        // ?????? DB ??????????????? ????????????
        db.collection("Post").document(str_Id).collection("Comment").orderBy("str_Date", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            System.out.println("Error: Post_Owner_Activity - Comment ???????????? ??????");
                            return;
                        }
                        for(DocumentChange doc: value.getDocumentChanges()){
                            switch (doc.getType()){
                                case ADDED:
                                    System.out.println("?????? ?????????"+doc.getDocument().getString("str_Email"));

                                    addItem(doc.getDocument().getString("str_Email"),doc.getDocument().getString("str_NickName"),doc.getDocument().getString("str_Content"),doc.getDocument().getString("str_Date"), doc.getDocument().getString("str_Uid"));
                                    commentAdapter.notifyDataSetChanged();
                                    break;
                                case MODIFIED:
                                    // ?????????????????? ??????
                                    break;
                                case REMOVED:
                                    // ?????????????????? ??????
                                    delItem(doc.getDocument().getString("str_Email"),doc.getDocument().getString("str_NickName"),doc.getDocument().getString("str_Content"),doc.getDocument().getString("str_Date"), doc.getDocument().getString("str_Uid"));
                                    commentAdapter.notifyDataSetChanged();
                                    break;
                            }
                        }
                    }
                });

        tv_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storageRef.child(str_title + "/" + str_application).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        new ImageDownload().execute(uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                });
            }
        });

        btn_participate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseAuth = FirebaseAuth.getInstance();
                mDatabaseRef = FirebaseDatabase.getInstance().getReference("Knu_Matching");
                if (mFirebaseAuth.getCurrentUser().getEmail().equals(str_email) == false) {
                    count ++;
                    tv_count.setText(count + "");
                    btn_participate.setEnabled(false);
                    btn_participate.setText("?????? ??????");
                    btn_participate_cancel.setEnabled(true);

                    user = FirebaseAuth.getInstance().getCurrentUser();
                    db.collection("Account").document(user.getEmail().replace(".", ">"))
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                UserAccount userAccount = task.getResult().toObject(UserAccount.class);
                                str_participate_Nickname = userAccount.getNickName();
                                str_participate_Major = userAccount.getMajor();
                                str_participate_StudentId = userAccount.getStudentId();
                                str_participate_EmailId = userAccount.getEmailId();
                                str_participate_Uid = userAccount.getUid();
                                ParticipateUser participateUser = new ParticipateUser(str_participate_Nickname, str_participate_Major, str_participate_StudentId, str_participate_EmailId, str_participate_Uid);
                                ParticipateUserSave(participateUser);
                                Intent intent = new Intent();
                                setResult(Activity.RESULT_OK, intent);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Visitor.this, "postActivity ??????", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // ????????? ???????????? ????????? ???????????? ???
                }

                Post post = new Post();
                post.setStr_Id(str_Id);

                db.collection("Participate").document(mFirebaseAuth.getCurrentUser().getEmail().replace(".",">")).collection("participate").document(str_Id).set(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                        }
                    }
                });
            }
        });


        db.collection("Post").document(str_Id).collection("Participate")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                int i = 0;
                int j = 0;
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("Visitor", document.getId() + " => " + document.getData());
                        j = i+1;
                        i++;
                    }
                    count = j;
                    tv_count.setText(count + "");
                } else {
                    Log.d("Visitor", "Error getting documents: ", task.getException());
                }
            }

        });

        db.collection("Post").document(str_Id).collection("Participate")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("Visitor", document.getId() + " => " + document.getData());
                        if (document.getId().equals(auth.getCurrentUser().getEmail().replace(".", ">"))) {
                            btn_participate_cancel.setEnabled(true);
                            btn_participate.setEnabled(false);
                            btn_participate.setText("?????? ??????");
                        }
                    }
                } else {
                    Log.d("Visitor", "Error getting documents: ", task.getException());
                }
            }

        });

    }

    // ?????? ??????
    private void addItem(String Email, String Nickname, String Content, String Date, String Uid){
        CommentItem item = new CommentItem();

        item.setStr_Email(Email);
        item.setStr_NickName(Nickname);
        item.setStr_Content(Content);
        item.setStr_Date(Date);
        item.setStr_Uid(Uid);
        item.setStr_Post_uid(str_Id);
        item.setStr_Comment_uid(str_Comment_uid);
        comment_list.add(item);
    }

    // ?????? ?????? ???????????? ??????
    private void delItem(String Email, String Nickname, String Content, String Date, String Uid){
        CommentItem item = new CommentItem();

        item.setStr_Email(Email);
        item.setStr_NickName(Nickname);
        item.setStr_Content(Content);
        item.setStr_Date(Date);
        item.setStr_Uid(Uid);

        comment_list.remove(item);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        // ?????? ?????? ??????
        toolbar = (Toolbar) findViewById(R.id.toolbar);             //?????? ??????
        setSupportActionBar(toolbar);                               //?????? ??????
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);      //???????????? ?????? ??????
        getSupportActionBar().setDisplayShowTitleEnabled(false);    //?????? ?????? ????????? ??????

        // id ??????
        btn_comment = findViewById(R.id.btn_comment);
        btn_participate = findViewById(R.id.btn_participate);
        tv_title = findViewById(R.id.tv_title);
        tv_count = findViewById(R.id.tv_count);
        tv_total = findViewById(R.id.tv_total);
        tv_StartDate = findViewById(R.id.tv_StartDate);
        tv_EndDate = findViewById(R.id.tv_EndDate);
        tv_file = findViewById(R.id.tv_file);
        tv_content = findViewById(R.id.tv_content);
        rv_comment = findViewById(R.id.rv_comment);
        edt_comment = findViewById(R.id.edt_comment);
        tv_link = findViewById(R.id.tv_link);
        btn_participate_cancel = findViewById(R.id.btn_participate_cancel);

        // intent ??? ????????????
        intent = getIntent();
        str_Id = intent.getStringExtra("Str_Id");
        str_title = intent.getStringExtra("Title");
        str_StartDate = intent.getStringExtra("StartDate");
        str_EndDate = intent.getStringExtra("EndDate");
        str_total = intent.getStringExtra("Number");
        str_content = intent.getStringExtra("Post");
        str_application = intent.getStringExtra("Filename");
        str_time = intent.getStringExtra("Time");
        str_email = intent.getStringExtra("Email");
        str_link = intent.getStringExtra("Link");
        str_uid = intent.getStringExtra("Uid");


        // ?????? ?????? ??????
        str_Current_Email = auth.getCurrentUser().getEmail();
    }

    private void ParticipateUserSave(ParticipateUser participateUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("Post").document(str_Id).collection("Participate")
                .document(auth.getCurrentUser().getEmail().replace(".", ">")).set(participateUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(Visitor.this, "???????????????", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Visitor.this, "??????", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private class ImageDownload extends AsyncTask<String, Void, Void> {

        private String fileName = str_application;

        private final String SAVE_FOLDER = "/save_folder";

        @Override
        protected Void doInBackground(String... params) {

            String savePath = Environment.getExternalStorageDirectory().toString() + SAVE_FOLDER;

            File dir = new File(savePath);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileUrl = params[0];

            if (new File(savePath + "/" + fileName).exists() == false) {
            } else {
            }

            String localPath = savePath + "/" + fileName + ".jpg";

            try {
                URL imgUrl = new URL(fileUrl);
                HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
                int len = conn.getContentLength();
                byte[] tmpByte = new byte[len];
                InputStream is = conn.getInputStream();
                File file = new File(localPath);
                FileOutputStream fos = new FileOutputStream(file);
                int read;
                for (; ; ) {
                    read = is.read(tmpByte);
                    if (read <= 0) {
                        break;
                    }
                    fos.write(tmpByte, 0, read);
                }
                is.close();
                fos.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            String targetDir = Environment.getExternalStorageDirectory().toString() + SAVE_FOLDER;
            File file = new File(targetDir + "/" + fileName + ".jpg");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            Toast.makeText(Visitor.this, "???????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}