package ir.ayantech.versioncontrol.model;

import com.google.gson.Gson;

/**
 * Created by Administrator on 6/10/2017.
 */

public class VCRequestModel {
    private VCInputModel Parameters;

    public VCRequestModel(VCInputModel Parameters) {
        this.Parameters = Parameters;
    }

    public VCRequestModel() {
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
