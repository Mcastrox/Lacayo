package com.mauricio.chatclone1.Fragments;

import com.mauricio.chatclone1.Notifications.MyResponse;
import com.mauricio.chatclone1.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAhTWeJRQ:APA91bEbtSKuGZbVjuz-fWD-I_kNFKaEfe74kkLHB8wPO-nKPQg5fKQ3xeyZISbt_mlOAZEYTljePwbb9ZmydsvrGI0uWej_ooSG4u2ARKDtdrFOemBR9zaxbAEWZpKFiloIyIPSv5rr"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
