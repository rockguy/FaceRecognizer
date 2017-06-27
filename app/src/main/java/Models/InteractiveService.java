package Models;

import java.io.File;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by vinnik on 13.03.2017.
 */

public interface InteractiveService {

    @GET("Photo/AllPhotos")
    Call<List<Photo>> allPhotos();

    @GET("Photo/AllPersons")
    Call<List<Person>> allPersons();

    @GET("Photo/CollectivePhoto")
    Call<ResponseBody> collectivePhoto();

    @Multipart
    @POST("Photo/AddPhoto")
    Call<String> addPhoto(@Part("shortName") RequestBody shortName,
                          @Part MultipartBody.Part photo);
    @Multipart
    @POST("Photo/Recognize")
    Call<Person> Recognize(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part photo);
    @Multipart
    @POST("Photo/RecognizePhotos")
    Call<List<Person>> Recognize(@Body File[] photo);

    @Multipart
    @POST("Photo/RecognizeConcatPhoto")
    Call<List<Person>> Recognize(@Part MultipartBody.Part photo);

    @POST("Photo/DeletePerson")
    Call<String> DeletePerson(@Body Person person);

    @POST("Photo/UpdatePerson")
    Call<String> UpdatePerson(@Body Person person);

    @GET("Photo/GetPersonDetail")
    Call<Person> GetPersonDetail(@Query("Id") int id);

    @GET("Photo/GetPersonInfo")
    Call<Person> GetPersonDetail(@Query("ShortName") String shortName);

    @GET("Photo/GetPersonPhoto")
    Call<ResponseBody> GetPersonPhoto(@Query("Id") int id);

    @GET("Photo/GetPersonPhotoInfo")
    Call<List<PhotoDetail>> GetPersonPhotoInfo(@Query("Id") int id);

    @GET("Photo/MakeMainPhoto")
    Call<String> MakeMainPhoto(@Query("Id") int id);

    @GET("Photo/DeletePhoto")
    Call<String> DeletePhoto(@Query("Id") int id);

    @POST("Photo/ChangeOwner")
    Call<String> ChangeOwner(@Query("Id") int id, @Query("shortName") String shortName);

}
