package top.orosirian.mcp.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import top.orosirian.mcp.model.resource.MusicResponse;

public interface MusicApi {

    @GET("/api/music/bdyy")
    Call<MusicResponse> getMusic(
            @Query("msg") String msg,
            @Query("n") String n,
            @Query("type") String type
    );

    // https://m.bqgl.cc/look/197809/

}
