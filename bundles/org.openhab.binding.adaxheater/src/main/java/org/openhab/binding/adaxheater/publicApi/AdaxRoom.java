package org.openhab.binding.adaxheater.publicApi;

public class AdaxRoom {
    public int id;
    public String homeId;
    public String name;
    public Boolean heatingEnabled;
    public Integer targetTemperature;
    public Integer temperature;

    public int getRoomId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getCurrentTemperature() {
        return temperature;
    }

    public Integer getTargetTemperature() {
        return targetTemperature;
    }
}
