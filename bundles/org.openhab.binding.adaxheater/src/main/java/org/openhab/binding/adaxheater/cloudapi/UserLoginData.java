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
 * The {@link UserLoginData} stores login data
 *
 * @author Nicolai Grodum - Initial contribution 27/03/2017
 */
public class UserLoginData {
    private Long id;
    private String name;
    private String privateKey;

    public UserLoginData(Long id, String name, String privateKey) {
        this.id = id;
        this.name = name;
        this.privateKey = privateKey;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPrivateKey() {
        return this.privateKey;
    }
}
