/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.openapi.validator;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Validator plugin utility class to hold the base functionality.
 */
class ValidatorUtil {
    /**
     * Parse and get the {@link OpenAPI} for the given OpenAPI contract.
     *
     * @param definitionURI URI for the OpenAPI contract
     * @return {@link OpenAPI} OpenAPI model
     * @throws OpenApiValidatorException in case of exception
     */
    static OpenAPI parseOpenAPIFile(String definitionURI) throws OpenApiValidatorException {
        Path contractPath = Paths.get(definitionURI);
        if (!Files.exists(contractPath)) {
            throw new OpenApiValidatorException(ErrorMessages.invalidFilePath(definitionURI));
        }

        if (!(definitionURI.endsWith(".yaml") || definitionURI.endsWith(".json"))) {
            throw new OpenApiValidatorException(ErrorMessages.invalidFile());
        }

        OpenAPI api = new OpenAPIV3Parser().read(definitionURI);
        if (api == null) {
            throw new OpenApiValidatorException(ErrorMessages.parserException(definitionURI));
        }

        return api;
    }

    /**
     * Summarize openAPI contract paths to easily access details to validate.
     *
     * @param openAPISummaries        list of openAPI path summaries
     * @param contract                openAPI contract
     * @param openAPIComponentSummary list of openAPI components
     */
    static void summarizeOpenAPI(List<OpenAPIPathSummary> openAPISummaries, OpenAPI contract
            , OpenAPIComponentSummary openAPIComponentSummary) {
        io.swagger.v3.oas.models.Paths paths = contract.getPaths();
        for (Map.Entry pathItem : paths.entrySet()) {
            OpenAPIPathSummary openAPISummary = new OpenAPIPathSummary();
            if (pathItem.getKey() instanceof String
                    && pathItem.getValue() instanceof PathItem) {
                String key = (String) pathItem.getKey();
                openAPISummary.setPath(key);

                PathItem operations = (PathItem) pathItem.getValue();
                if (operations.getGet() != null) {
                    openAPISummary.addAvailableOperation(Constants.GET);
                    openAPISummary.addOperation(Constants.GET, operations.getGet());
                }

                if (operations.getPost() != null) {
                    openAPISummary.addAvailableOperation(Constants.POST);
                    openAPISummary.addOperation(Constants.POST, operations.getPost());
                }

                if (operations.getPut() != null) {
                    openAPISummary.addAvailableOperation(Constants.PUT);
                    openAPISummary.addOperation(Constants.PUT, operations.getPut());
                }

                if (operations.getDelete() != null) {
                    openAPISummary.addAvailableOperation(Constants.DELETE);
                    openAPISummary.addOperation(Constants.DELETE, operations.getDelete());
                }

                if (operations.getHead() != null) {
                    openAPISummary.addAvailableOperation(Constants.HEAD);
                    openAPISummary.addOperation(Constants.HEAD, operations.getHead());
                }

                if (operations.getPatch() != null) {
                    openAPISummary.addAvailableOperation(Constants.PATCH);
                    openAPISummary.addOperation(Constants.PATCH, operations.getPatch());
                }

                if (operations.getOptions() != null) {
                    openAPISummary.addAvailableOperation(Constants.OPTIONS);
                    openAPISummary.addOperation(Constants.OPTIONS, operations.getOptions());
                }

                if (operations.getTrace() != null) {
                    openAPISummary.addAvailableOperation(Constants.TRACE);
                    openAPISummary.addOperation(Constants.TRACE, operations.getTrace());
                }
            }

            openAPISummaries.add(openAPISummary);
        }

        openAPIComponentSummary.setComponents(contract.getComponents());
    }

    /**
     * Extract the details to be validated from the resource.
     *
     * @param resourceSummaryList list of resource summaries
     * @param serviceNode         service node
     */
    static void summarizeResources(List<ResourceSummary> resourceSummaryList, ServiceNode serviceNode) {
        // Iterate resources available in a service and extract details to be validated.
        for (FunctionNode resource : serviceNode.getResources()) {
            AnnotationAttachmentNode annotation = null;
            ResourceSummary resourceSummary = new ResourceSummary();
            resourceSummary.setResourcePosition(resource.getPosition());
            // Find the "ResourceConfig" annotation.
            for (AnnotationAttachmentNode ann : resource.getAnnotationAttachments()) {
                if (Constants.HTTP.equals(ann.getPackageAlias().getValue())
                        && Constants.RESOURCE_CONFIG.equals(ann.getAnnotationName().getValue())) {
                    annotation = ann;
                }
            }

            if (annotation != null) {
                if (annotation.getExpression() instanceof BLangRecordLiteral) {
                    BLangRecordLiteral recordLiteral = (BLangRecordLiteral) annotation.getExpression();
                    for (BLangRecordLiteral.RecordField field : recordLiteral.getFields()) {
                        BLangExpression keyExpr;
                        BLangExpression valueExpr;

                        if (field.isKeyValueField()) {
                            BLangRecordLiteral.BLangRecordKeyValueField keyValue =
                                    (BLangRecordLiteral.BLangRecordKeyValueField) field;
                            keyExpr = keyValue.getKey();
                            valueExpr = keyValue.getValue();
                        } else {
                            BLangRecordLiteral.BLangRecordVarNameField varNameField =
                                    (BLangRecordLiteral.BLangRecordVarNameField) field;
                            keyExpr = varNameField;
                            valueExpr = varNameField;
                        }

                        if (keyExpr instanceof BLangSimpleVarRef) {
                            BLangSimpleVarRef path = (BLangSimpleVarRef) keyExpr;
                            String contractAttr = path.getVariableName().getValue();
                            // Extract the path and methods of the resource.
                            if (contractAttr.equals(Constants.PATH)) {
                                if (valueExpr instanceof BLangLiteral) {
                                    BLangLiteral value = (BLangLiteral) valueExpr;
                                    if (value.getValue() instanceof String) {
                                        resourceSummary.setPath((String) value.getValue());
                                        resourceSummary.setPathPosition(value.getPosition());
                                    }
                                }
                            } else if (contractAttr.equals(Constants.METHODS)) {
                                if (valueExpr instanceof BLangListConstructorExpr) {
                                    BLangListConstructorExpr methodSet = (BLangListConstructorExpr) valueExpr;
                                    for (BLangExpression methodExpr : methodSet.exprs) {
                                        if (methodExpr instanceof BLangLiteral) {
                                            BLangLiteral method = (BLangLiteral) methodExpr;
                                            resourceSummary.addMethod(((String) method.value)
                                                    .toLowerCase(Locale.ENGLISH));
                                            resourceSummary.setMethodsPosition(methodSet.getPosition());
                                        }
                                    }
                                }
                            } else if (contractAttr.equals(Constants.BODY)) {
                                if (valueExpr instanceof BLangLiteral) {
                                    BLangLiteral value = (BLangLiteral) valueExpr;
                                    if (value.getValue() instanceof String) {
                                        resourceSummary.setBody((String) value.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Extract and add the resource parameters
            if (resource.getParameters().size() > 0) {
                resourceSummary.setParameters(resource.getParameters());
            }

            // Add the resource summary to the resource summary list.
            resourceSummaryList.add(resourceSummary);
        }
    }

    /**
     * Validate ballerina resource against OpenAPI paths.
     *
     * @param tags                    lists of tags to enable validation on
     * @param operations              list of operations enable validation on
     * @param resourceSummaryList     list of resource summaries
     * @param openAPISummaryList      list of openAPI path summaries
     * @param openAPIComponentSummary component summaries
     * @param dLog                    diagnostic logger
     */
    static void validateResourcesAgainstOpenApi(List<String> tags, List<String> operations,
                                                List<ResourceSummary> resourceSummaryList,
                                                List<OpenAPIPathSummary> openAPISummaryList,
                                                OpenAPIComponentSummary openAPIComponentSummary, DiagnosticLog dLog) {
        boolean tagFilteringEnabled = tags.size() > 0;
        boolean operationFilteringEnabled = operations.size() > 0;

        for (ResourceSummary resourceSummary : resourceSummaryList) {
            OpenAPIPathSummary openAPIPathSummary = getOpenApiSummaryByPath(resourceSummary.getPath(),
                    openAPISummaryList);
            if (openAPIPathSummary == null) {
                dLog.logDiagnostic(Diagnostic.Kind.ERROR, resourceSummary.getPathPosition(),
                        ErrorMessages.undocumentedResourcePath(resourceSummary.getPath()));
            } else {
                List<String> unmatchedMethods = new ArrayList<>();
                if (!operationFilteringEnabled && !tagFilteringEnabled) {
                    for (String resourceMethod : resourceSummary.getMethods()) {
                        boolean noMatch = true;
                        for (String method : openAPIPathSummary.getAvailableOperations()) {
                            if (method.equals(resourceMethod)) {
                                noMatch = false;
                                break;
                            }
                        }

                        if (noMatch) {
                            unmatchedMethods.add(resourceMethod);
                        }

                        List<OpenAPIParameter> operationParamNames = openAPIPathSummary
                                .getParamNamesForOperation(resourceMethod);
                        List<ResourceParameter> resourceParamNames = resourceSummary.getParamNames();
                        for (ResourceParameter parameter : resourceParamNames) {
                            boolean isExist = false;
                            for (OpenAPIParameter openAPIParameter : operationParamNames) {
                                if (parameter.getName().equals(resourceSummary.getBody())) {
                                    // TODO: Process request body.
                                    isExist = true;
                                } else if (openAPIParameter.isTypeAvailableAsRef()) {
                                    if (parameter.getName().equals(openAPIParameter.getName())) {
                                        isExist = true;
                                        Schema schema = openAPIComponentSummary
                                                .getSchema(openAPIParameter.getLocalRef());
                                        if (schema != null) {
                                            isExist = validateResourceAgainstOpenAPIParams(parameter,
                                                    parameter.getParameter().symbol, schema, dLog, resourceMethod,
                                                    resourceSummary.getPath());
                                        }
                                    }
                                } else if (openAPIParameter.getName().equals(parameter.getName())) {
                                    isExist = validateResourceAgainstOpenAPIParams(parameter,
                                            parameter.getParameter().symbol,
                                            openAPIParameter.getParameter().getSchema(), dLog, resourceMethod,
                                            resourceSummary.getPath());
                                }
                            }

                            if (!isExist) {
                                dLog.logDiagnostic(Diagnostic.Kind.ERROR, parameter.getParameter().getPosition(),
                                        ErrorMessages.undocumentedResourceParameter(parameter.getName(),
                                                resourceMethod, resourceSummary.getPath()));
                            }
                        }
                    }

                    String methods = getUnmatchedMethodList(unmatchedMethods);
                    if (!openAPIPathSummary.getAvailableOperations().containsAll(resourceSummary.getMethods())) {
                        dLog.logDiagnostic(Diagnostic.Kind.ERROR, resourceSummary.getMethodsPosition(),
                                ErrorMessages.undocumentedResourceMethods(methods, resourceSummary.getPath()));
                    }
                }
            }
        }
    }

    /**
     * Validate openAPI path, operations and components against Ballerina resource.
     *
     * @param serviceNode             ballerina service node
     * @param tags                    list of tags to enable validation on
     * @param operations              list of operations to enable validation on
     * @param resourceSummaryList     list of resource summaries
     * @param openAPISummaryList      list of openAPI path summaries
     * @param openAPIComponentSummary openAPI component summary
     * @param dLog                    diagnostic logger
     */
    static void validateOpenApiAgainstResources(ServiceNode serviceNode, List<String> tags, List<String> operations,
                                                List<ResourceSummary> resourceSummaryList,
                                                List<OpenAPIPathSummary> openAPISummaryList,
                                                OpenAPIComponentSummary openAPIComponentSummary,
                                                DiagnosticLog dLog) {
        boolean tagFilteringEnabled = tags.size() > 0;
        boolean operationFilteringEnabled = operations.size() > 0;

        for (OpenAPIPathSummary openApiSummary : openAPISummaryList) {
            List<ResourceSummary> resourceSummaries = getResourceSummaryByPath(openApiSummary.getPath(),
                    resourceSummaryList);
            if (resourceSummaries == null) {
                dLog.logDiagnostic(Diagnostic.Kind.ERROR, getServiceNamePosition(serviceNode),
                        ErrorMessages.unimplementedOpenAPIPath(openApiSummary.getPath()));
            } else {
                List<String> allAvailableResourceMethods = getAllMethodsInResourceSummaries(resourceSummaries);
                List<String> unmatchedMethods = new ArrayList<>();

                // If operation filtering available proceed.
                // Else proceed to check tag filtering.
                if (operationFilteringEnabled) {
                    // If tag filtering available validate only the filtered operations grouped by given tags.
                    // Else proceed only to validate filtered operations.
                    if (tagFilteringEnabled) {
                        for (String method : openApiSummary.getAvailableOperations()) {
                            if (operations.contains(method) && openApiSummary.hasTags(tags, method)) {
                                validateOperationForOpenAPI(unmatchedMethods, allAvailableResourceMethods,
                                        resourceSummaries, method, openApiSummary, dLog, openAPIComponentSummary,
                                        serviceNode);
                            }
                        }

                        if (unmatchedMethods.size() > 0) {
                            String methods = getUnmatchedMethodList(unmatchedMethods);
                            dLog.logDiagnostic(Diagnostic.Kind.ERROR, getServiceNamePosition(serviceNode),
                                    ErrorMessages.unimplementedOpenAPIOperationsForPath(methods,
                                            openApiSummary.getPath()));
                        }
                    } else {
                        for (String method : openApiSummary.getAvailableOperations()) {
                            if (operations.contains(method)) {
                                validateOperationForOpenAPI(unmatchedMethods, allAvailableResourceMethods,
                                        resourceSummaries, method, openApiSummary, dLog, openAPIComponentSummary,
                                        serviceNode);
                            }
                        }

                        if (unmatchedMethods.size() > 0) {
                            String methods = getUnmatchedMethodList(unmatchedMethods);
                            dLog.logDiagnostic(Diagnostic.Kind.ERROR, getServiceNamePosition(serviceNode),
                                    ErrorMessages.unimplementedOpenAPIOperationsForPath(methods,
                                            openApiSummary.getPath()));
                        }
                    }
                } else {
                    // If tag filtering available proceed to validate all the operations grouped by given tags.
                    // Else proceed only to validate filtered operations.
                    if (tagFilteringEnabled) {
                        for (String method : openApiSummary.getAvailableOperations()) {
                            if (openApiSummary.hasTags(tags, method)) {
                                validateOperationForOpenAPI(unmatchedMethods, allAvailableResourceMethods,
                                        resourceSummaries, method, openApiSummary, dLog, openAPIComponentSummary,
                                        serviceNode);
                            }
                        }

                        if (unmatchedMethods.size() > 0) {
                            String methods = getUnmatchedMethodList(unmatchedMethods);
                            dLog.logDiagnostic(Diagnostic.Kind.ERROR, getServiceNamePosition(serviceNode),
                                    ErrorMessages.unimplementedOpenAPIOperationsForPath(methods,
                                            openApiSummary.getPath()));
                        }
                    } else {
                        for (String method : openApiSummary.getAvailableOperations()) {
                            validateOperationForOpenAPI(unmatchedMethods, allAvailableResourceMethods,
                                    resourceSummaries, method, openApiSummary, dLog, openAPIComponentSummary,
                                    serviceNode);
                        }

                        String methods = getUnmatchedMethodList(unmatchedMethods);
                        if (!allAvailableResourceMethods.containsAll(openApiSummary.getAvailableOperations())) {
                            dLog.logDiagnostic(Diagnostic.Kind.ERROR, getServiceNamePosition(serviceNode),
                                    ErrorMessages.unimplementedOpenAPIOperationsForPath(methods,
                                            openApiSummary.getPath()));
                        }
                    }
                }
            }
        }
    }

    private static String getUnmatchedMethodList(List<String> unmatchedMethods) {
        StringBuilder methods = new StringBuilder();
        for (int i = 0; i < unmatchedMethods.size(); i++) {
            if (i == 0) {
                methods.append(unmatchedMethods.get(i));
            } else {
                methods.append(", ").append(unmatchedMethods.get(i));
            }
        }

        return methods.toString();
    }

    private static OpenAPIPathSummary getOpenApiSummaryByPath(String path,
                                                              List<OpenAPIPathSummary> openAPISummaryList) {
        OpenAPIPathSummary openAPISummary = null;
        for (OpenAPIPathSummary openAPI : openAPISummaryList) {
            if (openAPI.getPath().equals(path)) {
                openAPISummary = openAPI;
                break;
            }
        }
        return openAPISummary;
    }

    private static void validateOperationForOpenAPI(List<String> unmatchedMethods,
                                                    List<String> allAvailableResourceMethods,
                                                    List<ResourceSummary> resourceSummaries, String method,
                                                    OpenAPIPathSummary openApiSummary, DiagnosticLog dLog,
                                                    OpenAPIComponentSummary openApiComponentSummary,
                                                    ServiceNode serviceNode) {
        boolean noMatch = true;
        for (String resourceMethod : allAvailableResourceMethods) {
            if (resourceMethod.equals(method)) {
                noMatch = false;
                break;
            }
        }

        if (noMatch) {
            unmatchedMethods.add(method);
        }

        // Check for parameter mismatch.
        checkForParameterMismatch(openApiSummary, resourceSummaries, method, serviceNode, openApiComponentSummary,
                dLog);
    }

    private static void checkForParameterMismatch(OpenAPIPathSummary openApiSummary,
                                                  List<ResourceSummary> resourceSummaries,
                                                  String method, ServiceNode serviceNode,
                                                  OpenAPIComponentSummary openAPIComponentSummary,
                                                  DiagnosticLog dLog) {
        List<OpenAPIParameter> operationParamNames = openApiSummary
                .getParamNamesForOperation(method);
        ResourceSummary resourceSummaryForMethod = getResourceSummaryByMethod(resourceSummaries, method);
        if (resourceSummaryForMethod != null) {
            List<ResourceParameter> resourceParamNames = resourceSummaryForMethod.getParamNames();
            for (OpenAPIParameter openAPIParameter : operationParamNames) {
                boolean isExist = false;
                ResourceParameter nonExistingResourceParameter = null;
                for (ResourceParameter parameter : resourceParamNames) {
                    if (openAPIParameter.isTypeAvailableAsRef()) {
                        if (openAPIParameter.getName().equals(parameter.getName())) {
                            isExist = true;
                            Schema schema = openAPIComponentSummary.getSchema(openAPIParameter.getLocalRef());
                            if (schema != null) {
                                isExist = validateOpenAPIAgainResourceParams(parameter,
                                        parameter.getParameter().symbol, schema, dLog, method,
                                        openApiSummary.getPath());
                            }
                        }
                    } else if (openAPIParameter.getName().equals(parameter.getName())) {
                        isExist = validateOpenAPIAgainResourceParams(parameter, parameter.getParameter().symbol,
                                openAPIParameter.getParameter().getSchema(), dLog, method, openApiSummary.getPath());
                    }

                    if (!isExist) {
                        nonExistingResourceParameter = parameter;
                        break;
                    }
                }

                if (!isExist) {
                    if (nonExistingResourceParameter != null) {
                        dLog.logDiagnostic(Diagnostic.Kind.ERROR,
                                nonExistingResourceParameter.getParameter().getPosition(),
                                ErrorMessages.unimplementedParameterForOperation(openAPIParameter.getName(),
                                        method, resourceSummaryForMethod.getPath()));
                    } else {
                        dLog.logDiagnostic(Diagnostic.Kind.ERROR, getServiceNamePosition(serviceNode),
                                ErrorMessages.unimplementedParameterForOperation(openAPIParameter.getName(),
                                        method, resourceSummaryForMethod.getPath()));
                    }
                    break;
                }
            }
        }
    }

    private static boolean validateResourceAgainstOpenAPIParams(ResourceParameter resourceParameter,
                                                                BVarSymbol resourceParameterType, Schema openAPIParam,
                                                                DiagnosticLog dLog, String method, String path) {
        BType resourceParamType = resourceParameterType.getType();

        if (resourceParamType.getKind().typeName().equals("record")
                && resourceParamType instanceof BRecordType
                && openAPIParam instanceof ObjectSchema) {
            // Check the existence of the fields.
            Map<String, Schema> properties = ((ObjectSchema) openAPIParam).getProperties();
            BRecordType recordType = (BRecordType) resourceParamType;
            for (BField field : recordType.fields) {
                boolean isExist = false;
                for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                    if (entry.getKey().equals(field.name.getValue())
                            && field.getType().getKind().typeName()
                            .equals(ValidatorUtil.convertOpenAPITypeToBallerina(entry.getValue().getType()))) {
                        isExist = true;
                        if (ValidatorUtil.convertOpenAPITypeToBallerina(entry.getValue().getType()).equals("record")) {
                            isExist = validateResourceAgainstOpenAPIParams(resourceParameter,
                                    field.symbol, entry.getValue(), dLog, method, path);
                        }
                    }
                }

                if (!isExist) {
                    dLog.logDiagnostic(Diagnostic.Kind.ERROR, field.pos,
                            ErrorMessages.undocumentedFieldInRecordParam(field.name.getValue(),
                                    resourceParameter.getName(), method, path));
                }
            }
            return true;
        } else if (resourceParamType.getKind().typeName().equals("[]")
                && openAPIParam.getType().equals("array")) {
            // TODO: Implement array type handling.
            return true;
        } else if (resourceParamType.getKind().typeName().equals("string")
                && openAPIParam.getType().equals("string")) {
            return true;
        } else if (resourceParamType.getKind().typeName().equals("int")
                && openAPIParam.getType().equals("integer")) {
            return true;
        } else if (resourceParamType.getKind().typeName().equals("boolean")
                && openAPIParam.getType().equals("boolean")) {
            return true;
        } else if (resourceParamType.getKind().typeName().equals("decimal")
                && openAPIParam.getType().equals("number")) {
            return true;
        }
        return false;
    }

    private static boolean validateOpenAPIAgainResourceParams(ResourceParameter resourceParam,
                                                              BVarSymbol resourceParameterType,
                                                              Schema openAPIParam,
                                                              DiagnosticLog dLog, String operation, String path) {
        BType resourceParamType = resourceParameterType.getType();
        if (resourceParamType.getKind().typeName().equals("record")
                && resourceParamType instanceof BRecordType
                && openAPIParam instanceof ObjectSchema) {
            // Check the existence of the fields.
            Map<String, Schema> properties = ((ObjectSchema) openAPIParam).getProperties();
            BRecordType recordType = (BRecordType) resourceParamType;
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                boolean isExist = false;
                for (BField field : recordType.fields) {
                    if (entry.getKey().equals(field.name.getValue())
                            && field.getType().getKind().typeName()
                            .equals(ValidatorUtil.convertOpenAPITypeToBallerina(entry.getValue().getType()))) {
                        isExist = true;
                        if (ValidatorUtil.convertOpenAPITypeToBallerina(entry.getValue().getType()).equals("record")) {
                            isExist = validateOpenAPIAgainResourceParams(resourceParam, field.symbol, entry.getValue(),
                                    dLog, operation, path);
                        }
                    }
                }

                if (!isExist) {
                    dLog.logDiagnostic(Diagnostic.Kind.ERROR, resourceParam.getParameter().getPosition(),
                            ErrorMessages.unimplementedFieldInOperation(entry.getKey(), resourceParam.getName(),
                                    operation, path));
                }
            }
            return true;
        } else if (resourceParamType.getKind().typeName().equals("[]")
                && openAPIParam.getType().equals("array")) {
            // TODO: Implement array type handling.
            return true;
        } else if (resourceParamType.getKind().typeName().equals("string")
                && openAPIParam.getType().equals("string")) {
            return true;
        } else if (resourceParamType.getKind().typeName().equals("int")
                && openAPIParam.getType().equals("integer")) {
            return true;
        } else if (resourceParamType.getKind().typeName().equals("boolean")
                && openAPIParam.getType().equals("boolean")) {
            return true;
        } else if (resourceParamType.getKind().typeName().equals("decimal")
                && openAPIParam.getType().equals("number")) {
            return true;
        }
        return false;
    }

    private static ResourceSummary getResourceSummaryByMethod(List<ResourceSummary> resourceSummaries, String method) {
        ResourceSummary matchingResource = null;
        for (ResourceSummary resourceSummary : resourceSummaries) {
            if (resourceSummary.isMethodAvailable(method)) {
                matchingResource = resourceSummary;
                break;
            }
        }
        return matchingResource;
    }

    private static List<String> getAllMethodsInResourceSummaries(List<ResourceSummary> resourceSummaries) {
        List<String> methods = new ArrayList<>();
        for (ResourceSummary resourceSummary : resourceSummaries) {
            methods.addAll(resourceSummary.getMethods());
        }

        return methods;
    }

    private static List<ResourceSummary> getResourceSummaryByPath(String path,
                                                                  List<ResourceSummary> resourceSummaryList) {
        List<ResourceSummary> resourceSummaries = null;
        for (ResourceSummary resourceSummary : resourceSummaryList) {
            if (resourceSummary.getPath() != null && resourceSummary.getPath().equals(path)) {
                if (resourceSummaries == null) {
                    resourceSummaries = new ArrayList<>();
                    resourceSummaries.add(resourceSummary);
                } else {
                    resourceSummaries.add(resourceSummary);
                }
            }
        }
        return resourceSummaries;
    }

    private static String convertOpenAPITypeToBallerina(String type) {
        String convertedType;
        switch (type) {
            case "integer":
                convertedType = "int";
                break;
            case "string":
                convertedType = "string";
                break;
            case "boolean":
                convertedType = "boolean";
                break;
            case "array":
                convertedType = "[]";
                break;
            case "object":
                convertedType = "record";
                break;
            case "number":
                convertedType = "decimal";
                break;
            default:
                convertedType = "";
        }

        return convertedType;
    }

    private static Diagnostic.DiagnosticPosition getServiceNamePosition(ServiceNode serviceNode) {
        return serviceNode.getName().getPosition();
    }
}
