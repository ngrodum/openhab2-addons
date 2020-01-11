/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.adaxheater.cloudapi;

import java.util.Objects;

/**
 * The {@link Pair} contains a pair
 *
 * @author Nicolai Grodum - Initial contribution 27/03/2017
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
