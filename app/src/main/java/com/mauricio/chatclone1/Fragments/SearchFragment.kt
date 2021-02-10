package com.mauricio.chatclone1.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mauricio.chatclone1.AdaptersClasses.UserAdapter
import com.mauricio.chatclone1.ModelClasses.Users
import com.mauricio.chatclone1.R
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var userAdapter : UserAdapter ? = null
    private var mUsers : List<Users> ? = null
    private lateinit var searchUserET: EditText
    private var recyclerView: RecyclerView ?= null

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
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchUserET = view.findViewById(R.id.searchUsersET)

        recyclerView = view.findViewById(R.id.searchList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager= LinearLayoutManager(context)


        mUsers= ArrayList()
        retrieveAllUsers()

        searchUserET!!.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
              searchForUsers(s.toString().toLowerCase())
            }

        })


        return view
    }
    private fun retrieveAllUsers(){
        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val  refUsers = FirebaseDatabase.getInstance().reference.child("Users")

        refUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                if (searchUserET!!.text.toString() == "")
                {
                    for(snapshot in p0.children){
                        val user : Users? = snapshot.getValue(Users::class.java)
                        if(!(user!!.getUID()).equals(firebaseUserID)){

                            // adding all the users to the view except my own profile
                            (mUsers as ArrayList<Users>).add(user)

                }
            }
                }
                userAdapter = UserAdapter(context!!, mUsers!!,false)
                recyclerView!!.adapter= userAdapter
            }

        })

    }

    //Searching for specific username

    private fun searchForUsers(str: String){
        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val  queryUsers = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("search").startAt(str)
                .endAt(str+ "\uf8ff")
        queryUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                for(snapshot in p0.children){
                    val user : Users? = snapshot.getValue(Users::class.java)
                    if(!(user!!.getUID()).equals(firebaseUserID)){

                        // adding all the users to the view except my own profile
                        (mUsers as ArrayList<Users>).add(user)

                    }
                }
                userAdapter = UserAdapter(context!!, mUsers!!,false)
                recyclerView!!.adapter= userAdapter

            }
        })





    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}