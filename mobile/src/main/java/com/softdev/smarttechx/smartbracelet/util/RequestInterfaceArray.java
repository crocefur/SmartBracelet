package com.softdev.smarttechx.smartbracelet.util;


import com.softdev.smarttechx.smartbracelet.model.ServerRequest;
import com.softdev.smarttechx.smartbracelet.model.UserList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RequestInterfaceArray {
    //index.php/

    @POST("apiV3/")
    Call<UserList> operation(@Body ServerRequest request);

}
