/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.mrxu.sql.translate;

import com.mrxu.sql.connector.SqlElasticRequestBuilder;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;

/**
 * Created by Eliran on 19/8/2015.
 */
public class SqlElasticSearchRequestBuilder implements SqlElasticRequestBuilder {
    ActionRequestBuilder requestBuilder;

    public SqlElasticSearchRequestBuilder(ActionRequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    @Override
    public ActionRequest request() {
        return requestBuilder.request();
    }

    @Override
    public String explain() {
        return requestBuilder.toString();
    }

    @Override
    public ActionResponse get() {
        return requestBuilder.get();
    }

    @Override
    public ActionRequestBuilder getBuilder() {
        return requestBuilder;
    }

    @Override
    public String toString() {
        return this.requestBuilder.toString();
    }
}
