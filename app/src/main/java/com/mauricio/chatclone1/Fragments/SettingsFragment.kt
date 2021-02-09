package com.mauricio.chatclone1.Fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.mauricio.chatclone1.ModelClasses.Users
import com.mauricio.chatclone1.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.net.URI

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

// INICIALIZANDO LAS COSAS QUE NECESITO

    private  lateinit var  set_facebook : ImageView
    private lateinit var set_instagram : ImageView
    private lateinit var set_website : ImageView


    var useReference : DatabaseReference ? = null
    var firebaseUser: FirebaseUser ? =null

    private lateinit var username_setting : TextView
    private lateinit var profile_image_settings : CircleImageView
    private lateinit var cover_image_settings : ImageView

    private val RequestCode = 438

    private var imageURI : Uri? = null
    private var storageRef : StorageReference ? = null

    private var coverChecked : String? = ""
    private var socialChecker : String? =""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        //social links
        set_facebook = view. findViewById(R.id.set_facebook)
        set_instagram = view.findViewById(R.id.set_instagram)
        set_website = view.findViewById(R.id.set_website)

        // Fotos de perfil y nombre de usuario
        username_setting = view. findViewById(R.id.username_settings)
        profile_image_settings = view.findViewById(R.id.profile_image_settings)
        cover_image_settings = view.findViewById(R.id.cover_image_settings)

        // Firebase References
        firebaseUser = FirebaseAuth.getInstance().currentUser
        useReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        useReference!!.addValueEventListener(object  : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val user: Users? = p0.getValue(Users::class.java)
                   if(context!= null){
                       username_setting.text = user!!.getUsername()
                       Picasso.get().load(user.getProfile()).into(profile_image_settings)
                       Picasso.get().load(user.getCover()).into(cover_image_settings)
                   }

                }
            }
        })

        profile_image_settings.setOnClickListener {
            pickImage()
        }

        cover_image_settings.setOnClickListener {
            coverChecked= "cover"
            pickImage()
        }

        set_facebook.setOnClickListener{
            socialChecker="facebook"
            setSocialLinks()

        }
        set_instagram.setOnClickListener {
            socialChecker="instagram"
            setSocialLinks()
        }
        set_website.setOnClickListener {
            socialChecker="website"
            setSocialLinks()
        }

        return view
    }

    private fun setSocialLinks() {
        val builder: AlertDialog.Builder =
                AlertDialog.Builder(context, R.style.ThemeOverlay_AppCompat_Dialog_Alert)

        if (socialChecker == "website") {
            builder.setTitle("Write URL:")
        } else {
            builder.setTitle("Write username:")
        }

        val editText = EditText(context)

        if( socialChecker == "website")
        {
            editText.hint="e.g www.google.com"
        }
        else
        {
            editText.hint="e.g jmca720"
        }
        builder.setView(editText)

        builder.setPositiveButton("Guardar",DialogInterface.OnClickListener { dialog, which ->
            val str = editText.text.toString()
            if(str == ""){
                Toast.makeText(context,"Por favor escribe algo ",Toast.LENGTH_SHORT).show()
            }
            else
            {
                saveSocialLinks(str)
            }
        })
        builder.setNegativeButton("Cancelar",DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        builder.show()


    }

    private fun saveSocialLinks(str: String) {
        val mapSocial = HashMap<String, Any>()
        useReference!!.updateChildren(mapSocial)


        when (socialChecker)
        {
            "facebook" ->
            {
                mapSocial["facebook"]= "https://m.facebook.com/$str"
            }
            "instagram" ->
            {
                mapSocial["instagram"]= "https://m.instagram.com/$str"
            }
            "website" ->
            {
                mapSocial["website"]= "https://$str"
            }
        }
        useReference!!.updateChildren(mapSocial).addOnCompleteListener {task ->
            if (task.isSuccessful){
                Toast.makeText(context,"Se ha guardado correctamente ",Toast.LENGTH_SHORT).show()

            }

        }

    }


    private fun pickImage() {
        val intent = Intent ()
        intent.type= "image/*"
        intent.action= Intent.ACTION_GET_CONTENT

        startActivityForResult(intent,RequestCode)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null)
        {
            imageURI= data.data
            Toast.makeText(context,"Subiendo foto",Toast.LENGTH_SHORT).show()
            uploadImageToDatabase()
        }
    }

    private fun uploadImageToDatabase() {
        val progressbar = ProgressDialog(context)
        progressbar.setMessage("Cargando por favor espere ...")
        progressbar.show()

        if(imageURI!=null){
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString()+".jpg")

            var uploadTask : StorageTask<*>
            uploadTask= fileRef.putFile(imageURI!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot,Task<Uri>>{ task ->
              if ( !task.isSuccessful ){
                  task.exception?.let{
                      throw it
                  }
              }
            return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val downloadUrl = task.result
                    val url= downloadUrl.toString()

                    if(coverChecked=="cover"){
                        val mapCoverImg = HashMap<String, Any>()
                        mapCoverImg["cover"]= url
                        useReference!!.updateChildren(mapCoverImg)
                        coverChecked=""
                    }
                    else
                    {
                        val mapProfileImg = HashMap<String, Any>()
                        mapProfileImg["profile"]= url
                        useReference!!.updateChildren(mapProfileImg)
                        coverChecked=""
                    }
                    progressbar.dismiss()

                }
            }

        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}