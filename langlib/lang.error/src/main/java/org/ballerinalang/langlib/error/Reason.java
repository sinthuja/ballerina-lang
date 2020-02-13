/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/
package org.ballerinalang.langlib.error;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Get the reason phrase of an error value.
 *
 * @since 0.990.4
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.error",
        functionName = "reason",
        args = {@Argument(name = "value", type = TypeKind.ERROR)},
        returnType = {@ReturnType(type = TypeKind.STRING)})
public class Reason {


    @Deprecated
    public static String reason(Strand strand, ErrorValue value) {
        return value.getReason();
    }

    public static BString reason_bstring(Strand strand, ErrorValue value) {
        return value.getErrorReason();
    }
}
