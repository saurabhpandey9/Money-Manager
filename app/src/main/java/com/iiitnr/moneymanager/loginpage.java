package com.iiitnr.moneymanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class loginpage extends AppCompatActivity {

    private TextView regpage_btn;
    private EditText email;
    private EditText password;
    private Button loginbtn;
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;
    private TextView forpass_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);

        mAuth=FirebaseAuth.getInstance();
        mDialog=new ProgressDialog(this);

        if(mAuth.getCurrentUser()!=null)
        {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        email =findViewById(R.id.email_et);
        password =findViewById(R.id.password_et);
        loginbtn =findViewById(R.id.login_bt);
        regpage_btn =findViewById(R.id.signup_tv);
        forpass_btn=findViewById(R.id.forgetpass_tv);


        //forgot password page starting

        forpass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forPassCustomDialog();
            }
        });


        //forgot page ending



        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mEmail = email.getText().toString().trim();
                String mPassword = password.getText().toString().trim();

                if (TextUtils.isEmpty(mEmail)) {
                    email.setError("Please Enter Valid Email");
                    return;
                }

                if (TextUtils.isEmpty(mPassword)) {
                    password.setError("Please Enter Valid Password");
                    return;
                }

                mDialog.setMessage("Please wait....");
                mDialog.show();

                mAuth.signInWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mDialog.dismiss();
                            checkEmailVerification();
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Login Failed!!..", Toast.LENGTH_LONG).show();

                        }
                    }

                });

            }
        });






        regpage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Registration.class));
                finish();
            }
        });


    }

    private void checkEmailVerification(){

        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        boolean emailflag=firebaseUser.isEmailVerified();

        if(emailflag){

            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
            finish();

        }
        else
        {
            Toast.makeText(getApplicationContext(), "Please verify your Email", Toast.LENGTH_LONG).show();
            mAuth.signOut();
        }
    }


    private void forPassCustomDialog()
    {
        final AlertDialog.Builder myDialog =new AlertDialog.Builder(loginpage.this);
        LayoutInflater inflater =LayoutInflater.from(loginpage.this);

        final View myview=inflater.inflate(R.layout.forgot_pass,null);

        final AlertDialog dialog=myDialog.create();
        dialog.setView(myview);

        final EditText forpassEmail=myview.findViewById(R.id.forpassemail_et);
        Button resetpass_btn=myview.findViewById(R.id.forpass_bt);

        resetpass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mEmail=forpassEmail.getText().toString().trim();
                if(TextUtils.isEmpty(mEmail))
                {
                    forpassEmail.setError("Please Enter Valid Email..!");
                    return;
                }

                mDialog.setMessage("Sending Reset Link...");
                mDialog.show();
                mAuth.sendPasswordResetEmail(mEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            dialog.dismiss();
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Reset link sent..!", Toast.LENGTH_LONG).show();

                        }
                        else
                        {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Error...in sending Password Reset Link", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        dialog.show();
    }

}
