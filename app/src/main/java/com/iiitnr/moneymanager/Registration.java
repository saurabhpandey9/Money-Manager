package com.iiitnr.moneymanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Registration extends AppCompatActivity {

    private TextView loginpage_tv;
    private EditText Email;
    private EditText Password;
    private Button Regbutton;
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        mAuth=FirebaseAuth.getInstance();

        Email=findViewById(R.id.reg_email_et);
        Password=findViewById(R.id.reg_password_et);
        Regbutton=findViewById(R.id.registration_bt);
        loginpage_tv=findViewById(R.id.login_tv);

        mDialog=new ProgressDialog(this);



        Regbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mEmail=Email.getText().toString().trim();
                String mPassword=Password.getText().toString().trim();

                if(TextUtils.isEmpty(mEmail)) {
                    Email.setError("Please Enter Valid Email");
                    return;
                }
                if(TextUtils.isEmpty(mPassword))
                {
                    Password.setError("Please Enter Valid Password");
                    return;
                }

                mDialog.setMessage("Registering...");
                mDialog.show();

                mAuth.createUserWithEmailAndPassword(mEmail,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            mDialog.dismiss();
                            sendEmailVerificaionLink();
                            finish();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Registration failed!!..",Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        }
                    }
                });
            }
        });



        loginpage_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),loginpage.class));
                finish();
            }
        });

    }

    private void sendEmailVerificaionLink(){

        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null)
        {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"Registered Successfully..Verification link sent..!",Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(getApplicationContext(),loginpage.class));
                        finish();
                        Toast.makeText(getApplicationContext(),"Please Verify your email before Login",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Error...Verification link hasn't sent..!!",Toast.LENGTH_LONG).show();
                        mAuth.signOut();

                    }

                }
            });
        }
    }
}
