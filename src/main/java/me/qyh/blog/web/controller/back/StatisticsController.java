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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.comment.service.CommentService;
import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.ArticleService;
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.service.TagService;
import me.qyh.blog.core.vo.SpaceQueryParam;
import me.qyh.blog.file.service.FileService;
import me.qyh.blog.file.store.local.EditablePathResourceHttpRequestHandler;
import me.qyh.blog.template.service.TemplateService;

@Controller
@RequestMapping("mgr/statistics")
public class StatisticsController extends BaseMgrController  {

	@Autowired
	private FileService fileService;
	@Autowired
	private ArticleService articleService;
	@Autowired
	private CommentService commentService;
	@Autowired
	private TagService tagService;
	@Autowired
	private TemplateService templateService;
	@Autowired
	private SpaceService spaceService;
	@Autowired(required = false)
	private EditablePathResourceHttpRequestHandler editablePathResourceHttpRequestHandler;

	@GetMapping
	public String queryStatisticsDetail(@RequestParam(value = "spaceId", required = false) Integer spaceId,
			ModelMap model, RedirectAttributes ra) {
		model.addAttribute("spaces", spaceService.querySpace(new SpaceQueryParam()));
		try {
			Space space = spaceId == null ? null : spaceService.getSpace(spaceId)
					.orElseThrow(() -> new LogicException("space.notExists", "空间不存在"));
			StatisticsDetail detail = new StatisticsDetail();
			detail.setArticleStatistics(articleService.queryArticleDetailStatistics(space));
			detail.setCommentStatistics(commentService.queryCommentStatistics(space));
			
			if(space == null){
				detail.setFileStatistics(fileService.queryFileStatistics());
			}
			
			detail.setPageStatistics(templateService.queryPageStatistics(space));
			detail.setTagStatistics(tagService.queryTagDetailStatistics(space));
			
			if(space == null && editablePathResourceHttpRequestHandler != null){
				detail.setStaticFileStatistics(editablePathResourceHttpRequestHandler.queryFileStatistics());
			}
			
			model.addAttribute("statistics", detail);
			return "mgr/statistics/index";
		} catch (LogicException e) {
			ra.addFlashAttribute(Constants.ERROR, e.getLogicMessage());
			return "redirect:/mgr/statistics";
		}
	}

}
