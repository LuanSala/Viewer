package lsa.viewercloudpoints.bluetooth;

/**
 * Created by Luan Sala on 26/02/2016.
 */
public class BondedDevice {

    public BondedDevice(String name, String address){
        deviceName = name;
        deviceAddress = address;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    private String deviceName;
    private String deviceAddress;

}
