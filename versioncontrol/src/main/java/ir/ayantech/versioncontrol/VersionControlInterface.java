package ir.ayantech.versioncontrol;

import ir.ayantech.versioncontrol.api.CheckVersion;
import ir.ayantech.versioncontrol.api.GetLastVersion;
import ir.ayantech.versioncontrol.model.VCRequestModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by shadoWalker on 5/9/17.
 */

public interface VersionControlInterface {
    @POST("checkVersion")
    Call<CheckVersion.CheckVersionResponse> checkVersion(@Body VCRequestModel requestModel);

    @POST("getLastVersion")
    Call<GetLastVersion.GetLastVersionResponseModel> getLastVersion(@Body VCRequestModel requestModel);
}
