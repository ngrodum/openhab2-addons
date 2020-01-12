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

/**
 * The {@link ClientErrorHolder} stores client error
 *
 * @author Nicolai Grodum - Initial contribution 27/03/2017
 */
public class ClientErrorHolder {
    private long errorTypeId;

    public ClientErrorHolder(long errorTypeId) {
        this.errorTypeId = errorTypeId;
    }

    public ClientErrorHolder(ClientErrorType errorTypeId) {
        this.errorTypeId = errorTypeId.getId();
    }

    public ClientErrorHolder() {
        this(0);
    }

    public void set(ClientErrorHolder other) {
        setErrorTypeId(other != null ? other.getErrorTypeId() : 0);
    }

    public void setErrorTypeId(long errorTypeId) {
        this.errorTypeId = errorTypeId;
    }

    public long getErrorTypeId() {
        return this.errorTypeId;
    }
}
