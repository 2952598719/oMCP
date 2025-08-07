package top.orosirian.mcp.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class Beans {

    @Bean
    public MusicApi musicApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.xcvts.cn")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(MusicApi.class);
    }

}
