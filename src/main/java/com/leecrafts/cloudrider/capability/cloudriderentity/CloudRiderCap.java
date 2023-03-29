package com.leecrafts.cloudrider.capability.cloudriderentity;

public class CloudRiderCap implements ICloudRiderCap {

    public int playerId;
    public boolean wasPersistent;

    public CloudRiderCap() {
        this.playerId = -1;
        this.wasPersistent = false;
    }

}
