/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.string;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.langlib.string.utils.StringUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Extern function ballerina.model.strings:length.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.string",
        functionName = "length",
        args = {@Argument(name = "s", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.INT)},
        isPublic = true
)
public class Length {

    public static long length(Strand strand, String value) {
        StringUtils.checkForNull(value);
        return value.length();
    }

    public static long length_bstring(Strand strand, BString value) {
        if (value == null) {
            throw BallerinaErrors.createNullReferenceError();
        }
        return value.length();
    }
}
