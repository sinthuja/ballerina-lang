/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.model.tree.statements;

import org.ballerinalang.model.clauses.DoClauseNode;
import org.ballerinalang.model.clauses.FromClauseNode;
import org.ballerinalang.model.clauses.WhereClauseNode;

import java.util.List;

/**
 * Represents the DoAction statement node.
 *
 * @since 1.2.0
 */
public interface QueryActionNode extends StatementNode {

    List<? extends FromClauseNode> getFromClauseNodes();

    void addFromClauseNode(FromClauseNode fromClauseNode);

    List<? extends WhereClauseNode> getWhereClauseNode();

    void addWhereClauseNode(WhereClauseNode whereClauseNode);

    DoClauseNode getDoClauseNode();

    void setDoClauseNode(DoClauseNode doClauseNode);
}
