
package org.asteriskjava.iax.protocol;

/**
 * Minimal events sent by the protocol engine.
 */
public interface ProtocolEventListener {
    public void newCall(Call c);

    public void registered(Friend f, boolean s);

    public void hungUp(Call c);

    public void ringing(Call c);

    public void answered(Call c);

    public void setHostReachable(Friend f, boolean b, int roundtrip);
}

