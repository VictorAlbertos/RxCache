package io.rx_cache;

/**
 * Created by victor on 26/02/16.
 */
public class DynamicKeyGroup extends DynamicKey {
    private final Object group;

    public DynamicKeyGroup(Object key, Object group) {
        super(key);
        this.group = group;
    }

    public Object getGroup() {
        return group;
    }
}
