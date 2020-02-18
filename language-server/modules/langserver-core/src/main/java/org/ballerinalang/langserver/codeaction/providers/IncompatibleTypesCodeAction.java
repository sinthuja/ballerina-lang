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
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.types.TypeKind;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;

/**
 * Code Action provider for incompatible types.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class IncompatibleTypesCodeAction extends AbstractCodeActionProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                           List<Diagnostic> diagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        WorkspaceDocumentManager documentManager = lsContext.get(CodeActionKeys.DOCUMENT_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(lsContext.get(CodeActionKeys.FILE_URI_KEY));
        LSDocumentIdentifier document = null;
        try {
            document = documentManager.getLSDocument(filePath.get());
        } catch (WorkspaceDocumentException e) {
            // ignore
        }

        if (document == null) {
            return actions;
        }

        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.getMessage().toLowerCase(Locale.ROOT).contains(CommandConstants.INCOMPATIBLE_TYPES)) {
                CodeAction codeAction = getIncompatibleTypesCommand(document, diagnostic, lsContext);
                if (codeAction != null) {
                    actions.add(codeAction);
                }
            }
        }
        return actions;
    }

    private static CodeAction getIncompatibleTypesCommand(LSDocumentIdentifier document, Diagnostic diagnostic,
                                                          LSContext context) {
        String diagnosticMessage = diagnostic.getMessage();
        Position position = diagnostic.getRange().getStart();
        int line = position.getLine();
        int column = position.getCharacter();
        String uri = context.get(CodeActionKeys.FILE_URI_KEY);
        List<Diagnostic> diagnostics = new ArrayList<>();

        Matcher matcher = CommandConstants.INCOMPATIBLE_TYPE_PATTERN.matcher(diagnosticMessage);
        if (matcher.find() && matcher.groupCount() > 1) {
            String foundType = matcher.group(2);
            WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
            try {
                BLangFunction func = getFunctionNode(line, column, document, documentManager, context);
                if (func != null && !BLangConstants.MAIN_FUNCTION_NAME.equals(func.name.value)) {
                    BLangStatement statement = getStatementByLocation(((BLangBlockFunctionBody) func.body).stmts,
                                                                      line + 1, column + 1);
                    if (statement instanceof BLangReturn) {
                        // Process full-qualified BType name  eg. ballerina/http:Client and if required; add
                        // auto-import
                        matcher = CommandConstants.FQ_TYPE_PATTERN.matcher(foundType);
                        List<TextEdit> edits = new ArrayList<>();
                        String editText = extractTypeName(context, matcher, foundType, edits);

                        // Process function node
                        Position start;
                        Position end;
                        if (func.returnTypeNode instanceof BLangValueType
                                && TypeKind.NIL.equals(((BLangValueType) func.returnTypeNode).getTypeKind())
                                && func.returnTypeNode.getWS() == null) {
                            // eg. function test() {...}
                            start = new Position(func.returnTypeNode.pos.sLine - 1,
                                                 func.returnTypeNode.pos.eCol - 1);
                            end = new Position(func.returnTypeNode.pos.eLine - 1, func.returnTypeNode.pos.eCol - 1);
                            editText = " returns (" + editText + ")";
                        } else {
                            // eg. function test() returns () {...}
                            start = new Position(func.returnTypeNode.pos.sLine - 1,
                                                 func.returnTypeNode.pos.sCol - 1);
                            end = new Position(func.returnTypeNode.pos.eLine - 1, func.returnTypeNode.pos.eCol - 1);
                        }
                        edits.add(new TextEdit(new Range(start, end), editText));

                        // Add code action
                        String commandTitle = CommandConstants.CHANGE_RETURN_TYPE_TITLE + foundType + "'";
                        CodeAction action = new CodeAction(commandTitle);
                        action.setKind(CodeActionKind.QuickFix);
                        action.setDiagnostics(diagnostics);
                        action.setEdit(new WorkspaceEdit(Collections.singletonList(
                                Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null),
                                                                    edits))))
                        );
                        return action;
                    }
                }
            } catch (CompilationFailedException e) {
                // ignore
            }
        }
        return null;
    }

    private static BLangFunction getFunctionNode(int line, int column, LSDocumentIdentifier document,
                                                 WorkspaceDocumentManager docManager, LSContext context)
            throws CompilationFailedException {
        String uri = document.getURIString();
        Position position = new Position();
        position.setLine(line);
        position.setCharacter(column + 1);
        context.put(DocumentServiceKeys.FILE_URI_KEY, uri);
        TextDocumentIdentifier identifier = new TextDocumentIdentifier(uri);
        context.put(DocumentServiceKeys.POSITION_KEY, new TextDocumentPositionParams(identifier, position));
        LSModuleCompiler.getBLangPackages(context, docManager, LSCustomErrorStrategy.class, true, false, false);

        // Get the current package.
        BLangPackage currentPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);

        // If package is testable package process as tests
        // else process normally
        String relativeFilePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangCompilationUnit compilationUnit;
        if (relativeFilePath.startsWith("tests" + File.separator)) {
            compilationUnit = currentPackage.getTestablePkg().getCompilationUnits().stream().
                    filter(compUnit -> (relativeFilePath).equals(compUnit.getName()))
                    .findFirst().orElse(null);
        } else {
            compilationUnit = currentPackage.getCompilationUnits().stream().
                    filter(compUnit -> relativeFilePath.equals(compUnit.getName())).findFirst().orElse(null);
        }
        if (compilationUnit == null) {
            return null;
        }
        Iterator<TopLevelNode> nodeIterator = compilationUnit.getTopLevelNodes().iterator();
        BLangFunction result = null;
        TopLevelNode next = (nodeIterator.hasNext()) ? nodeIterator.next() : null;
        Function<org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition, Boolean> isWithinPosition =
                diagnosticPosition -> {
                    int sLine = diagnosticPosition.getStartLine();
                    int eLine = diagnosticPosition.getEndLine();
                    int sCol = diagnosticPosition.getStartColumn();
                    int eCol = diagnosticPosition.getEndColumn();
                    return ((line > sLine || (line == sLine && column >= sCol)) &&
                            (line < eLine || (line == eLine && column <= eCol)));
                };
        while (next != null) {
            if (isWithinPosition.apply(next.getPosition())) {
                if (next instanceof BLangFunction) {
                    result = (BLangFunction) next;
                    break;
                } else if (next instanceof BLangTypeDefinition) {
                    BLangTypeDefinition typeDefinition = (BLangTypeDefinition) next;
                    if (typeDefinition.typeNode instanceof BLangObjectTypeNode) {
                        BLangObjectTypeNode typeNode = (BLangObjectTypeNode) typeDefinition.typeNode;
                        for (BLangFunction function : typeNode.functions) {
                            if (isWithinPosition.apply(function.getPosition())) {
                                result = function;
                                break;
                            }
                        }
                    }
                } else if (next instanceof BLangService) {
                    BLangService bLangService = (BLangService) next;
                    for (BLangFunction function : bLangService.resourceFunctions) {
                        if (isWithinPosition.apply(function.getPosition())) {
                            result = function;
                            break;
                        }
                    }
                }
                break;
            }
            next = (nodeIterator.hasNext()) ? nodeIterator.next() : null;
        }
        return result;
    }

    /**
     * Find statement by location.
     *
     * @param statements list of statements
     * @param line       line
     * @param column     column
     * @return {@link BLangStatement} if found, NULL otherwise
     */
    private static BLangStatement getStatementByLocation(List<BLangStatement> statements, int line, int column) {
        BLangStatement node = null;
        for (BLangStatement statement : statements) {
            BLangStatement rv;
            if ((rv = getStatementByLocation(statement, line, column)) != null) {
                return rv;
            }
        }
        return node;
    }

    /**
     * Find statements by location.
     *
     * @param node   lookup {@link BLangNode}
     * @param line   line
     * @param column column
     * @return {@link BLangStatement} if found, NULL otherwise
     */
    private static BLangStatement getStatementByLocation(BLangNode node, int line, int column) {
        try {
            if (checkNodeWithin(node, line, column) && node instanceof BLangStatement) {
                return (BLangStatement) node;
            }
            for (Field field : node.getClass().getDeclaredFields()) {
                Object obj = field.get(node);
                if (obj instanceof BLangBlockStmt) {
                    // Found a block-statement field, drilling down further
                    BLangStatement rv;
                    if ((rv = getStatementByLocation(((BLangBlockStmt) obj).getStatements(), line, column)) != null) {
                        return rv;
                    }
                } else if (obj instanceof BLangStatement) {
                    if (checkNodeWithin((BLangStatement) obj, line, column)) {
                        return (BLangStatement) obj;
                    }
                    // Found a statement field, drilling down further
                    BLangStatement rv;
                    if ((rv = getStatementByLocation((BLangStatement) obj, line, column)) != null) {
                        return rv;
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return null;
        }
        return null;
    }


    private static boolean checkNodeWithin(BLangNode node, int line, int column) {
        int sLine = node.getPosition().getStartLine();
        int eLine = node.getPosition().getEndLine();
        int sCol = node.getPosition().getStartColumn();
        int eCol = node.getPosition().getEndColumn();
        return (line > sLine || (line == sLine && column >= sCol)) &&
                (line < eLine || (line == eLine && column <= eCol));
    }

    private static String extractTypeName(LSContext context, Matcher matcher, String foundType, List<TextEdit> edits) {
        if (matcher.find() && matcher.groupCount() > 2) {
            String orgName = matcher.group(1);
            String alias = matcher.group(2);
            String typeName = matcher.group(3);
            String pkgId = orgName + "/" + alias;
            PackageID currentPkgId = context.get(
                    DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY).packageID;
            if (pkgId.equals(currentPkgId.toString())) {
                foundType = typeName;
            } else {
                List<BLangImportPackage> currentDocImports = CommonUtil.getCurrentFileImports(context);
                boolean pkgAlreadyImported = currentDocImports.stream()
                        .anyMatch(importPkg -> importPkg.orgName.value.equals(orgName)
                                && importPkg.alias.value.equals(alias));
                if (!pkgAlreadyImported) {
                    edits.addAll(CommonUtil.getAutoImportTextEdits(orgName, alias, context));
                }
                foundType = alias + CommonKeys.PKG_DELIMITER_KEYWORD + typeName;
            }
        }
        return foundType;
    }
}
