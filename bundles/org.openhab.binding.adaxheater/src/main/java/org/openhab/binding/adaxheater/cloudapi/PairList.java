package org.openhab.binding.adaxheater.cloudapi;

import java.util.ArrayList;

public class PairList<T1, T2> extends ArrayList<Pair<T1, T2>>
{
    public PairList() {
    }

    public PairList(final int n) {
        super(n);
    }

    public void add(final T1 t1, final T2 t2) {
        this.add(Pair.of(t1, t2));
    }
}
