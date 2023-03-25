package com.leecrafts.cloudrider.capability.cloudriderentity;

public class CloudRiderCap implements ICloudRiderCap {

    public int playerId;
    public boolean wasNotPersistent;

    public CloudRiderCap() {
        this.playerId = -1;
        this.wasNotPersistent = true;
    }

}
