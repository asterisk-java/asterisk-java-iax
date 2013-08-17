
package org.asteriskjava.iax.protocol;

public interface CallManager {
    /**
     * Returns if we can deal with this call or not. Pretends to be a
     * small PBX. Calls can be rejected if (for example) there isn't a
     * telephone plugged into this extension.
     */
    public boolean accept(Call ca);
}
