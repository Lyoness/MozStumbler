/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.mozstumbler.svclocator;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class ServiceConfig extends HashMap<Class<?>, Object> {

    private static final long serialVersionUID = 1111111111L;

    public static Object load(String className, Object... construct_varargs) {
        Class<?> c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading [" + className + "] class");
        }
        Constructor[] constructors = c.getConstructors();

        Constructor<?> myConstructor = null;
        for (Constructor<?> construct : constructors) {
            if (construct.getParameterTypes().length == construct_varargs.length) {
                myConstructor = construct;
                break;
            }
        }

        if (myConstructor == null) {
            throw new RuntimeException("No constructor found");
        }

        try {
            if (construct_varargs.length == 1) {
                // We allow passing in a context
                return myConstructor.newInstance(construct_varargs[0]);
            } else if (construct_varargs.length > 1) {
                throw new RuntimeException("Too many arguments passed into service loader");
            }
            return myConstructor.newInstance();

        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }



}