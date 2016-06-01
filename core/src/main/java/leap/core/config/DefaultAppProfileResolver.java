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

package leap.core.config;

import leap.core.AppConfig;
import leap.lang.Strings;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.tools.DEV;

import java.util.Map;

public class DefaultAppProfileResolver implements AppProfileResolver {
    protected static final String APP_PROFILE_CONFIG_RESOURCE       = "classpath:/profile";
    protected static final String APP_PROFILE_LOCAL_CONFIG_RESOURCE = "classpath:/profile_local";

    @Override
    public String resolveProfile(Object externalContext, Map<String, String> externalProperties) {
        //from system properties.
        String profile = System.getProperty(AppConfig.SYS_PROPERTY_PROFILE);
        if(!Strings.isEmpty(profile)) {
            return profile;
        }

        //from external properties.
        if(Strings.isEmpty(profile) && null != externalProperties) {
            profile = externalProperties.get(AppConfig.INIT_PROPERTY_PROFILE);
        }

        //Read local profile file if running in dev project.
        if(Strings.isEmpty(profile) && DEV.isDevProject(externalContext)) {
            //read from local profile file
            profile = readProfileFile(externalProperties, APP_PROFILE_LOCAL_CONFIG_RESOURCE);
        }

        if(Strings.isEmpty(profile)) {
            //read from default profile file
            profile = readProfileFile(externalProperties, APP_PROFILE_CONFIG_RESOURCE);
        }

        //auto detect profile name
        if(Strings.isEmpty(profile)){
            profile = autoDetectProfileName(externalContext);
        }

        if(Strings.isEmpty(profile)) {
            profile = AppConfig.PROFILE_PRODUCTION;
        }

        return profile;
    }

    protected String readProfileFile(Map<String,String> externalProperties, String location) {
        Resource r = Resources.getResource(location);

        if(null != r && r.exists() && !r.isDirectory()){

            String profile = Strings.trim(r.getContent());

            if(profile.startsWith("${") && profile.endsWith("}")) {

                String prop = profile.substring(1,profile.length() - 1);

                profile = System.getProperty(prop);

                if(Strings.isEmpty(profile) && null != externalProperties) {
                    profile = externalProperties.get(prop);
                }
            }

            return profile;
        }
        return null;
    }

    protected String autoDetectProfileName(Object externalContext){
        //Auto detect development environment (maven environment)
        if(DEV.isDevProject(externalContext)){
            return AppConfig.PROFILE_DEVELOPMENT;
        }else{
            return null;
        }
    }

}