package me.coolclk.wdisk;

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
import org.springframework.web.multipart.MultipartFile;

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
		@RequestMapping(value = "api/login", method = { RequestMethod.GET, RequestMethod.POST })
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
		@RequestMapping(value = "api/logined", method = { RequestMethod.GET, RequestMethod.POST })
		public Map<String, Object> checkUserLogin(HttpServletRequest httpServletRequest) {
			Map<String, Object> requestMap = new HashMap<>();
			requestMap.put("pass", APPLICATIONPROPERTIES.getPassword().trim().equals(""));
			if (httpServletRequest.getCookies() != null) {
				Cookie cookie = Arrays.stream(httpServletRequest.getCookies()).filter(c -> Objects.equals(c.getName(), APPLICATIONPROPERTIES.getLoginCookieName())).findAny().orElse(null);
				if (cookie != null) {
					requestMap.put("correct", Objects.equals(cookie.getValue(), APPLICATIONPROPERTIES.getPassword()));
				}
				requestMap.put("logined", cookie != null);
			}
			requestMap.put("pass", (boolean) requestMap.getOrDefault("logined", false) && (boolean) requestMap.getOrDefault("correct", false));
			return requestMap;
		}

		@ResponseBody
		@RequestMapping(value = "api/permission", method = { RequestMethod.GET, RequestMethod.POST })
		public Map<String, Object> checkPermission(HttpServletRequest httpServletRequest) {
			Map<String, Object> requestMap = new HashMap<>();
			boolean admin = (boolean) checkUserLogin(httpServletRequest).getOrDefault("pass", false);
			requestMap.put("admin", admin);
			Map<String, Boolean> permissionMap = new HashMap<>();
			permissionMap.put("listFiles", APPLICATIONPROPERTIES.isAllowGuestListFiles() || admin);
			permissionMap.put("downloadFiles", APPLICATIONPROPERTIES.isAllowGuestDownloadFiles() || admin);
			permissionMap.put("controlFiles", APPLICATIONPROPERTIES.isAllowGuestControlFiles() || admin);
			requestMap.put("permission", permissionMap);
			return requestMap;
		}

		@ResponseBody
		@RequestMapping(value = "api/listFiles", method = { RequestMethod.GET, RequestMethod.POST })
		public Map<String, Object> listDiskFiles(HttpServletRequest httpServletRequest, @RequestParam Map<String, Object> requestParam) {
			Map<String, Object> requestMap = new HashMap<>();
			if ((boolean) checkUserLogin(httpServletRequest).getOrDefault("pass", false) || APPLICATIONPROPERTIES.isAllowGuestListFiles()) {
				requestMap.put("permission", true);
				List<Map<String, Object>> fileList = new ArrayList<>();
				String pathKey = "";
				if (requestParam.containsKey("path") && requestParam.get("path") != "") {
					pathKey = requestParam.get("path") + "/";
				}
				File disk = new File(APPLICATIONPROPERTIES.getRealPath() + "/" + pathKey);
				if (disk.exists() && disk.isDirectory()) {
					File[] oldDiskFiles = disk.listFiles();
					List<File> diskFiles;
					if (oldDiskFiles != null) {
						diskFiles = List.of(oldDiskFiles);
						String searchKey = null;
						if (requestParam.containsKey("search") && requestParam.get("search") != "") {
							searchKey = (String) requestParam.get("search");
						}
						for (File file : diskFiles) {
							String name = file.getName();
							boolean searchPass = true;
							if (searchKey != null) {
								for (String k : (searchKey.contains(" ")) ? searchKey.toLowerCase().split(" ") : new String[]{searchKey}) {
									if (!name.toLowerCase().contains(k)) {
										searchPass = false;
										break;
									}
								}
							}
							if (searchPass) {
								String path = pathKey + name;
								fileList.add(new HashMap<>(Map.of("name", name, "directory", file.isDirectory(), "size", file.length(), "realPath", path, "downloadUrl", "api/downloadFile?file=" + path, "deleteUrl", "api/deleteFile?file=" + path)));
							}
						}
					}
				}
				requestMap.put("list", fileList);
			} else {
				requestMap.put("permission", false);
			}
			return requestMap;
		}

		@RequestMapping(value = "api/downloadFile", method = { RequestMethod.GET, RequestMethod.POST })
		public void downloadDiskFile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @RequestParam(value = "file") String filepath) {
			File file = new File(APPLICATIONPROPERTIES.getRealPath() + "/" + filepath);
			if ((boolean) checkUserLogin(httpServletRequest).getOrDefault("pass", false) || APPLICATIONPROPERTIES.isAllowGuestDownloadFiles()) {
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
					} catch (IOException ignored) {
					}
				}
			}
		}

		@PostMapping(value = "api/uploadFile")
		public Map<String, Object> uploadFileToDisk(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @RequestParam Map<String, Object> requestParam) {
			Map<String, Object> requestMap = new HashMap<>();
			requestMap.put("status", 0);
			if ((boolean) checkUserLogin(httpServletRequest).getOrDefault("pass", false) || APPLICATIONPROPERTIES.isAllowGuestControlFiles()) {
				requestMap.put("permission", true);
				if (requestParam.containsKey("file")) {
					String path = APPLICATIONPROPERTIES.getRealPath() + "/";
					if (requestParam.containsKey("path")) {
						path += requestParam.get("path") + "/";
					}
					if (new File(path).exists()) {
						try {
							MultipartFile[] files = (MultipartFile[]) requestParam.get("file");
							for (MultipartFile file : files) {
								if (!file.isEmpty()) {
									file.transferTo(new File(path + file.getName()));
								}
							}
							requestMap.put("status", 1);
						} catch (Exception ignored) {
						}
					}
				}
			} else {
				requestMap.put("permission", false);
			}
			return requestMap;
		}

		@RequestMapping(value = "api/renameFile")
		public Map<String, Object> renameDiskFile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @RequestParam Map<String, Object> requestParam) {
			Map<String, Object> requestMap = new HashMap<>();
			requestMap.put("status", 0);
			if ((boolean) checkUserLogin(httpServletRequest).getOrDefault("pass", false) || APPLICATIONPROPERTIES.isAllowGuestControlFiles()) {
				requestMap.put("permission", true);
				if (requestParam.containsKey("file") && requestParam.containsKey("name")) {
					String name = (String) requestParam.get("name");
					File file = new File(APPLICATIONPROPERTIES.getRealPath() + "/" + requestParam.get("file"));
					if (file.exists()) {
						if (file.renameTo(new File(APPLICATIONPROPERTIES.getRealPath() + "/" + replaceLast((String) requestParam.get("file"), file.getName(), name)))) {
							requestMap.put("status", 1);
						}
					}
				}
			} else {
				requestMap.put("permission", false);
			}
			return requestMap;
		}

		@ResponseBody
		@RequestMapping(value = "api/createFolder")
		public Map<String, Object> createFolderToDisk(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @RequestParam Map<String, Object> requestParam) {
			Map<String, Object> requestMap = new HashMap<>();
			requestMap.put("status", 0);
			if ((boolean) checkUserLogin(httpServletRequest).getOrDefault("pass", false) || APPLICATIONPROPERTIES.isAllowGuestControlFiles()) {
				requestMap.put("permission", true);
				if (requestParam.containsKey("path")) {
					File directory = new File(APPLICATIONPROPERTIES.getRealPath() + "/" + requestParam.get("path"));
					if (directory.exists() || directory.mkdirs()) {
						requestMap.put("status", 1);
					}
				}
			} else {
				requestMap.put("permission", false);
			}
			return requestMap;
		}

		@ResponseBody
		@RequestMapping(value = "api/deleteFile", method = { RequestMethod.GET, RequestMethod.POST })
		public Map<String, Object> deleteDiskFile(HttpServletRequest httpServletRequest, @RequestParam Map<String, Object> requestParam) {
			Map<String, Object> requestMap = new HashMap<>();
			requestMap.put("status", 0);
			if ((boolean) checkUserLogin(httpServletRequest).getOrDefault("pass", false) || APPLICATIONPROPERTIES.isAllowGuestControlFiles()) {
				requestMap.put("permission", true);
				if (requestParam.containsKey("file") && requestParam.get("file") != "") {
					File file = new File(APPLICATIONPROPERTIES.getRealPath() + "/" + requestParam.get("file"));
					if (file.exists()) {
						if (file.delete()) {
							requestMap.put("status", 1);
						}
					}
				}
			} else {
				requestMap.put("permission", false);
			}
			return requestMap;
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	public static String replaceLast(String string, String match, String replace) {
		StringBuilder sBuilder = new StringBuilder(string);
		int lastIndexOf = sBuilder.lastIndexOf(match);
		if (-1 == lastIndexOf) {
			return string;
		}

		return sBuilder.replace(lastIndexOf, lastIndexOf + match.length(), replace).toString();
	}
}
