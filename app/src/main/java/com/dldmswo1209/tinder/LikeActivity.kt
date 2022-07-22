package com.dldmswo1209.tinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.dldmswo1209.tinder.DBKey.Companion.DIS_LIKE
import com.dldmswo1209.tinder.DBKey.Companion.LIKE
import com.dldmswo1209.tinder.DBKey.Companion.LIKED_BY
import com.dldmswo1209.tinder.DBKey.Companion.MATCH
import com.dldmswo1209.tinder.DBKey.Companion.NAME
import com.dldmswo1209.tinder.DBKey.Companion.USERS
import com.dldmswo1209.tinder.DBKey.Companion.USER_ID
import com.dldmswo1209.tinder.databinding.ActivityLikeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import java.util.jar.Attributes

class LikeActivity : AppCompatActivity(), CardStackListener {
    var mBinding : ActivityLikeBinding? = null
    val binding get() = mBinding!!
    private var auth : FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userDB: DatabaseReference
    private val adapter = CardItemAdapter()
    private val cardItems = mutableListOf<CardItem>()
    private val manager by lazy {
        CardStackLayoutManager(this,this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLikeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDB = Firebase.database.reference.child(USERS)
        val currentUserDB = userDB.child(auth.currentUser!!.uid)
        currentUserDB.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // 처음에는 데이터가 존재하면 onDataChange 에 들어오게 된다
                // 데이터가 수정 되었을 때도 들어온다.
                if(snapshot.child(NAME).value == null){ // name 정보가 없으면
                    showNameInputPopup() // 팝업을 띄움(name 을 설정)
                    return
                }
                // 유저정보를 갱신
                getUnSelectedUsers()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        initCardStackView()
        initSignOutButton()
        initMatchedListButton()
    }
    private fun initCardStackView(){
        binding.cardStackView.layoutManager = manager
        binding.cardStackView.adapter = adapter
    }
    private fun initSignOutButton(){ // 로그아웃 버튼
        binding.signOutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    private fun initMatchedListButton(){ // 매치 리스트 버튼
        binding.matchListButton.setOnClickListener {
            startActivity(Intent(this, MatchedUserActivity::class.java))
        }
    }
    private fun getUnSelectedUsers(){
        // 싫어요나 좋아요를 하지 않은 유저 리스트를 가져와서 cardItems 에 추가하고 adapter 에 데이터가 변경되었다고 notify 를 해줌
        userDB.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if(snapshot.child(USER_ID).value != getCurrentUserID()
                    && !snapshot.child(LIKED_BY).child(LIKE).hasChild(getCurrentUserID())
                    && !snapshot.child(LIKED_BY).child(DIS_LIKE).hasChild(getCurrentUserID())) {
                    // 현재 snapshot 정보가 내가 아니고(내 카드는 보일 필요가 없음), 좋아요나 싫어요를 누르지 않은 경우
                    val userId = snapshot.child(USER_ID).value.toString()
                    var name = "undecided"
                    if(snapshot.child(NAME).value != null){
                        name = snapshot.child(NAME).value.toString()
                    }
                    cardItems.add(CardItem(userId, name))
                    adapter.submitList(cardItems)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                cardItems.find{ it.userId == snapshot.key }?.let{
                    it.name = snapshot.child(NAME).value.toString()
                }
                adapter.submitList(cardItems)
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun showNameInputPopup(){
        // 로그인 이후에 이름 정보를 저장하기 위해 팝업을 띄워주는 메소드
        val editText = EditText(this) // EditText 생성
        AlertDialog.Builder(this) // AlertDialog 생성
            .setTitle(getString(R.string.write_name)) // title 설정
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
        // 이름 정보를 DB에 저장하는 메소드
        val userId = getCurrentUserID() // 현재 유저의 uid 불러오기
        val currentUserDB = userDB.child(userId)
        val user = mutableMapOf<String, Any>()
        user[USER_ID] = userId
        user[NAME] = name
        currentUserDB.updateChildren(user) // 이름 정보가 추가된 user 를 DB에 업데이트 -> Realtime Database 에 추가

        // 유저 정보를 가져옴
        getUnSelectedUsers()
    }
    private fun getCurrentUserID(): String{
        // 현재 유저의 uid 를 리턴
        if(auth.currentUser == null){
            Toast.makeText(this, "로그인이 되어있지 않습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
        return auth.currentUser?.uid.orEmpty()

    }
    private fun like(){
        // 좋아요
        val card = cardItems[manager.topPosition-1] // 좋아요 한 카드를 가져옴
        cardItems.removeFirst() // 리스트에서 삭제

        // DB -> 좋아요를 받은 사람의 uid -> likedBy -> like -> 좋아요를 누른 사람의 uid -> true
        userDB.child(card.userId)
            .child(LIKED_BY)
            .child(LIKE)
            .child(getCurrentUserID())
            .setValue(true)

        Toast.makeText(this, "${card.name}님을 Like 하셨습니다.", Toast.LENGTH_SHORT).show()
        // 매칭이 되었으면(서로 좋아요를 누른 상태) DB에 매치 유무를 저장
        saveMatchIfOtherUserLikedMe(card.userId)
    }
    private fun disLike() {
        // 싫어요
        val card = cardItems[manager.topPosition-1]
        cardItems.removeFirst()

        userDB.child(card.userId)
            .child(LIKED_BY)
            .child(DIS_LIKE)
            .child(getCurrentUserID())
            .setValue(true)

        Toast.makeText(this, "${card.name}님을 disLike 하셨습니다.", Toast.LENGTH_SHORT).show()
    }
    private fun saveMatchIfOtherUserLikedMe(otherUserId: String) {
        // 서로 매치가 되었는지 확인하고 매치가 되었으면 DB에 match 유무를 저장
        val otherUserDB = userDB.child(getCurrentUserID()).child(LIKED_BY).child(LIKE).child(otherUserId)
        otherUserDB.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == true){
                    userDB.child(getCurrentUserID())
                        .child(LIKED_BY)
                        .child(MATCH)
                        .child(otherUserId)
                        .setValue(true)
                    userDB.child(otherUserId)
                        .child(LIKED_BY)
                        .child(MATCH)
                        .child(getCurrentUserID())
                        .setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {}

    override fun onCardSwiped(direction: Direction?) {
        // 카드 스와이프 이벤트
        when(direction){
            Direction.Right-> like() // 오른쪽은 좋아요
            Direction.Left -> disLike() // 왼쪽은 싫어요
            else->{

            }
        }
    }

    override fun onCardRewound() {}

    override fun onCardCanceled() {}

    override fun onCardAppeared(view: View?, position: Int) {}

    override fun onCardDisappeared(view: View?, position: Int) {}
}