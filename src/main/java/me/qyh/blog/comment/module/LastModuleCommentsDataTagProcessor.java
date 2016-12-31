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
package me.qyh.blog.comment.module;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import com.google.common.collect.Lists;

import me.qyh.blog.comment.base.BaseComment.CommentStatus;
import me.qyh.blog.config.UrlHelper;
import me.qyh.blog.entity.Space;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.ui.ContextVariables;
import me.qyh.blog.ui.data.DataTagProcessor;

public class LastModuleCommentsDataTagProcessor extends DataTagProcessor<List<ModuleComment>> {

	private static final Integer DEFAULT_LIMIT = 10;
	private static final String LIMIT = "limit";
	private static final String QUERY_ADMIN = "queryAdmin";
	private static final String MODULE = "module";

	private static final int MAX_LIMIT = 50;

	@Autowired
	private ModuleCommentService commentService;
	@Autowired
	private UrlHelper urlHelper;

	public LastModuleCommentsDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected List<ModuleComment> buildPreviewData(Space space, Attributes attributes) {
		List<ModuleComment> comments = Lists.newArrayList();
		ModuleComment comment = new ModuleComment();
		comment.setCommentDate(Timestamp.valueOf(LocalDateTime.now()));
		comment.setContent("测试内容");
		comment.setNickname("测试");
		comment.setEmail("test@test.com");
		comment.setGravatar(DigestUtils.md5DigestAsHex("test@test.com".getBytes()));
		comment.setAdmin(true);
		comment.setIp("127.0.0.1");
		CommentModule module = new CommentModule();
		module.setId(-1);
		module.setName("test");
		module.setUrl(urlHelper.getUrl());
		comment.setModule(module);
		comment.setId(-1);
		comment.setStatus(CommentStatus.NORMAL);
		comments.add(comment);
		return comments;
	}

	@Override
	protected List<ModuleComment> query(Space space, ContextVariables variables, Attributes attributes)
			throws LogicException {
		return commentService.queryLastComments(super.getVariables(MODULE, variables, attributes), getLimit(attributes),
				getQueryAdmin(variables, attributes));
	}

	private boolean getQueryAdmin(ContextVariables variables, Attributes attributes) {
		return Boolean.parseBoolean(super.getVariables(QUERY_ADMIN, variables, attributes));
	}

	private int getLimit(Attributes attributes) {
		int limit = DEFAULT_LIMIT;
		String v = attributes.get(LIMIT);
		if (v != null) {
			try {
				limit = Integer.parseInt(v);
			} catch (Exception e) {
			}
		}
		if (limit <= 0) {
			limit = DEFAULT_LIMIT;
		}
		if (limit > MAX_LIMIT) {
			limit = MAX_LIMIT;
		}
		return limit;
	}

}