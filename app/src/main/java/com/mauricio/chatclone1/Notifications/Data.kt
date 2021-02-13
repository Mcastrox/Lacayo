package com.mauricio.chatclone1.Notifications

class Data {
    private var user : String=""
    private var icon =0
    private var body : String=""
    private var title : String=""
    private var  sented: String=""
  //  private var messageId: String=""

    constructor(){}
    constructor(user: String, icon: Int, body: String, title: String, sented: String) {
        this.user = user
        this.icon = icon
        this.body = body
        this.title = title
        this.sented = sented
        //this.messageId= messageId
    }

    fun getUser(): String?{
        return user
    }
    fun setUser(user: String){
        this.user = user
    }
    fun getIcon(): Int{
        return icon
    }
    fun setIcon(icon: Int){
        this.icon = icon
    }
    fun getBody(): String?{
        return body
    }
    fun setBody(body: String){
        this.body = body
    }
    fun getTitle(): String?{
        return title
    }
    fun setTitle(title: String){
        this.title = title
    }
    fun getSented(): String?{
        return sented
    }
    fun setSented(sented: String){
        this.sented = sented
    }
/*
    fun getMessageId(): String?{
        return messageId
    }
    fun setMessageId(messageId: String){
        this.messageId= messageId
    }

 */


}