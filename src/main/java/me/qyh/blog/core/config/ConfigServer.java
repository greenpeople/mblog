/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.core.config;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Resources;


@Service
public class ConfigServer implements InitializingBean {

	private static final String PAGE_SIZE_FILE = "pagesize.file";
	private static final String PAGE_SIZE_USERFRAGEMENT = "pagesize.userfragment";
	private static final String PAGE_SIZE_USERPAGE = "pagesize.userpage";
	private static final String PAGE_SIZE_ARICLE = "pagesize.article";
	private static final String PAGE_SIZE_TAG = "pagesize.tag";

	private Properties config = new Properties();

	private static final Path RES_PATH = Constants.CONFIG_DIR.resolve("config.properties");

	static {
		FileUtils.createFile(RES_PATH);
	}

	private Resource resource = new PathResource(RES_PATH);

	/**
	 * 获取全局配置
	 * @return
	 */
	@Cacheable(key = "'globalConfig'", value = "configCache")
	public GlobalConfig getGlobalConfig() {
		GlobalConfig config = new GlobalConfig();
		config.setArticlePageSize(getInt(PAGE_SIZE_ARICLE, 5));
		config.setFilePageSize(getInt(PAGE_SIZE_FILE, 5));
		config.setTagPageSize(getInt(PAGE_SIZE_TAG, 5));
		config.setPagePageSize(getInt(PAGE_SIZE_USERPAGE, 5));
		config.setFragmentPageSize(getInt(PAGE_SIZE_USERFRAGEMENT, 5));

		return config;
	}


	/**
	 * 保存全局配置
	 * @param globalConfig
	 * @return
	 */
	@CachePut(key = "'globalConfig'", value = "configCache")
	public GlobalConfig updateGlobalConfig(GlobalConfig globalConfig) {
		config.setProperty(PAGE_SIZE_FILE, Integer.toString(globalConfig.getFilePageSize()));
		config.setProperty(PAGE_SIZE_TAG, Integer.toString(globalConfig.getTagPageSize()));
		config.setProperty(PAGE_SIZE_ARICLE, Integer.toString(globalConfig.getArticlePageSize()));
		config.setProperty(PAGE_SIZE_USERFRAGEMENT, Integer.toString(globalConfig.getFragmentPageSize()));
		config.setProperty(PAGE_SIZE_USERPAGE, Integer.toString(globalConfig.getPagePageSize()));
		store();
		return globalConfig;
	}

	private Integer getInt(String key, Integer defaultValue) {
		if (config.containsKey(key)) {
			return Integer.parseInt(config.getProperty(key));
		}
		return defaultValue;
	}

	private void store() {
		try (OutputStream os = new FileOutputStream(resource.getFile())) {
			config.store(os, "");
		} catch (Exception e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Resources.readResource(resource, config::load);
	}

}
