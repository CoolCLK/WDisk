package me.coolclk.wdisk;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@SpringBootApplication
@RestController
public class Application {
	public static Logger LOGGER = LoggerFactory.getILoggerFactory().getLogger("");
	@Controller
	static class ApplicationPage {
		@Autowired
		private ApplicationProperties APPLICATIONPROPERTIES;

		@ResponseBody
		@PostMapping(value = "api/login")
		public Map<String, Object> userLogin(HttpServletResponse httpServletResponse, @RequestParam Map<String, Object> requestParam) {
			Map<String, Object> requestMap = new HashMap<>();
			requestMap.put("correct", Objects.equals(requestParam.get("password"), APPLICATIONPROPERTIES.getPassword()));
			if ((boolean) requestMap.get("correct")) {
				Cookie cookie = new Cookie(APPLICATIONPROPERTIES.getLoginCookieName(), (String) requestParam.get("password"));
				cookie.setMaxAge(APPLICATIONPROPERTIES.getLoginCookieLife());
				cookie.setPath("/");
				httpServletResponse.addCookie(cookie);
			}
			return requestMap;
		}

		@ResponseBody
		@RequestMapping(value = "api/logined")
		public Map<String, Object> checkUserLogin(HttpServletRequest httpServletRequest) {
			Map<String, Object> requestMap = new HashMap<>();
			Cookie cookie = Arrays.stream(httpServletRequest.getCookies()).filter(c -> Objects.equals(c.getName(), APPLICATIONPROPERTIES.getLoginCookieName())).findAny().orElse(null);
			if (cookie != null) {
				requestMap.put("correct", Objects.equals(cookie.getValue(), APPLICATIONPROPERTIES.getPassword()));
			}
			requestMap.put("logined", cookie != null);
			return requestMap;
		}

		@ResponseBody
		@RequestMapping(value = "api/listFiles")
		public Map<String, Object> listDiskFiles(HttpServletRequest httpServletRequest, @RequestParam Map<String, Object> requestParam) {
			Map<String, Object> requestMap = new HashMap<>();
			List<Map<String, Object>> fileList = new ArrayList<>();
			if ((boolean) checkUserLogin(httpServletRequest).getOrDefault("correct", false)) {
				String pathKey = "";
				if (requestParam.containsKey("path") && requestParam.get("path") != "") {
					pathKey = (String) requestParam.get("path") + "/";
				}
				File disk = new File(APPLICATIONPROPERTIES.getRealPath() + "/" + pathKey);
				if (disk.exists() && disk.isDirectory()) {
					File[] diskFiles = disk.listFiles();
					if (diskFiles != null) {
						String searchKey = null;
						if (requestParam.containsKey("search") && requestParam.get("search") != "") {
							searchKey = (String) requestParam.get("search");
						}
						for (File file : diskFiles) {
							String name = file.getName();
							if (searchKey == null || name.contains(searchKey)) {
								String path = pathKey + name;
								fileList.add(new HashMap<>(Map.of("name", name, "directory", file.isDirectory(), "size", file.length(), "realPath", path, "rawUrl", "raw/" + path, "deleteUrl", "api/deleteFile?file=" + path)));
							}
						}
					}
				}
			}
			requestMap.put("list", fileList);
			return requestMap;
		}

		@ResponseBody
		@RequestMapping(value = "api/deleteFile")
		public Map<String, Object> deleteDiskFile(HttpServletRequest httpServletRequest, @RequestParam Map<String, Object> requestParam) {
			Map<String, Object> requestMap = new HashMap<>();
			requestMap.put("status", 0);
			if ((boolean) checkUserLogin(httpServletRequest).getOrDefault("correct", false)) {
				if (requestParam.containsKey("file") && requestParam.get("file") != "") {
					File file = new File(APPLICATIONPROPERTIES.getRealPath() + "/" + requestParam.get("file"));
					if (file.exists()) {
						if (file.delete()) {
							requestMap.put("status", 1);
						}
					}
				}
			}
			return requestMap;
		}

		@GetMapping(value = "raw/{file}/**")
		public void getRawFile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable(value = "file") String filepath) {
			File file = new File(APPLICATIONPROPERTIES.getRealPath() + "/" + filepath);
			if (file.exists() && file.isFile()) {
				try (FileInputStream inputStream = new FileInputStream(file); OutputStream outputStream = httpServletResponse.getOutputStream()) {
					byte[] data = new byte[1024];
					httpServletResponse.reset();
					httpServletResponse.setCharacterEncoding("UTF-8");
					httpServletResponse.setContentType("application/x-msdownload");
					httpServletResponse.setHeader("Accept-Ranges", "bytes");
					httpServletResponse.addHeader("Content-Length", String.valueOf(file.length()));
					httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
					int read;
					while ((read = inputStream.read(data)) != -1) {
						outputStream.write(data, 0, read);
					}
					outputStream.flush();
				} catch (IOException ignored) { }
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
