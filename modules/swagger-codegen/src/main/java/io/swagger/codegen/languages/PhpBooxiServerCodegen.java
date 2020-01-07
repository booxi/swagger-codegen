package io.swagger.codegen.languages;

import io.swagger.codegen.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class PhpBooxiServerCodegen extends AbstractPhpCodegen implements CodegenConfig {
    @SuppressWarnings("hiding")
    static Logger LOGGER = LoggerFactory.getLogger(PhpBooxiServerCodegen.class);

    public static final String PHP_LEGACY_SUPPORT = "phpLegacySupport";
    protected String testsPackage;
    protected String apiTestsPackage;
    protected String modelTestsPackage;
    protected String composerVendorName = "swagger";
    protected String composerProjectName = "server-bundle";
    protected String testsDirName = "Tests";
    protected String bundleName;
    protected String bundleClassName;
    protected String bundleExtensionName;
    protected String bundleAlias;
    protected String controllerDirName = "Controller";
    protected String serviceDirName = "Service";
    protected String controllerPackage;
    protected String servicePackage;
    protected Boolean phpLegacySupport = Boolean.TRUE;

    protected HashSet<String> typeHintable;

    public PhpBooxiServerCodegen() {
        super();

        // clear import mapping (from default generator) as php does not use it
        // at the moment
        importMapping.clear();

        modelDocTemplateFiles.clear();
        apiDocTemplateFiles.clear();

        supportsInheritance = true;
        srcBasePath = ".";
        setInvokerPackage("");
        packagePath = "";
        modelDirName = "core";
        docsBasePath = "Resources" + File.separator + "docs";
        apiDocPath = docsBasePath + File.separator + apiDirName;
        modelDocPath = docsBasePath + File.separator + modelDirName;
        outputFolder = "generated-code" + File.separator + "php";
        //apiTemplateFiles.put("api_controller.mustache", ".php");
        //modelTestTemplateFiles.put("testing/model_test.mustache", ".php");
        apiTestTemplateFiles = new HashMap<String, String>();
        //apiTestTemplateFiles.put("testing/api_test.mustache", ".php");
        embeddedTemplateDir = templateDir = "php-booxi-server";

        // default HIDE_GENERATION_TIMESTAMP to true
        hideGenerationTimestamp = Boolean.TRUE;

        setReservedWordsLowerCase(
            Arrays.asList(
                // local variables used in api methods (endpoints)
                "resourcePath", "httpBody", "queryParams", "headerParams",
                "formParams", "_header_accept", "_tempBody",

                // PHP reserved words
                "__halt_compiler", "abstract", "and", "array", "as", "break", "callable", "case", "catch", "class", "clone", "const", "continue", "declare", "default", "die", "do", "echo", "else", "elseif", "empty", "enddeclare", "endfor", "endforeach", "endif", "endswitch", "endwhile", "eval", "exit", "extends", "final", "for", "foreach", "function", "global", "goto", "if", "implements", "include", "include_once", "instanceof", "insteadof", "interface", "isset", "list", "namespace", "new", "or", "print", "private", "protected", "public", "require", "require_once", "return", "static", "switch", "throw", "trait", "try", "unset", "use", "var", "while", "xor"
            )
        );

        // ref: http://php.net/manual/en/language.types.intro.php
        languageSpecificPrimitives = new HashSet<String>(
            Arrays.asList(
                "bool",
                "int",
                "double",
                "float",
                "string",
                "object",
                "mixed",
                "number",
                "void",
                "byte",
                "array"
            )
        );

        defaultIncludes = new HashSet<String>(
            Arrays.asList(
                "\\DateTime",
                "UploadedFile"
            )
        );

        variableNamingConvention = "camelCase";

        // provide primitives to mustache template
        List sortedLanguageSpecificPrimitives= new ArrayList(languageSpecificPrimitives);
        Collections.sort(sortedLanguageSpecificPrimitives);
        String primitives = "'" + StringUtils.join(sortedLanguageSpecificPrimitives, "', '") + "'";
        additionalProperties.put("primitives", primitives);

        // ref: https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#data-types
        typeMapping = new HashMap<String, String>();
        typeMapping.put("integer", "int");
        typeMapping.put("long", "int");
        typeMapping.put("number", "float");
        typeMapping.put("float", "float");
        typeMapping.put("double", "double");
        typeMapping.put("string", "string");
        typeMapping.put("byte", "int");
        typeMapping.put("boolean", "bool");
        typeMapping.put("Date", "\\DateTime");
        typeMapping.put("DateTime", "\\DateTime");
        typeMapping.put("file", "UploadedFile");
        typeMapping.put("map", "array");
        typeMapping.put("array", "array");
        typeMapping.put("list", "array");
        typeMapping.put("object", "array");
        typeMapping.put("binary", "string");
        typeMapping.put("ByteArray", "string");
        typeMapping.put("UUID", "string");

        cliOptions.add(new CliOption(CodegenConstants.HIDE_GENERATION_TIMESTAMP, CodegenConstants.HIDE_GENERATION_TIMESTAMP_DESC)
                .defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(new CliOption(PHP_LEGACY_SUPPORT, "Should the generated code be compatible with PHP 5.x?").defaultValue(Boolean.TRUE.toString()));
    }

    public void setPhpLegacySupport(Boolean support) {
        this.phpLegacySupport = support;
    }

    public String controllerFileFolder() {
        return (outputFolder + File.separator + toPackagePath(controllerPackage, srcBasePath));
    }

    @Override
    public String escapeText(String input) {
        if (input != null) {
            // Trim the string to avoid leading and trailing spaces.
            return super.escapeText(input).trim();
        }
        return input;
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "php-booxi-server";
    }

    @Override
    public String getHelp() {
        return "Generates a Symfony server bundle.";
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        String suffix = apiTemplateFiles().get(templateName);
        if (templateName.equals("api_controller.mustache"))
            return controllerFileFolder() + '/' + toControllerName(tag) + suffix;

        return apiFileFolder() + '/' + toApiFilename(tag) + suffix;
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(PHP_LEGACY_SUPPORT)) {
            this.setPhpLegacySupport(Boolean.valueOf((String) additionalProperties.get(PHP_LEGACY_SUPPORT)));
        } else {
            additionalProperties.put(PHP_LEGACY_SUPPORT, phpLegacySupport);
        }

        additionalProperties.put("escapedInvokerPackage", invokerPackage.replace("\\", "\\\\"));
        additionalProperties.put("controllerPackage", controllerPackage);
        additionalProperties.put("servicePackage", servicePackage);
        additionalProperties.put("apiTestsPackage", apiTestsPackage);
        additionalProperties.put("modelTestsPackage", modelTestsPackage);

        // make api and model src path available in mustache template
        additionalProperties.put("apiSrcPath", "." + File.separator + toSrcPath(apiPackage, srcBasePath));
        additionalProperties.put("modelSrcPath", "." + File.separator + toSrcPath(modelPackage, srcBasePath));
        additionalProperties.put("testsSrcPath", "." + File.separator + toSrcPath(testsPackage, srcBasePath));
        additionalProperties.put("apiTestsSrcPath", "." + File.separator + toSrcPath(apiTestsPackage, srcBasePath));
        additionalProperties.put("modelTestsSrcPath", "." + File.separator + toSrcPath(modelTestsPackage, srcBasePath));
        additionalProperties.put("apiTestPath", "." + File.separator + testsDirName + File.separator + apiDirName);
        additionalProperties.put("modelTestPath", "." + File.separator + testsDirName + File.separator + modelDirName);

        // make api and model doc path available in mustache template
        additionalProperties.put("apiDocPath", apiDocPath);
        additionalProperties.put("modelDocPath", modelDocPath);

        // make test path available in mustache template
        additionalProperties.put("testsDirName", testsDirName);

        final String configDir = getPackagePath() + File.separator + "Resources" + File.separator + "config";
        final String dependencyInjectionDir = getPackagePath() + File.separator + "DependencyInjection";

        // Type-hintable primitive types
        // ref: http://php.net/manual/en/functions.arguments.php#functions.arguments.type-declaration
        if (phpLegacySupport) {
            typeHintable = new HashSet<String>(
                Arrays.asList(
                    "array"
                )
            );
        } else {
            typeHintable = new HashSet<String>(
                Arrays.asList(
                    "array",
                    "bool",
                    "float",
                    "int",
                    "string"
                )
            );
        }
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        objs = super.postProcessOperations(objs);

        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        operations.put("controllerName", toControllerName((String) operations.get("pathPrefix")));
        operations.put("symfonyService", toSymfonyService((String) operations.get("pathPrefix")));

        HashSet<CodegenSecurity> authMethods = new HashSet<>();
        List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");

        for (CodegenOperation op : operationList) {
            // Loop through all input parameters to determine, whether we have to import something to
            // make the input type available.
            for (CodegenParameter param : op.allParams) {
                // Determine if the parameter type is supported as a type hint and make it available
                // to the templating engine
                String typeHint = getTypeHint(param.dataType);
                if (!typeHint.isEmpty()) {
                    param.vendorExtensions.put("x-parameterType", typeHint);
                }

                // Create a variable to display the correct data type in comments for interfaces
                param.vendorExtensions.put("x-commentType", param.dataType);
                if (param.isContainer) {
                    param.vendorExtensions.put("x-commentType", param.dataType+"[]");
                }

                // Quote default values for strings
                // @todo: The default values for headers, forms and query params are handled
                //        in DefaultCodegen fromParameter with no real possibility to override
                //        the functionality. Thus we are handling quoting of string values here
                if (param.dataType.equals("string") && param.defaultValue != null && !param.defaultValue.isEmpty()) {
                    param.defaultValue = "'"+param.defaultValue+"'";
                }
            }

            // Create a variable to display the correct return type in comments for interfaces
            if (op.returnType != null) {
                op.vendorExtensions.put("x-commentType", op.returnType);
                if (!op.returnTypeIsPrimitive) {
                    op.vendorExtensions.put("x-commentType", op.returnType+"[]");
                }
            } else {
                op.vendorExtensions.put("x-commentType", "void");
            }

            // Add operation's authentication methods to whole interface
            if (op.authMethods != null) {
                authMethods.addAll(op.authMethods);
            }
        }

        operations.put("authMethods", authMethods);

        return objs;
    }

    @Override
    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
        objs = super.postProcessAllModels(objs);
        
        Map<String, CodegenModel> allModels = new HashMap<>();
        for (Map.Entry<String, Object> entry : objs.entrySet()) {
            String modelName = toModelName(entry.getKey());
            Map<String, Object> inner = (Map<String, Object>) entry.getValue();
            List<Map<String, Object>> models = (List<Map<String, Object>>) inner.get("models");
            for (Map<String, Object> mo : models) {
                CodegenModel cm = (CodegenModel) mo.get("model");
                allModels.put(modelName, cm);
            }
        }

        for (CodegenModel model : allModels.values()) {
            for (CodegenProperty var : model.vars) {
                String booxiType = buildBooxiEntityType(var, allModels);
                var.vendorExtensions.put("booxiEntityType", booxiType);

                String booxiCommentType = buildBooxiCommentType(var, allModels);
                var.vendorExtensions.put("booxiCommentType", booxiCommentType);
            }
        }

        return objs;
    }

    protected String buildBooxiEntityType(CodegenProperty var, Map<String, CodegenModel> allModels) {
        String dataFormat = var.dataFormat == null ? "" : var.dataFormat;
        if (var.isDateTime) {
            return "\\BooxiEntity::TIMESTAMP";
        }
        if (var.isDate) {
            return "\\BooxiEntity::DATE";
        } 
        if (var.isString) {
            if (dataFormat.equals("decimal")) {
                return "\\BooxiEntity::DECIMAL";
            } else if (dataFormat.equals("language")) {
                return "\\BooxiEntity::LANG";
            } else {
                return "\\BooxiEntity::STRING";
            }
        }

        if (var.isInteger) {
            if (dataFormat.equals("id")) {
                return "\\BooxiEntity::ID";
            } else {
                return "\\BooxiEntity::INTEGER";
            }
        }
        if (var.isFloat || var.isDouble) {
            return "\\BooxiEntity::FLOAT";
        }
        if (var.isBoolean) {
            return "\\BooxiEntity::BOOLEAN";
        }

        if (var.isListContainer) {
            return "\\BooxiEntity::ARRAY_OF(" + this.buildBooxiEntityType(var.items, allModels) + ")";
        }


        CodegenModel typeModel = allModels.get(var.baseType);
        if (typeModel != null) {
            if (typeModel.isEnum) {
                if (typeModel.dataType.equals("string")) {
                    return "\\BooxiEntity::STRING";
                } else if (typeModel.dataType.equals("int")) {
                    return "\\BooxiEntity::INTEGER";
                }
            } else {
                return "\\BooxiEntity::ENTITY('\\" + var.datatype + "')";
            }
        }
        return "\\BooxiEntity::UNSUPPORTED_TYPE";
    }

    protected String buildBooxiCommentType(CodegenProperty var, Map<String, CodegenModel> allModels) {
        StringBuilder docType = new StringBuilder();
        if (var.isDateTime || var.isString || var.isInteger || var.isFloat || var.isDouble || var.isBoolean) {
            docType.append(var.datatype);
        } else if (var.isDate) {
            docType.append("\\SimpleDate");
        } else {
            CodegenModel typeModel = allModels.get(var.baseType);
            if (typeModel == null) {
                // Embedded object
                docType.append("\\").append(var.datatype);
            } else if (typeModel.isEnum) {
                if (typeModel.dataType.equals("string")) {
                    docType.append("string");
                } else if (typeModel.dataType.equals("int")) {
                    docType.append("int");
                } else {
                    // Unexpected type
                    docType.append("mixed");
                }
            } else {
                // Normal object
                docType.append("\\").append(var.datatype);
            }
        }

        if (var.isListContainer) {
            docType.append("[]");
        }

        return docType.toString();
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        objs = super.postProcessModels(objs);

        ArrayList<Object> modelsArray = (ArrayList<Object>) objs.get("models");
        Map<String, Object> models = (Map<String, Object>) modelsArray.get(0);
        CodegenModel model = (CodegenModel) models.get("model");

        // Simplify model var type
        for (CodegenProperty var : model.vars) {
            if (var.datatype != null) {
                // Determine if the parameter type is supported as a type hint and make it available
                // to the templating engine
                String typeHint = getTypeHint(var.datatype);
                if (!typeHint.isEmpty()) {
                    var.vendorExtensions.put("x-parameterType", typeHint);
                }

                // Create a variable to display the correct data type in comments for models
                var.vendorExtensions.put("x-commentType", var.datatype);
                if (var.isContainer) {
                    var.vendorExtensions.put("x-commentType", var.datatype+"[]");
                }

                if (var.isBoolean) {
                    var.getter = var.getter.replaceAll("^get", "is");
                }
            }
        }

        return objs;
    }

    @Override
    public String escapeReservedWord(String name) {
        if(this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    @Override
    public String apiTestFileFolder() {
        return (outputFolder + File.separator + toPackagePath(apiTestsPackage, srcBasePath));
    }

    @Override
    public String modelTestFileFolder() {
        return (outputFolder + File.separator + toPackagePath(modelTestsPackage, srcBasePath));
    }

    public void setComposerVendorName(String composerVendorName) {
        this.composerVendorName = composerVendorName;
    }

    public void setComposerProjectName(String composerProjectName) {
        this.composerProjectName = composerProjectName;
    }

    @Override
    public void setInvokerPackage(String invokerPackage) {
        super.setInvokerPackage(invokerPackage);
        apiPackage = invokerPackage + "\\" + apiDirName;
        modelPackage = invokerPackage + "\\" + modelDirName;
        testsPackage = invokerPackage + "\\" + testsDirName;
        apiTestsPackage = testsPackage + "\\" + apiDirName;
        modelTestsPackage = testsPackage + "\\" + modelDirName;
        controllerPackage = invokerPackage + "\\" + controllerDirName;
        servicePackage = invokerPackage + "\\" + serviceDirName;
    }

    @Override
    public String getTypeDeclaration(Property p) {
        if (p instanceof ArrayProperty) {
            ArrayProperty ap = (ArrayProperty) p;
            Property inner = ap.getItems();
            return getTypeDeclaration(inner);
        }

        if (p instanceof MapProperty) {
            MapProperty mp = (MapProperty) p;
            Property inner = mp.getAdditionalProperties();
            return getTypeDeclaration(inner);
        }

        if (p instanceof RefProperty) {
            return getTypeDeclaration(getPropertyTypeDeclaration(p));
        }

        return getPropertyTypeDeclaration(p);
    }

    /**
     * Output the type declaration of the property
     *
     * @param p Swagger Property object
     * @return a string presentation of the property type
     */
    public String getPropertyTypeDeclaration(Property p) {
        String swaggerType = getSwaggerType(p);
        if (typeMapping.containsKey(swaggerType)) {
            return typeMapping.get(swaggerType);
        }
        return swaggerType;
    }

    @Override
    public String getTypeDeclaration(String name) {
        if (!languageSpecificPrimitives.contains(name)) {
            return modelPackage + "\\" + name;
        }
        return super.getTypeDeclaration(name);
    }

    /**
     * Return the fully-qualified "Model" name for import
     *
     * @param name the name of the "Model"
     * @return the fully-qualified "Model" name for import
     */
    @Override
    public String toModelImport(String name) {
        if ("".equals(modelPackage())) {
            return name;
        } else {
            return modelPackage() + "\\" + name;
        }
    }

    @Override
    public String toEnumValue(String value, String datatype) {
        if ("int".equals(datatype) || "double".equals(datatype) || "float".equals(datatype)) {
            return value;
        } else {
            return "\"" + escapeText(value) + "\"";
        }
    }

    /**
     * Return the regular expression/JSON schema pattern (http://json-schema.org/latest/json-schema-validation.html#anchor33)
     *
     * @param pattern the pattern (regular expression)
     * @return properly-escaped pattern
     */
    @Override
    public String toRegularExpression(String pattern) {
        return escapeText(pattern);
    }

    public String toApiName(String name) {
        if (name.isEmpty()) {
            return "DefaultApiInterface";
        }
        return camelize(name, false) + "ApiInterface";
    }

    protected String toControllerName(String name) {
        if (name.isEmpty()) {
            return "DefaultController";
        }
        return camelize(name, false) + "Controller";
    }

    protected String toSymfonyService(String name) {
        String prefix = composerVendorName + ".api.";
        if (name.isEmpty()) {
            return prefix + "default";
        }

        return prefix + name;
    }

    protected String getTypeHint(String type) {
        // Type hint array types
        if (type.endsWith("[]")) {
            return "array";
        }

        // Check if the type is a native type that is type hintable in PHP
        if (typeHintable.contains(type)) {
            return type;
        }

        // Default includes are referenced by their fully-qualified class name (including namespace)
        if (defaultIncludes.contains(type)) {
            return type;
        }

        // Model classes are assumed to be imported and we reference them by their class name
        if (isModelClass(type)) {
            // This parameter is an instance of a model
            return extractSimpleName(type);
        }
        
        // PHP does not support type hinting for this parameter data type
        return "";
    }

    protected Boolean isModelClass(String type) {
        return Boolean.valueOf(type.contains(modelPackage()));
    }
}
