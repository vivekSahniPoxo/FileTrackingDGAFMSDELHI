package com.example.filetracking.utils;

/**
 * @author zzc
 * @date 2019/06/17
 */
public class ErrorStatus {
    public static String STATUS = "";

    ErrorStatus() {

    }

    /**
     *
     * @param errorCode 错误码
     * @return 返回状态解释
     */
    public static String getErrorStatus(int errorCode) {
        switch (errorCode) {
            case -20000:
            case -9986:
                STATUS = "memory allocation failed";
                break;
            case -19999:
                STATUS = "No device found";
                break;
            case -19998:
                STATUS = "Failed to get device information";
                break;
            case -19997:
                STATUS = "Device serial number out of bounds";
                break;
            case -19996:
                STATUS = "Turn off devices that are turned off";
                break;
            case -19995:
                STATUS = "The flag to read the tag is 2";
                break;
            case -19994:
                STATUS = "read tag mac error";
                break;
            case -19993:
            case 4:
                STATUS = "Tag not found";
                break;
            case -19992:
                STATUS = "write data mismatch";
                break;
            case -19991:
                STATUS = "Failed to read tag";
                break;
            case -19990:
            case -1:
                STATUS = "operation failed";
                break;
            case -19989:
                STATUS = "read password error";
                break;
            case -19988:
                STATUS = "Destroy password error";
                break;
            case -19987:
                STATUS = "destroy label error";
                break;
            case -19986:
                STATUS = "set permission error";
                break;
            case -19972:
                STATUS = "MAC firmware received error packet";
                break;
            case -9999:
                STATUS = "The RF reader has been turned on and is in operation";
                break;
            case -9998:
                STATUS = "The provided memory space is too small";
                break;
            case -9997:
                STATUS = "general error";
                break;
            case -9996:
                STATUS = "bus driver loading error";
                break;
            case -9995:
                STATUS = "The bus driver version does not support";
                break;
            case -9994:
                STATUS = "reserve";
                break;
            case -9993:
                STATUS = "Antenna port error";
                break;
            case -9992:
                STATUS = "RF reader initialization failed";
                break;
            case -9991:
                STATUS = "parameter not available";
                break;
            case -9990:
                STATUS = "No RF reader available";
                break;
            case -9989:
                STATUS = "Uninitialized";
                break;
            case -9987:
                STATUS = "operation cancel";
                break;
            case -9985:
                STATUS = "The device is busy, the device is performing the previous operation";
                break;
            case -9984:
                STATUS = "RF reader error";
                break;
            case -9983:
                STATUS = "RF reader does not exist";
                break;
            case -9982:
                STATUS = "Class library function function is not available";
                break;
            case -9981:
                STATUS = "RF reader MAC firmware no response";
                break;
            case -9980:
                STATUS = "Cannot update non-volatile MAC registers";
                break;
            case -9979:
                STATUS = "Data cannot be written to the non-volatile MAC registers";
                break;
            case -9978:
                STATUS = "MAC component prevents writes to non-volatile registers";
                break;
            case -9977:
                STATUS = "Receive data overflow error";
                break;
            case -9976:
                STATUS = "MAC component returns wrong value";
                break;
            case 3:
                STATUS = "command failed";
                break;
            default:
                STATUS = errorCode + "";
                break;
        }
        return STATUS;
    }
}
