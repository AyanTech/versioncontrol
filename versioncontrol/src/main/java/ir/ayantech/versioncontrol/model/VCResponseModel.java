package ir.ayantech.versioncontrol.model;

import com.google.gson.Gson;

/**
 * Created by Administrator on 6/10/2017.
 */

public class VCResponseModel {
    private VCStatusModel Status;

    public VCResponseModel(VCStatusModel status) {
        this.Status = status;
    }

    public VCStatusModel getStatus() {
        return Status;
    }

    public void setStatus(VCStatusModel status) {
        this.Status = status;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
