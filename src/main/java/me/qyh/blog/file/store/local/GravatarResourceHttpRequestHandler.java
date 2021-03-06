package me.qyh.blog.file.store.local;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;

import me.qyh.blog.core.service.GravatarSearcher;
import me.qyh.blog.core.util.FileUtils;

/**
 * 用于缓存gravatar头像，用来解决gravatar有时候访问慢的问题
 *
 */
public class GravatarResourceHttpRequestHandler extends CustomResourceHttpRequestHandler {

	@Autowired
	private StaticResourceUrlHandlerMapping mapping;

	private static final String DEFAULT_GRAVATRA_URL = "https://cn.gravatar.com/avatar/";

	private static final String URL_PREFIX = "/avatar/**";

	private String gravatarServerUrl = DEFAULT_GRAVATRA_URL;

	protected final Path avatarDir;

	/**
	 * 用于头像访问失败或者不存在时显示
	 */
	private Path defaultAvatar;

	private List<GravatarSearcher> gravatarSearchers = new ArrayList<>();

	private static final Logger logger = LoggerFactory.getLogger(GravatarResourceHttpRequestHandler.class);

	private static final String MD5_PATTERN = "[a-fA-F0-9]{32}";

	private static final long CACHE_SECONDS = 7 * 24 * 24 * 60L;

	private long avatarCacheSeconds = CACHE_SECONDS;

	public GravatarResourceHttpRequestHandler(String absoluteAvatarPath) {
		Objects.requireNonNull(absoluteAvatarPath);
		avatarDir = Paths.get(absoluteAvatarPath);
		FileUtils.forceMkdir(avatarDir);
	}

	@Override
	protected Resource getResource(HttpServletRequest request) throws IOException {
		String path = super.getPath(request);
		if (path.startsWith("/avatar")) {
			path = path.substring(7);
		}
		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		if (!path.matches(MD5_PATTERN)) {
			return getDefaultAvatar();
		}

		Path avatar = getAvatarFromLocal(path);
		if (Files.exists(avatar)) {
			long lastModify = -1;
			try {
				lastModify = Files.getLastModifiedTime(avatar).toMillis();
			} catch (IOException e) {
				//
			}
			if (lastModify == -1 || System.currentTimeMillis() - lastModify < this.avatarCacheSeconds * 1000L) {
				return new PathResource(avatar);
			}
		}

		if (!inSystem(path)) {
			return getDefaultAvatar();
		}

		Optional<Path> fromGravatar;
		try {
			fromGravatar = getAvatarFromGravatar(path);
		} catch (IOException e) {
			logger.debug(e.getMessage(), e);
			return getDefaultAvatar();
		}
		if (fromGravatar.isPresent()) {
			return new PathResource(fromGravatar.get());
		}
		return getDefaultAvatar();
	}

	protected Path getAvatarFromLocal(String path) {
		return avatarDir.resolve(path);
	}

	private Resource getDefaultAvatar() {
		return defaultAvatar == null ? null : new PathResource(defaultAvatar);
	}

	@Override
	protected MediaType getMediaType(HttpServletRequest request, Resource resource) {
		return MediaType.IMAGE_JPEG;
	}

	protected Optional<Path> getAvatarFromGravatar(String md5) throws IOException {
		UrlResource resource = new UrlResource(new URL(gravatarServerUrl + md5));
		try (InputStream is = resource.getInputStream()) {
			Path temp = FileUtils.appTemp("jpeg");
			Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
			Path dest = avatarDir.resolve(md5);
			Files.move(temp, dest, StandardCopyOption.REPLACE_EXISTING);
			return Optional.of(dest);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		setCacheSeconds((int) avatarCacheSeconds);

		setLocations(Arrays.asList(new PathResource(avatarDir)));
		super.afterPropertiesSet();

		gravatarSearchers.addAll(BeanFactoryUtils
				.beansOfTypeIncludingAncestors(getApplicationContext(), GravatarSearcher.class, true, false).values());

		mapping.registerResourceHttpRequestHandlerMapping(URL_PREFIX, this);
	}

	public void setGravatarServerUrl(String gravatarServerUrl) {
		Objects.requireNonNull(gravatarServerUrl);
		if (!gravatarServerUrl.endsWith("/")) {
			gravatarServerUrl += "/";
		}
		this.gravatarServerUrl = gravatarServerUrl;
	}

	public void setGravatarSearchers(List<GravatarSearcher> gravatarSearchers) {
		this.gravatarSearchers = gravatarSearchers;
	}

	public void setDefaultAvatarAbsoultePath(String defaultAvatarAbsoultePath) {
		this.defaultAvatar = Paths.get(defaultAvatarAbsoultePath);
	}

	private boolean inSystem(String md5) {
		for (GravatarSearcher searcher : this.gravatarSearchers) {
			if (searcher.contains(md5)) {
				return true;
			}
		}
		return false;
	}

	public void setAvatarCacheSeconds(long avatarCacheSeconds) {
		this.avatarCacheSeconds = avatarCacheSeconds;
	}

}
