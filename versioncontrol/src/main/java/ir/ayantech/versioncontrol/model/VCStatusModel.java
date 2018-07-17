package ir.ayantech.versioncontrol.model;

/**
 * Created by Administrator on 6/10/2017.
 */

public class VCStatusModel {
    private String Code;
    private String Description;

    public VCStatusModel(String code, String message) {
        this.Code = code;
        this.Description = message;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        this.Code = code;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String message) {
        this.Description = message;
    }
}
