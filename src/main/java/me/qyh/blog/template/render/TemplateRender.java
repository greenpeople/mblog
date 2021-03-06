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
package me.qyh.blog.template.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.CollectionUtils;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.exception.LockException;
import me.qyh.blog.core.exception.RuntimeLogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Messages;
import me.qyh.blog.core.security.AuthencationException;
import me.qyh.blog.core.util.ExceptionUtils;
import me.qyh.blog.core.util.Formats;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.core.util.StringUtils;
import me.qyh.blog.core.util.Times;
import me.qyh.blog.core.util.Validators;
import me.qyh.blog.core.vo.LockBean;
import me.qyh.blog.web.Webs;
import me.qyh.blog.web.lock.LockHelper;

/**
 * 用来将模板解析成字符串
 * 
 * @author Administrator
 *
 */
public final class TemplateRender implements InitializingBean {

	@Autowired
	private PlatformTransactionManager transactionManager;
	@Autowired
	private TemplateRenderExecutor templateRenderer;
	@Autowired
	private TemplateExceptionTranslater templateExceptionTranslater;
	@Autowired
	private Messages messages;

	private Map<String, Object> pros = new HashMap<>();

	private List<TemplateRenderHandler> renderHandlers = new ArrayList<>();

	public RenderResult doRender(String templateName, Map<String, ?> model, HttpServletRequest request,
			ReadOnlyResponse response, ParseConfig config) throws Exception {
		ParseContextHolder.getContext().setConfig(config);
		try {
			String content = doRender(templateName, model, request, response);
			MediaType type = ParseContextHolder.getContext().getMediaType();
			RenderResult result = new RenderResult(type, content);
			if (!renderHandlers.isEmpty()) {
				for (TemplateRenderHandler handler : renderHandlers) {
					if (handler.match(templateName)) {
						handler.afterRender(result, request);
					}
				}
			}
			return result;
		} catch (Throwable e) {
			markRollBack();

			// 从异常栈中寻找 逻辑异常
			Optional<Throwable> finded = ExceptionUtils.getFromChain(e, RuntimeLogicException.class,
					LockException.class, AuthencationException.class, RedirectException.class, MissLockException.class);
			if (finded.isPresent()) {
				throw (Exception) finded.get();
			}

			// 如果没有逻辑异常，转化模板异常
			Optional<TemplateRenderException> optional = templateExceptionTranslater.translate(templateName, e);
			if (optional.isPresent()) {
				throw optional.get();
			}

			throw new SystemException(e.getMessage(), e);

		} finally {
			try {
				commit();
			} finally {
				ParseContextHolder.remove();
			}
		}
	}

	private void markRollBack() {
		TransactionStatus status = ParseContextHolder.getContext().getTransactionStatus();
		if (status != null) {
			status.setRollbackOnly();
		}
	}

	private void commit() {
		TransactionStatus status = ParseContextHolder.getContext().getTransactionStatus();
		if (status != null) {
			transactionManager.commit(status);
		}
	}

	private String doRender(String viewTemplateName, final Map<String, ?> model, final HttpServletRequest request,
			final ReadOnlyResponse response) throws Exception {
		Map<String, Object> _model = model == null ? new HashMap<>() : new HashMap<>(model);
		if (!CollectionUtils.isEmpty(pros)) {
			_model.putAll(pros);
		}
		_model.put("messages", messages);
		_model.put("urls", Webs.getSpaceUrls(request));
		_model.put("user", Environment.getUser());
		_model.put("space", Environment.getSpace());
		_model.put("ip", Environment.getIP());

		if (Webs.unlockRequest(request)) {
			String unlockId = request.getParameter("unlockId");
			LockBean lockBean = LockHelper.getLockBean(request, unlockId)
					.orElseThrow(() -> new MissLockException(unlockId));
			_model.put("lock", lockBean.getLock());
			_model.put("unlockId", lockBean.getId());
		}

		return templateRenderer.execute(viewTemplateName, _model, request, response);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		pros.put("validators", Validators.class);
		pros.put("jsons", Jsons.class);
		pros.put("strings", StringUtils.class);
		pros.put("times", Times.class);
		pros.put("formats", Formats.class);
	}

	public void setPros(Map<String, Object> pros) {
		this.pros = pros;
	}

	public void setRenderHandlers(List<TemplateRenderHandler> renderHandlers) {
		Objects.requireNonNull(renderHandlers);
		this.renderHandlers = renderHandlers;
	}

}
