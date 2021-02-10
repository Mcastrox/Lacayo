package com.mauricio.chatclone1.AdaptersClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mauricio.chatclone1.MessageChatActivity
import com.mauricio.chatclone1.ModelClasses.Users
import com.mauricio.chatclone1.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter (mContext: Context,mUsers :  List<Users>,isChatChecked : Boolean): RecyclerView.Adapter<UserAdapter.ViewHolder?>() {
    private val mContext : Context
    private val mUsers: List<Users>
    private val isChatChecked: Boolean
    init {
        this.isChatChecked= isChatChecked
        this.mContext=mContext
        this.mUsers=mUsers
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout,viewGroup,false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user : Users = mUsers[position]
        holder.userNameTxt.text= user!!.getUsername()
        Picasso.get().load(user.getProfile()).into(holder.profileImageView)

        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Enviar mensaje ",
                "Ver perfil"

            )
            val builder : AlertDialog.Builder= AlertDialog.Builder(mContext)
            builder.setTitle("¿ Que deseas hacer ?")
            builder.setItems(options, DialogInterface.OnClickListener{dialog, position ->
                if(position== 0){
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("visit_id", user.getUID().toString())
                    mContext.startActivity(intent)
                }
                if(position== 1){

                }
            })
            builder.show()
        }
    }

    class ViewHolder (itemView : View): RecyclerView.ViewHolder(itemView){

        val userNameTxt : TextView
        val profileImageView : CircleImageView
        val onLineTxt : CircleImageView
        val offLineTxt : CircleImageView
        val lastMessageTxt : TextView
        init {
            userNameTxt = itemView.findViewById(R.id.username)
            profileImageView = itemView.findViewById(R.id.profile_image)
            onLineTxt = itemView.findViewById(R.id.image_online)
            offLineTxt = itemView.findViewById(R.id.image_offline)
            lastMessageTxt = itemView.findViewById(R.id.message_last)

        }

    }
}
