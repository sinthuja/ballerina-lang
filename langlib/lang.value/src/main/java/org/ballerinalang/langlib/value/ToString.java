/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.ballerinalang.langlib.value;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.utils.StringUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Returns a simple, human-readable representation of the given value as a String.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.value",
        functionName = "toString",
        args = {@Argument(name = "value", type = TypeKind.ANY)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ToString {
    public static String toString(Strand strand, Object value) {
        return StringUtils.getStringValue(value);
    }

    public static BString toString_bstring(Strand strand, Object value) {
        return StringUtils.getBStringValue(value);
    }
}
