/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.api.meta;

import leap.lang.*;
import leap.lang.exception.ObjectExistsException;
import leap.web.api.meta.model.*;

import java.util.*;

/**
 * The builder of {@link ApiMetadata}
 */
public class ApiMetadataBuilder extends MApiNamedWithDescBuilder<ApiMetadata> {
	
    protected String             basePath;
    protected String             termsOfService;
    protected MApiContactBuilder contact;
    protected String             version;
    protected String             host;

    protected Set<String>                      protocols    = new LinkedHashSet<>();
    protected Set<String>                      consumes     = new LinkedHashSet<>();
    protected Set<String>                      produces     = new LinkedHashSet<>();
    protected Map<String, MApiResponseBuilder> responses    = new LinkedHashMap<>();
    protected Map<String, MApiPathBuilder>     paths        = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    protected Map<String, MApiModelBuilder>    models       = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    protected Map<String, MApiPermission>      permissions  = new LinkedHashMap<>();
    protected Map<String, MApiTag>             tags         = new LinkedHashMap<>();
    protected List<MApiSecurityDef>            securityDefs = new ArrayList<>();
    protected Set<String>                      operationIds = new HashSet<>();

    public ApiMetadataBuilder() {
        super();
    }

	public ApiMetadataBuilder(String basePath) {
		this.basePath = basePath;
	}
	
	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getTermsOfService() {
		return termsOfService;
	}

	public void setTermsOfService(String termsOfService) {
		this.termsOfService = termsOfService;
	}

	public MApiContactBuilder getContact() {
		return contact;
	}

	public void setContact(MApiContactBuilder contact) {
		this.contact = contact;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Set<String> getProtocols() {
		return protocols;
	}
	
	public void addProtocol(String p) {
		protocols.add(p);
	}
	
	public void addProtocols(String... ps) {
		Collections2.addAll(protocols, ps);
	}

	public Set<String> getConsumes() {
		return consumes;
	}
	
	public void addConsume(String mimeType) {
		consumes.add(mimeType);
	}
	
	public void addConsumes(String... cs) {
		Collections2.addAll(consumes, cs);
	}

	public Set<String> getProduces() {
		return produces;
	}
	
	public void addProduce(String mimeType) {
		produces.add(mimeType);
	}
	
	public void addProduces(String... ps) {
		Collections2.addAll(produces, ps);
	}

    public Map<String, MApiResponseBuilder> getResponses() {
        return responses;
    }

    public void putResponse(String name, MApiResponseBuilder r) {
        responses.put(name, r);
    }

    public void putResponse(String name, MApiResponse r) {
        putResponse(name, new MApiResponseBuilder(r));
    }

    public void setResponses(Map<String, MApiResponseBuilder> responses) {
        this.responses = responses;
    }

    public void addPath(MApiPathBuilder path) throws ObjectExistsException {
		Args.notNull(path, "api path");
		Args.notEmpty(path.getPathTemplate(), "path template");
		
		if(paths.containsKey(path.getPathTemplate())) {
			throw new ObjectExistsException("The path template '" + path.getPathTemplate() + "' already exists");
		}
		
		String fullPath = path.getPathTemplate();
		String relativePath = Strings.removeStart(fullPath, basePath);
		
		paths.put(relativePath, path);
	}
	
	public Map<String, MApiPathBuilder> getPaths() {
		return paths;
	}
	
	public MApiPathBuilder getPath(String pathTemplate) {
		Args.notEmpty(pathTemplate,"path template");
        String relativePath = Strings.removeStart(pathTemplate, basePath);
		return paths.get(relativePath);
	}

    public MApiPathBuilder getOrAddPath(String pathTemplate) {
        MApiPathBuilder path = getPath(pathTemplate);
        if(null == path) {
            path = new MApiPathBuilder();
            path.setPathTemplate(pathTemplate);
            addPath(path);
        }
        return path;
    }

	public Map<String, MApiModelBuilder> getModels() {
		return models;
	}
	
	public void addModel(MApiModelBuilder model) throws ObjectExistsException {
		Args.notNull(model, "model");
		
		if(models.containsKey(model.getName())) {
			throw new ObjectExistsException("The model '" + model.getName() + "' already exists");
		}
		
		models.put(model.getName(), model);
	}

	public boolean containsModel(String name) {
		return models.containsKey(name);
	}

    public Map<String, MApiPermission> getPermissions() {
        return permissions;
    }

    public void addPermission(MApiPermission p) {
        if(permissions.containsKey(p.getValue())) {
            throw new IllegalStateException("Permission '" + p.getValue() + "' already exists!");
        }
        permissions.put(p.getValue(), p);
    }


	public List<MApiSecurityDef> getSecurityDefs() {
        return securityDefs;
    }
	
	public void addSecurityDef(MApiSecurityDef def) {
	    securityDefs.add(def);
	}

    public Map<String, MApiTag> getTags() {
        return tags;
    }

    public void addTag(MApiTag tag) {
        if(tags.containsKey(tag.getName())) {
            throw new IllegalStateException("Tag '" + tag.getName() + "' already exists!");
        }
        tags.put(tag.getName(), tag);
    }

    public boolean hasModel(String name) {
        for(MApiModelBuilder m : models.values()){
            if(name.equals(m.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasModel(Class<?> type) {
        return tryGetModel(type) != null;
    }

    public MApiModelBuilder tryGetModel(Class<?> type) {
        for(MApiModelBuilder m : models.values()){
            if(type.equals(m.getJavaType())) {
                return m;
            }
        }
        return null;
    }

    public Set<String> getOperationIds() {
        return operationIds;
    }

    @Override
    public ApiMetadata build() {

        MApiPermission[] permissions = this.permissions.values().toArray(new MApiPermission[0]);
        MApiTag[]        tags        = this.tags.values().toArray(new MApiTag[0]);

        return new ApiMetadata(name, title, summary, description,
        					termsOfService, null == contact ? null : contact.build(),
        					version, host, basePath,
        					protocols.toArray(Arrays2.EMPTY_STRING_ARRAY), 
        					consumes.toArray(Arrays2.EMPTY_STRING_ARRAY), 
        					produces.toArray(Arrays2.EMPTY_STRING_ARRAY),
                            Builders.buildMap(responses),
        					Builders.buildMap(paths),
        					Builders.buildMap(models),
                            permissions,
        					securityDefs.toArray(new MApiSecurityDef[]{}),
                            tags,
        					attrs);
    }
	
}