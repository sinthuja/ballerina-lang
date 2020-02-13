/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction.providers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.executors.AddDocumentationExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Code Action provider for adding single documentation.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddDocumentationCodeAction extends AbstractCodeActionProvider {
    public AddDocumentationCodeAction() {
        super(Arrays.asList(CodeActionNodeType.FUNCTION,
                            CodeActionNodeType.OBJECT,
                            CodeActionNodeType.SERVICE,
                            CodeActionNodeType.RESOURCE,
                            CodeActionNodeType.RECORD,
                            CodeActionNodeType.OBJECT_FUNCTION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                           List<org.eclipse.lsp4j.Diagnostic> diagnostics) {
        String docUri = lsContext.get(CodeActionKeys.FILE_URI_KEY);
        int line = lsContext.get(CodeActionKeys.POSITION_START_KEY).getLine();

        CommandArgument nodeTypeArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_TYPE, nodeType.name());
        CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri);
        CommandArgument lineStart = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, String.valueOf(line));
        List<Object> args = new ArrayList<>(Arrays.asList(nodeTypeArg, docUriArg, lineStart));

        CodeAction action = new CodeAction(CommandConstants.ADD_DOCUMENTATION_TITLE);
        Command command = new Command(CommandConstants.ADD_DOCUMENTATION_TITLE, AddDocumentationExecutor.COMMAND, args);
        action.setCommand(command);
        return Collections.singletonList(action);
    }
}
