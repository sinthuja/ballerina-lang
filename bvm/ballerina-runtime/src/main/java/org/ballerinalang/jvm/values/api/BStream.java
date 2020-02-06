/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.jvm.values.api;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;

/**
 * <p>
 * The {@link BStream} represents a stream in Ballerina.
 * </p>
 *
 * @since 1.2.0
 */
public interface BStream extends BStreamIterator, BRefValue {
    /**
     * Returns the constrained {@code BType} of the stream.
     *
     * @return constrained type
     */
    BType getConstraintType();

    /**
     * Returns a stream which applies a filtering condition on the input stream.
     *
     * @param stream The input stream being filtered
     * @param functionPointer The function pointer which represents the filtering condition
     * @return The output stream
     */
    BStream filter(BStream stream, BFunctionPointer<Object, Boolean> functionPointer);

    /**
     * Returns a new stream which applies a mapping condition on the input stream.
     *
     * @param stream The input stream being mapped
     * @param functionPointer The function pointer which represents the mapping condition
     * @return The output stream
     */
    BStream map(BStream stream, BFunctionPointer<Object, Object> functionPointer);

    /**
     * Returns the next element in the stream after applying filters, mapping and reductions.
     *
     * @param strand The strand in which the filtering, mapping reduction... etc functions are invoked
     * @return The next element if the stream has or Nil if stream ends.
     */
    Object next(Strand strand);
}
