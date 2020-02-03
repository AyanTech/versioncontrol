package ir.ayantech.versioncontrol.api;

import androidx.annotation.Nullable;

import ir.ayantech.versioncontrol.model.VCResponseModel;


/**
 * Created by shadoWalker on 5/9/17.
 */

public interface VCResponseStatus {
    void onSuccess(VersionControlAPI versionControlAPI, String message, @Nullable VCResponseModel responseModel);

    void onFail(VersionControlAPI versionControlAPI,
                String error,
                boolean canTry);
}
