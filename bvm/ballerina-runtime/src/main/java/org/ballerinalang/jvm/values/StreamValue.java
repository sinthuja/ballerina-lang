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

package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.IteratorUtils;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.api.BFunctionPointer;
import org.ballerinalang.jvm.values.api.BIterator;
import org.ballerinalang.jvm.values.api.BStream;
import org.ballerinalang.jvm.values.api.BString;

import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * The {@link StreamValue} represents a stream in Ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *
 * @since 1.2.0
 */
public class StreamValue implements RefValue, BStream {

    private BType type;
    private BType constraintType;
    private BType iteratorNextReturnType;
    private BIterator<Object> iterator;
    public FunctionPointerWrapper<Boolean, Object> filter;
    public FunctionPointerWrapper<Object, Object> mapper;


    /**
     * The name of the underlying broker topic representing the stream object.
     */
    public String streamId;

    @Deprecated
    public StreamValue(BType type) {
        this.constraintType = ((BStreamType) type).getConstrainedType();
        this.type = new BStreamType(constraintType);
        this.streamId = UUID.randomUUID().toString();
        this.iterator = null;
        this.filter = new NoFilterFunctionPointerWrapper();
        this.mapper = new NoMapFunctionPointerWrapper();
    }

    public StreamValue(BType type, BIterator iterator, BFunctionPointer<Object, Boolean> filterFunc,
                       BFunctionPointer<Object, Object> mapFunc) {
        this.constraintType = ((BStreamType) type).getConstrainedType();
        this.type = new BStreamType(constraintType);
        this.streamId = UUID.randomUUID().toString();
        this.iterator = iterator;

        if (filterFunc != null) {
            this.filter = new FilterFunctionPointerWrapper(filterFunc);
        } else {
            this.filter = new NoFilterFunctionPointerWrapper();
        }

        if (mapFunc != null) {
            this.mapper = new MapFunctionPointerWrapper(mapFunc);
        } else {
            this.mapper = new NoMapFunctionPointerWrapper();
        }
    }

    public StreamValue(BStream sourceStream, BFunctionPointer<Object, Boolean> filterFunc,
                       BFunctionPointer<Object, Object> mapFunc) {
        this(sourceStream.getType(), sourceStream, filterFunc, mapFunc);
    }

    public String getStreamId() {
        return streamId;
    }

    public BType getIteratorNextReturnType() {
        if (iteratorNextReturnType == null) {
            iteratorNextReturnType = IteratorUtils.createIteratorNextReturnType(constraintType);
        }

        return iteratorNextReturnType;
    }

    /**
     * {@inheritDoc}
     */
    public String stringValue() {
        return "stream <" + getType().toString() + ">";
    }

    @Override
    public BString bStringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return this.type;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public Object frozenCopy(Map<Object, Object> refs) {
        throw new UnsupportedOperationException();
    }

    public BType getConstraintType() {
        return constraintType;
    }

    @Override
    public BStream filter(BStream stream, BFunctionPointer<Object, Boolean> filterFunc) {
        return new StreamValue(stream, filterFunc, null);
    }

    @Override
    public BStream map(BStream stream, BFunctionPointer<Object, Object> mapFunc) {
        return new StreamValue(stream, null, mapFunc);
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Object next() {
        Object next;
        do {
            next = iterator.next();
            if (next == null) {
                return null;
            }
        } while (!filter.execute(next));

        return mapper.execute(next);
    }

    @Override
    public String toString() {
        return stringValue();
    }

    interface FunctionPointerWrapper<T, R> {
        T execute(R element);
    }

    static class NoFilterFunctionPointerWrapper implements FunctionPointerWrapper<Boolean, Object> {

        @Override
        public Boolean execute(Object element) {
            return true;
        }
    }

    static class FilterFunctionPointerWrapper implements FunctionPointerWrapper<Boolean, Object> {
        private BFunctionPointer<Object, Boolean> filterFunc;

        FilterFunctionPointerWrapper(BFunctionPointer<Object, Boolean> filterFunc) {
            this.filterFunc = filterFunc;
        }

        @Override
        public Boolean execute(Object element) {
            //TODO: use scheduler to invoke the filterFunc
            return filterFunc.call(new Object[]{Scheduler.getStrand(), element, true});
        }
    }

    static class NoMapFunctionPointerWrapper implements FunctionPointerWrapper<Object, Object> {

        @Override
        public Object execute(Object element) {
            return element;
        }
    }

    static class MapFunctionPointerWrapper implements FunctionPointerWrapper<Object, Object> {
        private BFunctionPointer<Object, Object> mapFunc;

        MapFunctionPointerWrapper(BFunctionPointer<Object, Object> mapFunc) {
            this.mapFunc = mapFunc;
        }

        @Override
        public Object execute(Object element) {
            //TODO: use scheduler to invoke the mapFunc
            return mapFunc.call(new Object[]{Scheduler.getStrand(), element, true});
        }
    }
}
