package com.example.voteon

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login_page.*
import kotlinx.android.synthetic.main.activity_register_page.*
import kotlinx.android.synthetic.main.activity_register_page.loading_1
import kotlinx.android.synthetic.main.activity_register_page.txt_error
import kotlinx.android.synthetic.main.activity_register_page.txt_password
import kotlinx.android.synthetic.main.activity_register_page.txt_username

var password_s: String = ""
private lateinit var auth: FirebaseAuth
private lateinit var database: DatabaseReference

class RegisterPage : AppCompatActivity() {

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)
        auth = FirebaseAuth.getInstance()

        sign_in.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }

        var username = txt_username.text.toString()
        var email = txt_email.text.toString()
        var password_1 = txt_password.text.toString()
        var password_2 = txt_password1.text.toString()
        var error = txt_error.text

        cbx_show_password.setOnCheckedChangeListener { buttonView, isChecked ->
            if(cbx_show_password.isChecked) {
                txt_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                txt_password1.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
            }
            else{
                txt_password.setTransformationMethod(PasswordTransformationMethod.getInstance())
                txt_password1.setTransformationMethod(PasswordTransformationMethod.getInstance())
            }
        }

        txt_password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                calculatePasswordStrength(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })

        btn_register.setOnClickListener {
            var username = txt_username.text.toString()
            var email = txt_email.text.toString()
            var password_1 = txt_password.text.toString()
            var password_2 = txt_password1.text.toString()
            var error = txt_error.text
            txt_error.text = ""
            loading_1.visibility = View.VISIBLE
            txt_error.setTextColor(Color.parseColor("#E2574C"))
            if(username != "" && email != "" && password_1 != "" && password_2 != ""){
                if (username.length < 3){
                    Toast.makeText(this, username.length.toString(), Toast.LENGTH_SHORT).show()
                    txt_error.text = "user name too short"
                    loading_1.visibility = View.GONE
                }
                if(password_1 != password_2){
                    txt_error.text = "password mismatch"
                    loading_1.visibility = View.GONE
                }
                if(txt_error.text == ""){
                    Log.d("Auth", "1")
                    if(password_s != "weak password!"){

                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password_1)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    val user = auth.currentUser
                                    val uid = FirebaseAuth.getInstance().uid
                                    database = FirebaseDatabase.getInstance().reference

                                    //Saving user details to firebase
                                    writeNewUser(uid.toString(), username, email, password_1)

                                } else {
                                    loading_1.visibility = View.GONE
                                    txt_error.setTextColor(Color.parseColor("#FFB714"))
                                    try {
                                        throw task.exception!!
                                    } // if user enters wrong email.
                                    catch (weakPassword: FirebaseAuthWeakPasswordException) {
                                        txt_error.text = "onComplete: weak_password"

                                    } // if user enters wrong password.
                                    catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
                                        txt_error.text = "onComplete: malformed_email"

                                    } catch (existEmail: FirebaseAuthUserCollisionException) {
                                        txt_error.text = "onComplete: exist_email"

                                    } catch (e: Exception) {
                                        txt_error.text =  "onComplete: " + e.message
                                    }
                                }

                                // ...
                            }

                    }else{
                        loading_1.visibility = View.VISIBLE
                        txt_error.text = "Weak password!"
                    }

                }
            }
            else{
                loading_1.visibility = View.VISIBLE
                txt_error.text = "Fill out all forms!"
            }
        }
    }

    private fun calculatePasswordStrength(str: String) {
        // Now, we need to define a PasswordStrength enum
        // with a calculate static method returning the password strength
        val passwordStrength = PasswordStrength.calculate(str)
        txt_error.setText(passwordStrength.msg)
        txt_error.setTextColor(passwordStrength.color)
        password_s = txt_error.text.toString()
    }

    private fun writeNewUser(userId: String ,name: String, email: String, password: String) {
        val user = User_detail(email, name, password, userId, "")
        database = FirebaseDatabase.getInstance().reference

        database.child("users").child(userId).setValue(user).addOnSuccessListener {
            Toast.makeText(this, "Write successful.", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, Welcome::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            Handler().postDelayed({startActivity(intent)}, 2000)

        }.addOnFailureListener {
            loading_1.visibility = View.GONE
            Toast.makeText(this, "Write not successful.", Toast.LENGTH_SHORT).show()
        }
    }
}

enum class PasswordStrength(var msg: Int, var color: Int) {
    // we use some color in green tint =>
    //more secure is the password, more darker is the color associated
    WEAK(R.string.weak, Color.parseColor("#E2574C")),
    MEDIUM(R.string.medium, Color.parseColor("#FFB714")),
    STRONG(R.string.strong, Color.parseColor("#3399CC")),
    VERY_STRONG(R.string.very_strong, Color.parseColor("#264535"));

    companion object {
        private const val MIN_LENGTH = 8
        private const val MAX_LENGTH = 15
        fun calculate(password: String): PasswordStrength {
            var score = 0
            // boolean indicating if password has an upper case
            var upper = false
            // boolean indicating if password has a lower case
            var lower = false
            // boolean indicating if password has at least one digit
            var digit = false
            // boolean indicating if password has a leat one special char
            var specialChar = false
            for (i in 0 until password.length) {
                val c = password[i]
                if (!specialChar && !Character.isLetterOrDigit(c)) {
                    score++
                    specialChar = true
                } else {
                    if (!digit && Character.isDigit(c)) {
                        score++
                        digit = true
                    } else {
                        if (!upper || !lower) {
                            if (Character.isUpperCase(c)) {
                                upper = true
                            } else {
                                lower = true
                            }
                            if (upper && lower) {
                                score++
                            }
                        }
                    }
                }
            }
            val length = password.length
            if (length > MAX_LENGTH) {
                score++
            } else if (length < MIN_LENGTH) {
                score = 0
            }
            when (score) {
                0 -> return WEAK
                1 -> return MEDIUM
                2 -> return STRONG
                3 -> return VERY_STRONG
                else -> {
                }
            }
            return VERY_STRONG
        }
    }


}
