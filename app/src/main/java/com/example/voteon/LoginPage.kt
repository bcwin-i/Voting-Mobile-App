package com.example.voteon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login_page.*

class LoginPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)
        auth = FirebaseAuth.getInstance()

        sign_up.setOnClickListener {
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }

        val currentUser = auth.currentUser

        if(currentUser != null){
            val intent = Intent(this, Welcome::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        cbx_show_p.setOnCheckedChangeListener { buttonView, isChecked ->
            if(cbx_show_p.isChecked) {
                txt_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
            }
            else{
                txt_password.setTransformationMethod(PasswordTransformationMethod.getInstance())
            }
        }

        btn_sign_in.setOnClickListener {
            loading_1.visibility = View.VISIBLE
            val email = txt_username.text.toString()
            val password = txt_password.text.toString()

            if(email != "" && password != ""){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(
                        OnCompleteListener<AuthResult?> { task ->
                            if (!task.isSuccessful) {
                                try {
                                    throw task.exception!!
                                } // if user enters wrong email.
                                catch (invalidEmail: FirebaseAuthInvalidUserException) {
                                    txt_error.text = "invalid_email"

                                    // TODO: take your actions!
                                } // if user enters wrong password.
                                catch (wrongPassword: FirebaseAuthInvalidCredentialsException) {
                                    txt_error.text = "wrong_password"

                                    // TODO: Take your action
                                } catch (e: Exception) {
                                    txt_error.text =  e.message
                                }
                            }else{
                                val intent = Intent(this, Welcome::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                Handler().postDelayed({startActivity(intent)}, 2000)
                            }
                        }
                    )
            }else{
                txt_error.text =  "Empty field!"
            }
            loading_1.visibility = View.GONE
        }


    }
}
