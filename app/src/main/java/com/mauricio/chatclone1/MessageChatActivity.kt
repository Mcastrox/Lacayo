package com.mauricio.chatclone1

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.mauricio.chatclone1.ModelClasses.Users
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MessageChatActivity : AppCompatActivity() {

    var userIdVisit : String = ""
    var firebaseUser: FirebaseUser?=null


    private lateinit var send_message_btn: ImageView
    private lateinit var message_text : EditText
    private lateinit var username_mchat: TextView
    private lateinit var profile_image_mchat: CircleImageView
    private lateinit var attact_image_file : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        // Loading username and profile picture
        username_mchat = findViewById(R.id.username_mchat)
        profile_image_mchat = findViewById(R.id.profile_image_mchat)

        intent= intent
        //receiver id
        userIdVisit= intent.getStringExtra("visit_id").toString()
        //sender id


        firebaseUser= FirebaseAuth.getInstance().currentUser

        val reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)
        reference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val user : Users? = p0.getValue(Users::class.java)
                username_mchat.text=user!!.getUsername()
                Picasso.get().load(user.getProfile()).into(profile_image_mchat)

            }
        })


        message_text= findViewById(R.id.text_message)
        send_message_btn= findViewById(R.id.send_message_btn)
        attact_image_file = findViewById(R.id.attact_image_file)


        send_message_btn.setOnClickListener {
            val message = message_text.text.toString()
            if(message==""){
                Toast.makeText(this,"Escribe algo por favor ...", Toast.LENGTH_SHORT).show()

            }
            else
            {
                sendMessageToUser(firebaseUser!!.uid,userIdVisit,message)
            }
            message_text.setText("")
        }
        attact_image_file.setOnClickListener {
            val intent = Intent()
            intent.action= Intent.ACTION_GET_CONTENT
            intent.type= "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"),438)

        }
    }

    private fun sendMessageToUser(senderId: String, receiverId: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap= HashMap<String, Any?>()
        messageHashMap["sender"]= senderId
        messageHashMap["message"]= message
        messageHashMap["receiver"]= receiverId
        messageHashMap["isseen"]= false
        messageHashMap["url"]=""
        messageHashMap["messageId"]= messageKey

        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener {task ->
                if(task.isSuccessful){
                    val chatsListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatLists")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)

                    chatsListReference.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if(!p0.exists())
                            {
                                chatsListReference.child("id").setValue(userIdVisit)

                            }
                            val chatsListReceiverRef = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatLists")
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)
                            chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                        }
                    })

                    //implement the push notifications using fcm
                        val reference = FirebaseDatabase.getInstance().reference
                            .child("Users").child(firebaseUser!!.uid)


                }
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==438 && resultCode== RESULT_OK && data!=null && data!!.data!=null)
        {
            val progressbar = ProgressDialog(this)
            progressbar.setMessage("Cargando por favor espere ...")
            progressbar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref= FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask : StorageTask<*>
            uploadTask= filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if ( !task.isSuccessful ){
                    task.exception?.let{
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if(task.isSuccessful)
                {

                        val downloadUrl = task.result
                        val url= downloadUrl.toString()


                        val messageHashMap= HashMap<String, Any?>()
                        messageHashMap["sender"]= firebaseUser!!.uid
                        messageHashMap["message"]= "Sent you an image . "
                        messageHashMap["receiver"]= userIdVisit
                        messageHashMap["isseen"]= false
                        messageHashMap["url"]=url
                        messageHashMap["messageId"]= messageId

                        ref.child("Chats").child(messageId!!).setValue(messageHashMap)

                    }
            }

        }
    }
}