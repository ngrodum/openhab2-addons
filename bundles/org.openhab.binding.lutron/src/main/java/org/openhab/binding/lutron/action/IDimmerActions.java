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
package org.openhab.binding.lutron.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link IDimmerActions} interface defines the interface for all thing actions supported by the dimmer thing.
 * This is only necessary to work around a bug in openhab-core (issue #1536). It should be removed once that is
 * resolved.
 *
 * @author Bob Adair - Initial contribution
 *
 */
@NonNullByDefault
public interface IDimmerActions {

    public void setLevel(@Nullable Double level, @Nullable Double fadeTime, @Nullable Double delayTime);
}
