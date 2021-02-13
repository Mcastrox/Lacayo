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
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.mauricio.chatclone1.AdaptersClasses.ChatsAdapter
import com.mauricio.chatclone1.Fragments.APIService
import com.mauricio.chatclone1.ModelClasses.Chat
import com.mauricio.chatclone1.ModelClasses.Users
import com.mauricio.chatclone1.Notifications.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageChatActivity : AppCompatActivity() {

    var userIdVisit : String = ""
    var firebaseUser: FirebaseUser?=null
    var chatsAdapter: ChatsAdapter?=null
    var mChatList : List<Chat>?=null

    var apiService: APIService? = null

    var notify = false



    private lateinit var send_message_btn: ImageView
    private lateinit var message_text : EditText
    private lateinit var username_mchat: TextView
    private lateinit var profile_image_mchat: CircleImageView
    private lateinit var attact_image_file : ImageView
    private lateinit var recycler_view_chats: RecyclerView

    var reference : DatabaseReference? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        var toolbar: Toolbar= findViewById(R.id.toolbar_message_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title=""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this,WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        apiService= Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)


        // Loading chats on recyclerview
        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd= true
        recycler_view_chats.layoutManager=linearLayoutManager


        // Loading username and profile picture
        username_mchat = findViewById(R.id.username_mchat)
        profile_image_mchat = findViewById(R.id.profile_image_mchat)

        intent= intent
        //receiver id
        userIdVisit= intent.getStringExtra("visit_id").toString()
        //sender id


        firebaseUser= FirebaseAuth.getInstance().currentUser

         reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val user : Users? = p0.getValue(Users::class.java)
                username_mchat.text=user!!.getUsername()
                Picasso.get().load(user.getProfile()).into(profile_image_mchat)

                retrieveMessages (firebaseUser!!.uid,userIdVisit,user.getProfile())

            }
        })


        message_text= findViewById(R.id.text_message)
        send_message_btn= findViewById(R.id.send_message_btn)
        attact_image_file = findViewById(R.id.attact_image_file)


        send_message_btn.setOnClickListener {
            notify=true
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
            notify=true
            val intent = Intent()
            intent.action= Intent.ACTION_GET_CONTENT
            intent.type= "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"),438)

        }
        seenMessages(userIdVisit)
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
                }
            }
//implement the push notifications using fcm
        val usersReference = FirebaseDatabase.getInstance().reference
                .child("Users").child(firebaseUser!!.uid)
        usersReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val user= p0.getValue(Users::class.java)
                if(notify)
                {
                    sendNotification(receiverId,user!!.getUsername(),message)
                }
                notify= false
            }

        })
    }

    private fun sendNotification(receiverId: String, username: String?, message: String) {
        val ref= FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverId)

        query.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children)
                {

                    val token : Token? = dataSnapshot.getValue(Token::class.java)

                    val data = Data(firebaseUser!!.uid,
                            R.mipmap.ic_launcher,
                            "$username: $message",
                            "Nuevo mensaje ",
                            userIdVisit

                            )
                    val sender = Sender(data!!,token!!.getToken().toString())
                    apiService!!.sendNotification(sender)
                            .enqueue(object : Callback<MyResponse>
                            {
                                override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                                    TODO("Not yet implemented")
                                }

                                override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                                    if ( response.code()==200)
                                    {
                                        if(response.body()!!.success !==1)
                                        {
                                            Toast.makeText(this@MessageChatActivity,"Failed nothing happen", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }

                            })
                }
            }

        })
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
                                .addOnCompleteListener { task ->
                                    if(task.isSuccessful)
                                    {
                                        progressbar.dismiss()

                                        //implement the push notifications using fcm
                                        val reference = FirebaseDatabase.getInstance().reference
                                                .child("Users").child(firebaseUser!!.uid)
                                        reference.addValueEventListener(object : ValueEventListener{
                                            override fun onCancelled(error: DatabaseError) {
                                                TODO("Not yet implemented")
                                            }

                                            override fun onDataChange(p0: DataSnapshot) {
                                                val user= p0.getValue(Users::class.java)
                                                if(notify)
                                                {
                                                    sendNotification(userIdVisit,user!!.getUsername(),"Sent you an image . ")
                                                }
                                                notify= false
                                            }

                                        })

                                    }
                                }


                    }
            }

        }
    }

    private fun retrieveMessages(senderId: String, receiverId: String, receiverImageUrl: String?) {

        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for(snapshot in p0.children)
                {
                    val chat = snapshot.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                            || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId) )
                    {
                        (mChatList as ArrayList<Chat>).add(chat)

                    }
                    chatsAdapter= ChatsAdapter(this@MessageChatActivity,(mChatList as ArrayList<Chat>),receiverImageUrl!!)
                    recycler_view_chats.adapter= chatsAdapter
                }
            }
        })
    }

    var seenListener: ValueEventListener? = null


    private fun seenMessages(userId: String){
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener= reference!!.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot)
            {
            for (dataSnapshot in p0.children)
                {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userIdVisit))
                    {
                        val hasMap = HashMap<String , Any?>()
                        hasMap["isseen"] = true
                        dataSnapshot.ref.updateChildren(hasMap)

                    }
                }
            }

        })
    }

    override fun onPause() {
        super.onPause()
        reference!!.removeEventListener(seenListener!!)

    }
}