package ir.ayantech.versioncontrol;


import ir.ayantech.versioncontrol.api.VCResponseStatus;
import ir.ayantech.versioncontrol.model.VCInputModel;

/**
 * Created by shadoWalker on 5/9/17.
 */

public interface CallVCApi<RequestModel extends VCInputModel> {
    void callApi(VCResponseStatus status, RequestModel inputModel);
}
