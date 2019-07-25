package com.iiitnr.moneymanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iiitnr.moneymanager.Model.Data;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton fab_button;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private TextView totalsumtv;
    private ProgressDialog mDialog;

    //Global Variable

    private String type;
    private int amount;
    private String note;
    private String post_key;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //variable decleration for progress dialog box
        mDialog=new ProgressDialog(this);

        //totalsumtv defind by id
        totalsumtv=findViewById(R.id.total_ammount);

        toolbar=findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Money Management List");

        mAuth=FirebaseAuth.getInstance();

        FirebaseUser mUser=mAuth.getCurrentUser();
        String uId=mUser.getUid();

        mDatabase=FirebaseDatabase.getInstance().getReference().child("Money Management List").child(uId);

        mDatabase.keepSynced(true);


        //Total sum of money spent calculation
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int totalamount=0;

                for(DataSnapshot snap:dataSnapshot.getChildren()){
                    Data data=snap.getValue(Data.class);
                    totalamount+=data.getAmount();

                    String totalsum=String.valueOf(totalamount);

                    totalsum="Rs."+totalsum;
                    totalsumtv.setText(totalsum);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //implementation of recycler view
        recyclerView=findViewById(R.id.recycler_home);
        LinearLayoutManager layoutManager= new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        //ending of recycler view



        fab_button=findViewById(R.id.fab_btn);


        fab_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog();
            }
        });

    }

    private void customDialog()
    {
        AlertDialog.Builder myDialog =new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater =LayoutInflater.from(MainActivity.this);

        View myview=inflater.inflate(R.layout.input_doc,null);

        final AlertDialog dialog=myDialog.create();
        dialog.setView(myview);

        final EditText type=myview.findViewById(R.id.edt_type);
        final EditText ammount=myview.findViewById(R.id.edt_ammount);
        final EditText note=myview.findViewById(R.id.edt_note);
        Button savebtn=myview.findViewById(R.id.save_btn);

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mType=type.getText().toString().trim();
                String mAmmount=ammount.getText().toString().trim();
                String mNote=note.getText().toString().trim();



                if(TextUtils.isEmpty(mType))
                {
                    type.setError("Please Enter Type");
                    return;
                }

                if (TextUtils.isEmpty(mAmmount))
                {
                    ammount.setError("Enter Amount");
                    return;
                }


                int mammount=Integer.parseInt(mAmmount);
                String id=mDatabase.push().getKey();
                String date= DateFormat.getDateInstance().format(new Date());

                Data data=new Data(mType,mammount,mNote,date,id);

                mDialog.setMessage("Adding...");
                mDialog.show();

                mDatabase.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(),"Data Added",Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        }

                        else
                        {
                            Toast.makeText(getApplicationContext(),"Data Addition Failed..",Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        }
                    }
                });



                dialog.dismiss();
            }
        });

        dialog.show();


    }
    //this part is being dedicated to logout button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.logout_btn:
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(),loginpage.class));
                Toast.makeText(getApplicationContext(),"logout successfully",Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //for retriving data from database
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Data,MyViewHolder>adapter=new FirebaseRecyclerAdapter<Data, MyViewHolder>
                (
                        Data.class,
                        R.layout.item_data,
                        MyViewHolder.class,
                        mDatabase
                ) {
            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, final Data model, final int position) {
                viewHolder.setDate(model.getDate());
                viewHolder.setType(model.getType());
                viewHolder.setNote(model.getNote());
                viewHolder.setAmount(model.getAmount());

                //Th work of this function is that when you click on this it will open update layout
                viewHolder.myview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        post_key=getRef(position).getKey();
                        type=model.getType();
                        note=model.getNote();
                        amount=model.getAmount();

                        updateDate();
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{

        View myview;
        public MyViewHolder(View itemView) {
            super(itemView);
            myview=itemView;
        }

        public void setType (String type){
            TextView mType=myview.findViewById(R.id.type_tv);
            mType.setText(type);
        }

        public void setNote(String note){
            TextView mNote=myview.findViewById(R.id.note_tv);
            mNote.setText(note);
        }

        public void setDate(String date){
            TextView mDate=myview.findViewById(R.id.date_tv);
            mDate.setText(date);
        }

        public void setAmount(int amount){
            TextView mAmount=myview.findViewById(R.id.amountspnt_tv);
            String amm=String.valueOf(amount);
            mAmount.setText(amm);
        }
    }

    //retriving database part ended

    //start update database part

    public void updateDate(){

        final AlertDialog.Builder myDialog=new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater=LayoutInflater.from(MainActivity.this);

        View mview=inflater.inflate(R.layout.update_input,null);

        final AlertDialog dialog=myDialog.create();

        dialog.setView(mview);

        final EditText type_updtv=mview.findViewById(R.id.edt_type_upd);
        final EditText amount_updtv=mview.findViewById(R.id.edt_ammount_upd);
        final EditText note_updtv=mview.findViewById(R.id.edt_note_upd);

        //written while making update option
        type_updtv.setText(type);
        type_updtv.setSelection(type.length());

        amount_updtv.setText(String.valueOf(amount));
        amount_updtv.setSelection(String.valueOf(amount).length());

        note_updtv.setText(note);
        note_updtv.setSelection(note.length());
        //till here(update option)

        Button update_updbtn=mview.findViewById(R.id.update_btn);
        Button delete_updbtn=mview.findViewById(R.id.delete_btn);

        update_updbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                type=type_updtv.getText().toString().trim();
                String mAmount=amount_updtv.getText().toString().trim();
                note=note_updtv.getText().toString().trim();


                if(TextUtils.isEmpty(type))
                {
                    type_updtv.setError("Please Enter Type..!!");
                    return;
                }

                if(TextUtils.isEmpty(mAmount))
                {
                    amount_updtv.setError("Please Enter Amount..!!");
                    return;
                }



                amount =Integer.parseInt(mAmount);
                String date=DateFormat.getDateInstance().format(new Date());


                Data data=new Data(type,amount,note,date,post_key);
                mDatabase.child(post_key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(),"Data updated Successfully",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"updation failed..",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.dismiss();

            }
        });

        delete_updbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDialog.setMessage("Deleting..");
                mDialog.show();

                mDatabase.child(post_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Data deleted successfully",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Deletion Failed..!!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.dismiss();

            }
        });

        dialog.show();

    }

}
