/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.api.spec.swagger;

public class SwaggerConstants {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String SWAGGER               = "swagger";
    public static final String INFO                  = "info";
    public static final String NAME                  = "name";
    public static final String TITLE                 = "title";
    public static final String SUMMARY               = "summary";
    public static final String DESCRIPTION           = "description";
    public static final String OPERATION_ID          = "operationId";
    public static final String TERMS_OF_SERVICE      = "termsOfService";
    public static final String CONTACT               = "contact";
    public static final String LICENSE               = "license";
    public static final String VERSION               = "version";
    public static final String HOST                  = "host";
    public static final String BASE_PATH             = "basePath";
    public static final String PATHS                 = "paths";
    public static final String DEFINITIONS           = "definitions";
    public static final String PARAMETERS            = "parameters";
    public static final String IN                    = "in";
    public static final String REQUIRED              = "required";
    public static final String READONLY              = "readOnly";
    public static final String RESPONSES             = "responses";
    public static final String TAGS                  = "tags";
    public static final String EXTERNAL_DOCS         = "externalDocs";
    public static final String URL                   = "url";
    public static final String EMAIL                 = "email";
    public static final String PRODUCES              = "produces";
    public static final String CONSUMES              = "consumes";
    public static final String SCHEMES               = "schemes";
    public static final String SECURITY              = "security";
    public static final String SECURITY_DEFINITIONS  = "securityDefinitions";
    public static final String SCHEMA                = "schema";
    public static final String TYPE                  = "type";
    public static final String DISCRIMINATOR         = "discriminator";
    public static final String FORMAT                = "format";
    public static final String ENUM                  = "enum";
    public static final String ARRAY                 = "array";
    public static final String DEFAULT               = "default";
    public static final String PATTERN               = "pattern";
    public static final String MAX_LENGTH            = "maxLength";
    public static final String MIN_LENGTH            = "minLength";
    public static final String MAXIMUM               = "maximum";
    public static final String MINIMUM               = "minimum";
    public static final String EXCLUSIVE_MAXIMUM     = "exclusiveMaximum";
    public static final String EXCLUSIVE_MINIMUM     = "exclusiveMinimum";
    public static final String ITEMS                 = "items";
    public static final String REF                   = "$ref";
    public static final String OBJECT                = "object";
    public static final String FILE                  = "file";
    public static final String PROPERTIES            = "properties";
    public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    public static final String DEPRECATED            = "deprecated";
    public static final String ALL_OF                = "allOf";

    //oauth2
	public static final String OAUTH2               = "oauth2";
	public static final String FLOW                 = "flow";
	public static final String IMPLICIT             = "implicit";
	public static final String ACCESS_CODE          = "accessCode";
	public static final String AUTHZ_URL            = "authorizationUrl";
	public static final String TOKEN_URL            = "tokenUrl";
	public static final String SCOPES               = "scopes";

    public static final String USER_REQUIRED        = "userRequired";
    public static final String CLIENT_REQUIRED      = "clientRequired";
	
    //extend
    public static final String X_SECURITY   = "x-security";

    public static final String X_CORS       = "x-cors";

    public static final String X_ENTITY     = "x-entity";
    public static final String X_IDENTITY   = "x-identity";
    public static final String X_UNIQUE     = "x-unique";
    public static final String X_CREATABLE  = "x-creatable";
    public static final String X_UPDATABLE  = "x-updatable";
    public static final String X_SORTABLE   = "x-sortable";
    public static final String X_FILTERABLE = "x-filterable";
    public static final String X_EXPANDABLE = "x-expandable";

	
	protected SwaggerConstants() {
		
	}

}