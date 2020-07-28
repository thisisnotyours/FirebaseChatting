package com.suek.ex85firebasechatting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView civProfile;
    EditText etName;

    //프로필 이미지 Uri 참조변수
    Uri imgUri;    //사진을 선택해야만 uri 가 생기니까 참조변수만..

    boolean isChanged= false;  //데이터의 변경이 있었는가?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        civProfile= findViewById(R.id.civ_profile);
        etName= findViewById(R.id.et_name);

        //이미 저장되어있는 정보들 읽어오기
        loadData();
        if(G.nickName != null){    //저장된게 있을때
            etName.setText(G.nickName);
            Glide.with(this).load(G.profileUri).into(civProfile);
        }
    }

    //사진변경
    public void clickImage(View view) {
        Intent intent= new Intent(Intent.ACTION_PICK);  //내 앱이 아니라, '갤러리 앱'이 외부라이브러리를 사용해서 따로 외부라이브러리 사용안함
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==100 && resultCode==RESULT_OK){   //사진선택한 인텐트100, 사진선택 오케이
            imgUri= data.getData();     //data= 돌아온 인텐트
            if(imgUri!=null){
                Glide.with(this).load(imgUri).into(civProfile);

                //프로필 이미지를 변경했다고 표식
                isChanged= true;
            }

        }
    }






    //데이터를 저장하는 메소드
    void saveData(){
        //바꾼 이미지와 채팅명을 다른사람 핸드폰에서 보여줄 수 있도록 서버에 업로드 작업을해야함
        //->> Firebase DB 에 저장

        G.nickName= etName.getText().toString();


        // 1. 이미지 파일부터 Firebase Storage 에 업로드
        // 1) 먼저 업로드할 파일명이 같으면 안되므로(덮어쓰기됨) 날짜를 이용해서 파일명 지정
        SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMddmmss");
        String fileNAme= sdf.format(new Date()) + ".png";    //날짜명.png 로 저장됨(중복된데이터 x)

        FirebaseStorage firebaseStorage= FirebaseStorage.getInstance();   //FirebaseStorage 데이터 '관리자'객체
        final StorageReference imgRef= firebaseStorage.getReference("profileImages/" + fileNAme); //StorageReference imgRef= 파일명 참조변수    //여기까지는 파일명만 만들어짐

        //파일명 참조변수에 이미지 업로드
        UploadTask task = imgRef.putFile(imgUri);   //이미지 데이터..   //Task<RESULT> =결과를 받는 쓰레드
        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //firebase 실시간 DB 에 저장할 업로드된 실제 인터넷경로 =즉, URL 알아내기
                imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {  //이중리스너 //imgRef.getDownloadUrl().addOnSuccessListener
                                                                                                         //---> (리턴 to Task- imgRef 에 명령을 시킨게 아니라 Task 가 그 작업을 함)
                    @Override
                    public void onSuccess(Uri uri) {     //Uri uri --> 다운로드 Url

                        //다운로드 URL 을 G.profileUri 에 저장
                        G.profileUri= uri.toString();

                        //firebase 에 저장된 이미지 파일의 경로를 fire DB에 저장하고(다른사람 핸드폰에서 볼수있게) 내 디바이스에도 저장(내 폰에 저장)

                        // 1) firebase DB에 저장 => [ G.nickName, imgUri 의 다운로드 URL(이미지의 진짜주소) ]
                        FirebaseDatabase firebaseDatabase= FirebaseDatabase.getInstance();    ////FirebaseDatabase 데이터 '관리자'객체
                        //"profiles"라는 이름의 자식노드 참조객체
                        DatabaseReference profileRef= firebaseDatabase.getReference("profiles");    //노드를 제어하는건 Reference ***
                        //nickName 을 키값으로 지정한 노드에 이미지 Url 을 값을 지정
                        profileRef.child(G.nickName).setValue(G.profileUri);   //profileUri- 서버에 업로드된 http 주소


                        // 2) 내 디바이스에 저장- SharedPReference(key, value 를 저장하는 놈) 이용하여 저장 => [ G.nickName, G.profileUri ]
                        SharedPreferences pref= getSharedPreferences("account", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();

                        editor.putString("nickName", G.nickName);
                        editor.putString("profileUrl", G.profileUri);

                        editor.commit();

                        Toast.makeText(MainActivity.this, "저장완료", Toast.LENGTH_SHORT).show();

                        //모든저장이 완료되었으므로
                        // 채팅화면으로 이동
                        Intent intent= new Intent(MainActivity.this, ChattingActivity.class);
                        startActivity(intent);
                        finish();

                    }
                });
            }
        });

    }


    //입장버튼을 누르면 채팅 액티비티로 이동
    public void clickBtn(View view) {

        //데이터의 변경이 있었는가?
        if( isChanged ) saveData();
        else {
            Intent intent= new Intent(this, ChattingActivity.class);
            startActivity(intent);
            finish();
        }

    }


    //디바이스에 저장된 정보 읽어오기
    void loadData(){

        SharedPreferences pref= getSharedPreferences("account", MODE_PRIVATE);
        G.nickName= pref.getString("nickName", null);   //저장된 닉네임이 없을때
        G.profileUri= pref.getString("profileUrl", null);  //저장된 프로필사진이 없을때

        Toast.makeText(this, G.nickName, Toast.LENGTH_SHORT).show();

    }
}
