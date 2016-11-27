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
package me.qyh.blog.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import me.qyh.blog.bean.BlogFileCount;
import me.qyh.blog.entity.BlogFile;
import me.qyh.blog.pageparam.BlogFileQueryParam;

/**
 * 
 * @author Administrator
 *
 */
public interface BlogFileDao {

	/**
	 * 插入文件纪录
	 * 
	 * @param blogFile
	 *            待插入的文件
	 */
	void insert(BlogFile blogFile);

	/**
	 * 根据id查询文件
	 * 
	 * @param id
	 *            文件id
	 * @return 如果不存在，返回null
	 */
	BlogFile selectById(Integer id);

	/**
	 * 当插入新节点时，更新父节点的左值
	 * 
	 * @param parent
	 *            父节点
	 */
	void updateLftWhenAddChild(BlogFile parent);

	/**
	 * 当插入新节点时，更新父节点的右值
	 * 
	 * @param parent
	 *            父节点
	 */
	void updateRgtWhenAddChild(BlogFile parent);

	/**
	 * 删除节点时，更新受影响节点的左值
	 * 
	 * @param toDelete
	 *            待删除的节点
	 */
	void updateLftWhenDelete(BlogFile toDelete);

	/**
	 * 删除节点时，更新受影响节点的右值
	 * 
	 * @param toDelete
	 *            待删除的节点
	 */
	void updateRgtWhenDelete(BlogFile toDelete);

	/**
	 * 查询文件数目
	 * 
	 * @param param
	 *            查询参数
	 * @return 文件数目
	 */
	int selectCount(BlogFileQueryParam param);

	/**
	 * 查询文件集合
	 * 
	 * @param param
	 *            查询参数
	 * @return 文件集合
	 */
	List<BlogFile> selectPage(BlogFileQueryParam param);

	/**
	 * 根据路径查询改路径上传的所有文件
	 * 
	 * @param node
	 *            节点
	 * @return 文件集合
	 */
	List<BlogFile> selectPath(BlogFile node);

	/**
	 * 查询该节点下的文件类型以及对应的数目
	 * 
	 * @param parent
	 *            父文件节点
	 * @return 结果集
	 */
	List<BlogFileCount> selectSubBlogFileCount(BlogFile parent);

	/**
	 * 查询该文件下所有文件的大小
	 * 
	 * @param parent
	 *            父节点
	 * @return 总大小， 如果为空文件夹，返回0
	 */
	long selectSubBlogFileSize(BlogFile parent);

	/**
	 * 查询根节点
	 * 
	 * @return 根节点
	 */
	BlogFile selectRoot();

	/**
	 * 更新文件节点
	 * 
	 * @param toUpdate
	 *            待更新的文件节点
	 */
	void update(BlogFile toUpdate);

	/**
	 * 删除
	 * 
	 * @param db
	 *            待删除节点
	 */
	void delete(BlogFile db);

	/**
	 * 删除该文件对象对应文件记录
	 * 
	 * @param db
	 *            文件独享
	 * @return 被删的普通文件纪录数目
	 */
	int deleteCommonFile(BlogFile db);

	/**
	 * 查询父节点的所有子节点
	 * 
	 * @param p
	 *            父节点
	 * @return 所有子节点
	 */
	List<BlogFile> selectChildren(BlogFile p);

	/**
	 * 根据父节点和路径查询节点
	 * 
	 * @param parent
	 *            父节点
	 * @param path
	 *            路径
	 * @return 如果不存在，返回null。否在返回已经存在的节点对象
	 */
	BlogFile selectByParentAndPath(@Param("parent") BlogFile parent, @Param("path") String path);

	/**
	 * 删除没有关联的文件信息
	 */
	void deleteUnassociateCommonFile();
}
