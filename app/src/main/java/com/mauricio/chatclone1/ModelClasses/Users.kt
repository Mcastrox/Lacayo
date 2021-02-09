package com.mauricio.chatclone1.ModelClasses

class Users {
    private var uid : String =""
    private var username : String =""
    private var profile : String =""
    private var cover : String =""
    private var search : String =""
    private var status : String =""
    private var facebook : String =""
    private var instagram : String =""
    private var website : String =""

    constructor()
    constructor(uid: String, username: String, profile: String, cover: String, search: String, status: String, facebook: String, instagram: String, website: String) {
        this.uid = uid
        this.username = username
        this.profile = profile
        this.cover = cover
        this.search = search
        this.status = status
        this.facebook = facebook
        this.instagram = instagram
        this.website = website
    }
    fun getUID (): String?{
        return uid
    }
    fun setUID (uid: String){
        this.uid=uid
    }
    fun getUsername (): String?{
        return username
    }
    fun serUsername (username: String){
        this.username=username
    }
    fun getProfile (): String?{
        return profile
    }
    fun setProfile (profile: String){
        this.profile=profile
    }
    fun getStatus (): String?{
        return status
    }
    fun setStatus (status: String){
        this.status=status
    }
    fun getCover (): String?{
        return cover
    }
    fun setCover (cover: String){
        this.cover=cover
    }
    fun getSearch (): String?{
        return search
    }
    fun setSearch (search: String){
        this.search=search
    }
    fun getFacebook (): String?{
        return facebook
    }
    fun setFacebook (facebook: String){
        this.facebook=facebook
    }
    fun getInstagram (): String?{
        return instagram
    }
    fun setInstagram (instagram: String){
        this.instagram=instagram
    }
    fun getWeb (): String?{
        return website
    }
    fun setWeb (website: String){
        this.website=website
    }





}