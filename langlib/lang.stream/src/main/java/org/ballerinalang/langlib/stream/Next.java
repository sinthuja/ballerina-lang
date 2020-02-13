/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.stream;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native implementation of lang.stream:next(stream&lt;Type&gt;).
 *
 * @since 1.2
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.stream", functionName = "next",
        args = {@Argument(name = "strm", type = TypeKind.STREAM)},
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class Next {
    public static Object next(Strand strand, StreamValue strm) {
        Object next = strm.next();
        if (next == null) {
            return null;
        }

        return BValueCreator.createRecord(new MapValueImpl<>(strm.getIteratorNextReturnType()), next);
    }
}
