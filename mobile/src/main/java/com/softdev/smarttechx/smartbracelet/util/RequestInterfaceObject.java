package com.softdev.smarttechx.smartbracelet.util;


import com.softdev.smarttechx.smartbracelet.model.ServerRequest;
import com.softdev.smarttechx.smartbracelet.model.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RequestInterfaceObject {
    //index.php/

    @POST("apiV3/")
    Call<ServerResponse> operation(@Body ServerRequest request);

}
