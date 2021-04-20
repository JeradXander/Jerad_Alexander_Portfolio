package com.alexanderapps.rubbersidedown.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {"Content-Type:application/json",
            "Authorization:key=AAAABodATu0:APA91bFN8Pv-wC_ZHqQc5oktYBzuROU89eKMsTcyF_81sGhuPlyb5NL2M_xF9w4ZkiOn1HKmOpbLcO-8P7zC6Gull0D_T9BK1HSIDwkRihaXwPZdB46pqqn79XiYbmKdbXYIG7UsJjoQ"
            })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
