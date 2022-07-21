package com.dldmswo1209.tinder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.dldmswo1209.tinder.databinding.ActivityLikeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LikeActivity : AppCompatActivity() {
    var mBinding : ActivityLikeBinding? = null
    val binding get() = mBinding!!
    private var auth : FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userDB: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLikeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDB = Firebase.database.reference.child("Users")
        val currentUserDB = userDB.child(auth.currentUser!!.uid)
        currentUserDB.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // 처음에는 데이터가 존재하면 onDataChange 에 들어오게 된다
                // 데이터가 수정 되었을 때도 들어온다.
                if(snapshot.child("name").value == null){ // name 정보가 없으면
                    showNameInputPopup() // 팝업을 띄움
                    return
                }
                // 유저정보를 갱신

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun showNameInputPopup(){
        val editText = EditText(this) // EditText 생성
        AlertDialog.Builder(this) // AlertDialog 생성
            .setTitle("이름을 입력해주세요") // title 설정
            .setView(editText) // View 설정
            .setPositiveButton("저장"){_,_ -> // 저장 버튼
                if(editText.text.isEmpty()){ // editText 가 비어있으면
                    showNameInputPopup() // 다시 팝업을 띄움
                }else{
                    saveUserName(editText.text.toString()) // 이름 저장 메소드 호출
                }
            }
            .setCancelable(false) // 뒤로가기 비활성화
            .show()
    }
    private fun saveUserName(name: String){
        val userId = getCurrentUserID() // 현재 유저의 uid 불러오기
        val currentUserDB = userDB.child(userId)
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId
        user["name"] = name
        currentUserDB.updateChildren(user) // 이름 정보가 추가된 user 를 DB에 업데이트 -> Realtime Database 에 추가
    }
    private fun getCurrentUserID(): String{
        if(auth.currentUser == null){
            Toast.makeText(this, "로그인이 되어있지 않습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
        return auth.currentUser?.uid.orEmpty()

    }
}