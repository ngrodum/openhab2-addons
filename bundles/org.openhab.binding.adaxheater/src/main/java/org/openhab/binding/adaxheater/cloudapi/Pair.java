package org.openhab.binding.adaxheater.cloudapi;

import java.util.Objects;

/**
 * Created by ngrodum on 19/12/16.
 */
public class Pair<A, B> {
    public final A first;
    public final B second;

    public Pair(A var1, B var2) {
        this.first = var1;
        this.second = var2;
    }

    public String toString() {
        return "Pair[" + this.first + "," + this.second + "]";
    }

    public boolean equals(Object var1) {
        return var1 instanceof Pair && Objects.equals(this.first, ((Pair)var1).first) && Objects.equals(this.second, ((Pair)var1).second);
    }

    public int hashCode() {
        return this.first == null?(this.second == null?0:this.second.hashCode() + 1):(this.second == null?this.first.hashCode() + 2:this.first.hashCode() * 17 + this.second.hashCode());
    }

    public static <A, B> Pair<A, B> of(A var0, B var1) {
        return new Pair(var0, var1);
    }
}
