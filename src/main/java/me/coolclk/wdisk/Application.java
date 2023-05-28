package me.coolclk.wdisk;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
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
				File disk = new File(APPLICATIONPROPERTIES.getRealPath());
				if (disk.exists() && disk.isDirectory()) {
					File[] diskFiles = disk.listFiles();
					if (diskFiles != null) {
						String searchKey = null;
						if (requestParam.containsKey("search")) {
							searchKey = (String) requestParam.get("search");
						}
						for (File file : diskFiles) {
							String name = file.getName();
							if (searchKey == null || name.contains(searchKey)) {
								String path = "" + name;
								fileList.add(new HashMap<>(Map.of("name", name, "directory", file.isDirectory(), "size", file.length(), "rawUrl", "raw/" + path, "deleteUrl", "api/deleteFile?file=" + path)));
							}
						}
					}
				}
			}
			requestMap.put("list", fileList);
			return requestMap;
		}

		@ResponseBody
		@GetMapping(value = "raw/{file}")
		public void getRawFile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathParam(value = "file") String filepath) {
			File file = new File(APPLICATIONPROPERTIES.getRealPath() + "/" + filepath);
			if (file.exists() && file.isFile()) {
				httpServletResponse.reset();
				httpServletResponse.setCharacterEncoding("UTF-8");
				httpServletResponse.setContentType("application/octet-stream");
				httpServletResponse.setHeader("Content-Disposition", "attachment;fileName=" + file.getName());
				httpServletResponse.addHeader("Content-Length", String.valueOf(file.length()));
				httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
				try {
					FileInputStream fileInputStream = new FileInputStream(file);
					InputStream fis = new BufferedInputStream(fileInputStream);
					byte[] buffer = new byte[fis.available()];
					fis.read(buffer);
					fis.close();
					BufferedOutputStream os = new BufferedOutputStream(httpServletResponse.getOutputStream());
					os.write(buffer);
					os.flush();
					fis.close();
					fileInputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
