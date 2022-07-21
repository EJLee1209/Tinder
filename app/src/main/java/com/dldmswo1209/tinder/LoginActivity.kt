package com.dldmswo1209.tinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.dldmswo1209.tinder.databinding.ActivityLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private var mBinding : ActivityLoginBinding? = null
    private val binding get() = mBinding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        // 페이스북 로그인 응답을 처리할 callbackManager 생성
        callbackManager = CallbackManager.Factory.create()

        initButton()
        initEmailAndPasswordEditText()
        initFacebookLoginButton()
    }

    private fun initButton() {
        binding.loginButton.setOnClickListener {
            auth.signInWithEmailAndPassword(getInputEmail(), getInputPassword())
                .addOnSuccessListener {
                    // 로그인 성공시 로그인 액티비티 종료
                    handleSuccessLogin()
                }
                .addOnFailureListener {
                    // 로그인 실패시 토스트 메시지 표시
                    Toast.makeText(this, "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
                }

        }
        binding.signUpButton.setOnClickListener {
            auth.createUserWithEmailAndPassword(getInputEmail(),getInputPassword())
                .addOnSuccessListener {
                    Toast.makeText(this, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "이미 가입한 이메일 이거나, 회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }

        }
    }
    private fun initEmailAndPasswordEditText(){
        binding.emailEditText.addTextChangedListener {
            val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
            binding.signUpButton.isEnabled = enable
            binding.loginButton.isEnabled = enable
        }
        binding.passwordEditText.addTextChangedListener{
            val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
            binding.signUpButton.isEnabled = enable
            binding.loginButton.isEnabled = enable
        }
    }
    private fun initFacebookLoginButton(){ // facebook 로그인 버튼 이벤트 처리
        binding.facebookLoginButton.setPermissions("email","public_profile") // 로그인 버튼에 권한 요청(email 과 프로필 가져오기)
        // 로그인 결과에 응답하기 위해서 로그인 버튼에 콜백 등록
        binding.facebookLoginButton.registerCallback(callbackManager, object: FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult) {
                // 로그인 성공
                // Firebase auth 로 값을 전달해서 로그인
                val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                auth.signInWithCredential(credential) // token 으로 만들어진 credential 을 통해 로그인 시도
                    .addOnSuccessListener {
                        handleSuccessLogin() // 로그인 성공시 DB에 정보를 저장하는 메소드
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@LoginActivity, "페이스북 로그인이 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }

            }
            override fun onCancel() {}

            override fun onError(error: FacebookException) {
                Toast.makeText(this@LoginActivity, "페이스북 로그인이 실패했습니다.", Toast.LENGTH_SHORT).show()
            }


        })
    }

    private fun getInputPassword(): String {
        return binding.passwordEditText.text.toString()
    }

    private fun getInputEmail(): String {
        return binding.emailEditText.text.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSuccessLogin(){
        if(auth.currentUser == null){
            Toast.makeText(this, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = auth.currentUser!!.uid
        // database의 Users -> userId(현재 로그인한 유저의 uid)
        val currentUserDB = Firebase.database.reference.child("Users").child(userId)
        // DB에 저장할 형태 key-value
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId
        // user 정보를 db에 저장한다 Users -> userId -> user
        currentUserDB.updateChildren(user)
        finish()
    }

}