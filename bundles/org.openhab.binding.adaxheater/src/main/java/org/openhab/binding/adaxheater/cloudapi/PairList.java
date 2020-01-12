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

import java.util.ArrayList;

/**
 * The {@link PairList} stores a pair as a list
 *
 * @author Nicolai Grodum - Initial contribution 27/03/2017
 */
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
