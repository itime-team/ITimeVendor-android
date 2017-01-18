package org.unimelb.itime.vendor.wrapper;

import org.unimelb.itime.vendor.listener.ITimeEventInterface;

/**
 * Created by yuhaoliu on 4/01/2017.
 */

public class WrapperEvent {
    private ITimeEventInterface event;
    private String vendorEventUid;
    private boolean isAnimated;


    public WrapperEvent(ITimeEventInterface event) {
        this.event = event;
    }

    public ITimeEventInterface getEvent() {
        return event;
    }

    public void setEvent(ITimeEventInterface event) {
        this.event = event;
    }

    public String getVendorEventUid() {
        return vendorEventUid;
    }

    public void setVendorEventUid(String vendorEventUid) {
        this.vendorEventUid = vendorEventUid;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        isAnimated = animated;
    }
}
