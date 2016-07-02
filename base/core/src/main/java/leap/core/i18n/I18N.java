/*
 * Copyright 2014 the original author or authors.
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
package leap.core.i18n;

import java.util.ArrayList;
import java.util.List;

import leap.core.annotation.LocalizeKey;
import leap.lang.Named;
import leap.lang.Strings;
import leap.lang.annotation.Localizable;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;

public class I18N {
	
	private static final String BEAN_LOCALIZABLE_PROPERTIES_KEY = I18N.class.getName() + "$localizable";
	
	public static void localize(MessageSource ms, Named bean) {
		BeanType bt = BeanType.of(bean.getClass());
		localize(ms, bean, getLocalizedKeyPrefix(bt, bean), bt);
	}
	
	public static void localize(MessageSource ms, Named bean, String keyPrefix) {
		localize(ms, bean, keyPrefix, BeanType.of(bean.getClass()));
	}
	
	public static String getLocalizedKeyPrefix(Named bean) {
		return getLocalizedKeyPrefix(BeanType.of(bean.getClass()), bean);
	}
	
	private static void localize(MessageSource ms, Named bean, String keyPrefix, BeanType bt) {
		BeanProperty[] bps = getLocalizableBeanProperties(bt, bean);
		
		if(bps.length == 0){
			return;
		}
		
		for(int i=0;i<bps.length;i++){
			BeanProperty bp = bps[i];

			String pkey = keyPrefix + "." + bp.getName();
			String localizedValue = ms.tryGetMessage(pkey);

			if(null != localizedValue){
				bp.setValue(bean, localizedValue);
			}
		}
		
		if(bean instanceof leap.core.i18n.Localizable){
			((leap.core.i18n.Localizable) bean).localize(ms, keyPrefix);
		}
	}
	
	private static String getLocalizedKeyPrefix(BeanType bt, Named bean) {
		LocalizeKey a = bt.getBeanClass().getAnnotation(LocalizeKey.class);
		
		String keyPrefix = null != a ? a.value() : null;
		
		if(Strings.isEmpty(keyPrefix) && bean instanceof LocalizeKeyed){
			keyPrefix = ((LocalizeKeyed)bean).getLocalizeKey();
		}
		
		if(Strings.isEmpty(keyPrefix)){
			keyPrefix = bt.getBeanClass().getName();
		}
		
		return keyPrefix + "." + bean.getName().toLowerCase();
	}
	
	private static BeanProperty[] getLocalizableBeanProperties(BeanType bt, Object bean) {
		BeanProperty[] props = (BeanProperty[])bt.getAttribute(BEAN_LOCALIZABLE_PROPERTIES_KEY);

		if(null == props){

			List<BeanProperty> list = new ArrayList<>();
			
			for(BeanProperty bp : bt.getProperties()){
				
				if(bp.isAnnotationPresent(Localizable.class)){
					list.add(bp);
				}
				
			}
			
			props = list.toArray(new BeanProperty[list.size()]);
			
			bt.setAttribute(BEAN_LOCALIZABLE_PROPERTIES_KEY, props);
		}
		
		return props;
	}

	protected I18N() {
		
	}

}