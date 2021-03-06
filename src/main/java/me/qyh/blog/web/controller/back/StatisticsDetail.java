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
package me.qyh.blog.web.controller.back;

import me.qyh.blog.comment.vo.CommentStatistics;
import me.qyh.blog.core.vo.ArticleDetailStatistics;
import me.qyh.blog.core.vo.TagDetailStatistics;
import me.qyh.blog.file.vo.FileStatistics;
import me.qyh.blog.file.vo.StaticFileStatistics;
import me.qyh.blog.template.vo.PageStatistics;

public class StatisticsDetail {
	private ArticleDetailStatistics articleStatistics;
	private TagDetailStatistics tagStatistics;
	private CommentStatistics commentStatistics;
	private PageStatistics pageStatistics;
	private FileStatistics fileStatistics;
	private StaticFileStatistics staticFileStatistics;

	public ArticleDetailStatistics getArticleStatistics() {
		return articleStatistics;
	}

	public void setArticleStatistics(ArticleDetailStatistics articleStatistics) {
		this.articleStatistics = articleStatistics;
	}

	public TagDetailStatistics getTagStatistics() {
		return tagStatistics;
	}

	public void setTagStatistics(TagDetailStatistics tagStatistics) {
		this.tagStatistics = tagStatistics;
	}

	public CommentStatistics getCommentStatistics() {
		return commentStatistics;
	}

	public void setCommentStatistics(CommentStatistics commentStatistics) {
		this.commentStatistics = commentStatistics;
	}

	public PageStatistics getPageStatistics() {
		return pageStatistics;
	}

	public void setPageStatistics(PageStatistics pageStatistics) {
		this.pageStatistics = pageStatistics;
	}

	public FileStatistics getFileStatistics() {
		return fileStatistics;
	}

	public void setFileStatistics(FileStatistics fileStatistics) {
		this.fileStatistics = fileStatistics;
	}

	public StaticFileStatistics getStaticFileStatistics() {
		return staticFileStatistics;
	}

	public void setStaticFileStatistics(StaticFileStatistics staticFileStatistics) {
		this.staticFileStatistics = staticFileStatistics;
	}

}