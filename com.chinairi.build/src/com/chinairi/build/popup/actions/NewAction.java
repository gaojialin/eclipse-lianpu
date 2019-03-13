package com.chinairi.build.popup.actions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.internal.Workbench;

public class NewAction implements IObjectActionDelegate {
	private static final String[] EXCLUDEPRO = new String[] { "position", "zhCNpro", "type", "isSearch",
			"isDatatable", "scale", "entity", "field" };
	private static final String[] DATACOLUMNEPRO = new String[] {"pattern", "key", "unit", "field",
			"width", "className", "entity", "field" };
	private static final String[] VIEWPRO = new String[] {"pattern", "key", "unit",
			"width", "bindPath", "entity", "field" };
	private static final String classPaht = "\\src\\main\\resources\\";
	private static final String TEPLE_JSP = "\\WebContent\\temp\\itag\\initAddPage.jsp";
	private static final String WEB_CONTENT_JSP = "\\WebContent\\jsp\\";
	private static final String PAGENM = "\\init.jsp";
	private static final String PRO_ZH_CN = "_zh_CN.properties";
	private Shell shell;
	private static String tempStr;
	private static String temp;
	private static String parjectPath;
	private static MessageConsoleStream printer;

	/**
	 * Constructor for Action1.
	 */
	public NewAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		/*TagClass tagClass = new TagClass();
		tagClass.getFields("");*/
		printer = ConsoleFactory.getConsole().newMessageStream();
		// printer.setActivateOnWrite(activate);
		printer.println("[INFO] " + getDateStr(new Date()) + "  开始构建……");
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText("选择数据模型文件(xls,xlsx)");
		dialog.setFilterExtensions(new String[] { "*.*", "*.xml", "*.txt", "*.asmx" });
		String filePath = dialog.open();
		printer.println("[INFO] " + getDateStr(new Date()) + "  开始读取文件  " + filePath);
		// System.out.println(getDateStr(new Date())+" 开始读取 "+filePath);
		IProject project = getCurrentProject();
		parjectPath = project.getLocation().toString();
		if (filePath != null) {
			try {
				tempStr = getStandTemple(parjectPath + TEPLE_JSP);
				if (tempStr != null) {
					List<PageInfo> pages = getExcelData(filePath);
					printer.println("[INFO] " + getDateStr(new Date()) + "  文件读取成功!");
					// System.out.println(getDateStr(new Date())+" 数据读取完毕");
					int i = 1;
					for (PageInfo page : pages) {
						temp = tempStr;
						printer.println(
								"[INFO] " + getDateStr(new Date()) + "  正在生成第  " + i + " 个任务 ：" + page.getPagePath());
						// System.out.println(getDateStr(new Date())+ i +
						// "……正在生成第" + page.getPagePath());
						getPageInfos(page, page.getTags());
						// System.out.println(getDateStr(new Date())+i + " Done
						// " + 1 + page.getPagePath());
						printer.println(
								"[INFO] " + getDateStr(new Date()) + "  生成成功第  " + i + " 个任务 ：" + page.getPagePath()+" ("+page.getTitle()+")");
						i++;
					}
					MessageDialog.openInformation(shell, "", "😀  successed! 构建成功 ,请进行确认.😀  \n"+ "如有疑问请联系 gaojl@iri.cn");
				} else {
					printer.println("[ERROR] " + getDateStr(new Date()) + "  文件读取失败!");
					//System.out.println(getDateStr(new Date()) + " 文件读取失败");
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				printer.println("[ERROR] " + getDateStr(new Date()) + "\n"+e1.getMessage());
				e1.printStackTrace();
				MessageDialog.openInformation(shell, "Error", "😭 构建失败. 😭  \n");
			}
		}

	}

	public static String getDateStr(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	public static List<PageInfo> getExcelData(String fileName) throws IOException {
		File file = new File(fileName);
		List<PageInfo> pages = new ArrayList<>();
		BufferedInputStream bin = null;
		try {
			bin = new BufferedInputStream(new FileInputStream(file));
			Workbook wb = WorkbookFactory.create(bin);
			if (wb != null) {
				for (int i = 0; i < wb.getNumberOfSheets(); i++) {
					PageInfo pageInfo = new PageInfo();
					Sheet sheet = wb.getSheetAt(i);
					List<Map<String, String>> list = new ArrayList<>();
					setTyxx(pageInfo, sheet);
					pageInfo.setTags(list);
					pages.add(pageInfo);
					Row row3 = sheet.getRow(2);
					for (int r = 3; r <= sheet.getLastRowNum(); r++) {
						Map<String, String> map = new HashMap<>();
						for (int c = 0; c < row3.getLastCellNum(); c++) {
							if (StringUtils.isBlank(ExcelUtils.getValue(sheet, 2, c))) {
								break;
							}
							String value = ExcelUtils.getValue(sheet, r, c);
							if (StringUtils.isNotBlank(value)) {
								if ("scale".equals(ExcelUtils.getValue(row3.getCell(c)))) {
									if (ExcelUtils.getValue(sheet, r, 4).toLowerCase().equals("numbertextbox")) {
										String[] scales = value.split(",");
										if (scales.length > 1) {
											map.put("integerPlaces", String.valueOf(Integer.valueOf(value.split(",")[0])
													- Integer.valueOf(value.split(",")[1])).replace(".0", ""));
											map.put("decimalPlaces", value.split(",")[1].replace(".0", ""));
										} else {
											map.put("integerPlaces", value.split(",")[0].replace(".0", ""));
											map.put("decimalPlaces", "0");
										}
									} else {
										map.put("maxLength",
												String.valueOf(Integer.valueOf(value.split(",")[0].replace(".0", ""))));
									}
								} else if ("name".equals(ExcelUtils.getValue(row3.getCell(c)))
										&& !"uploadButton".equals(ExcelUtils.getValue(sheet, r, 4))
										&& !"Y".equals(ExcelUtils.getValue(sheet, r, 5))) {
									map.put("name", pageInfo.getEntityName() + "." + value);
								} else {
									if (NumberUtils.isCreatable(value)) {
										value = value.replace(".0", "");
									}
									map.put(ExcelUtils.getValue(row3.getCell(c)), value);
								}
							} else {
								if ("dataSource".equals(ExcelUtils.getValue(row3.getCell(c)))
										&& "select".equals(ExcelUtils.getValue(sheet, r, 4))) {
									map.put("dataSource", ExcelUtils.getValue(sheet, r, 2) + "List");
								}
								if ("key".equals(ExcelUtils.getValue(row3.getCell(c)))) {
									map.put("key", ExcelUtils.getValue(sheet, r, 2));
								}
							}
						}
						list.add(map);
					}
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bin != null) {
				bin.close();
			}
		}
		return pages;

	}

	private static void setTyxx(PageInfo pageInfo, Sheet sheet) throws UnsupportedEncodingException {
		pageInfo.setCommonClassPath(ExcelUtils.getValue(sheet, 0, 0));
		pageInfo.setCategory(ExcelUtils.getValue(sheet, 0, 1));
		pageInfo.setClassName(ExcelUtils.getValue(sheet, 0, 2));
		pageInfo.setPagePath(ExcelUtils.getValue(sheet, 1, 0));
		pageInfo.setEntityName(ExcelUtils.getValue(sheet, 1, 1));
		pageInfo.setTitle(sheet.getSheetName());
	}

	public IProject getCurrentProject() {
		ISelectionService selectionService = Workbench.getInstance().getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = selectionService.getSelection();
		IProject project = null;
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IResource) {
				project = ((IResource) element).getProject();
			} else if (element instanceof IJavaElement) {
				IJavaProject jProject = ((IJavaElement) element).getJavaProject();
				project = jProject.getProject();
			}
		}
		return project;
	}

	public static String getStandTemple(String filename) throws IOException {
		BufferedReader bin = null;
		StringBuilder sb = null;
		try {
			File file = new File(filename);
			System.out.println(file.getAbsolutePath());
			bin = new BufferedReader(new FileReader(file));
			sb = new StringBuilder();
			String s;
			while ((s = bin.readLine()) != null) {
				sb.append(s);
				sb.append("\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			bin.close();
		}
		return sb.toString();
	}

	// 读取jsp标准模板
	// 读取EXCEL数据
	// 填充jsp模板数据
	// 生成各个jsp页面
	public static Map<String, String> getPageInfos(PageInfo page, List<Map<String, String>> data) throws IOException {
		Map<String, String> tags = new HashMap<>();
		StringBuilder addfromtagsString = null;
		StringBuilder datatagsString = new StringBuilder("		<i:idTemplateField>\n");
		String position = "";
		Map<String, String> zhcn = new HashMap<>();
		for (int i = 0; i <= data.size(); i++) {
			if (i == data.size()) {
				tags.put(position, addfromtagsString.toString());
				break;
			}
			Map<String, String> Tag = data.get(i);
			StringBuilder tagStringb = new StringBuilder("			<i:" + Tag.get("type"));
			StringBuilder DatatagString = new StringBuilder("			<i:" + getDataColumTag(Tag.get("type")));
			String tagType = Tag.get("type");
			if (i == 0) {
				datatagsString.append("				<i:keyField entityKey=\"" + page.getEntityName()
						+ ".dbid\" listKey=\"dbid\" />\n		</i:idTemplateField>\n");
			}
			// inputTag 解析
			waveFormInput(Tag, tagStringb, tagType);
			waveDatatable(page, datatagsString, Tag, DatatagString);
			zhcn.put(Tag.get("key"), Tag.get("zhCNpro"));
			if (StringUtils.isBlank(position) || !position.equals(Tag.get("position"))) {
				if (StringUtils.isNotBlank(position)) {
					tags.put(position, addfromtagsString.toString());
				}
				addfromtagsString = new StringBuilder("");
				// 初始化，position=""
				addfromtagsString.append(tagStringb.toString());
				position = Tag.get("position");
			} else {
				addfromtagsString.append(tagStringb.toString());
			}
		}
		tags.put("dataContent", datatagsString.toString());
		// tags.put("pagePage", "mtxx");
		generalJspFile(page, tags);
		generatePropertyFile(page, zhcn);
		return tags;
	}

	public static String getDataColumTag(String inputname) {
		switch (inputname.toLowerCase()) {
		case "select":
		case "textbox":
		case "textarea":
			return "stringColumn";
		case "numbertextbox":
			return "numberColumn";
		case "datetextbox":
			return "dateColumn";
		case "uploadbutton":
			return "downloadCommandColumn";
		}
		return inputname;
	}

	/**
	 * 生成jsp页面
	 * 
	 * @param page
	 * @param map
	 * @throws IOException
	 */
	public static void generalJspFile(PageInfo page, Map<String, String> map) throws IOException {
		File file = new File(parjectPath + WEB_CONTENT_JSP + page.getCategory() + "\\" + page.getPagePath());
		file.mkdirs();
		//System.out.println(file.getAbsolutePath());
		printer.println("[INFO] " +page.getTitle());
		BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsolutePath() + PAGENM));
		map.put("title", new String(page.getTitle().getBytes("UTF-8"),"GBK"));
		for (String tem : map.keySet()) {
			temp = temp.replace("%{" + tem + "}", map.get(tem));
		}
		bw.write(temp);
		bw.flush();
		bw.close();
	}

	/**
	 * 生成国际化属性文件
	 * 
	 * @param page
	 * @param map
	 */

	public static void generatePropertyFile(PageInfo page, Map<String, String> map) {
		String classPath = page.getCommonClassPath();
		if (StringUtils.isNotBlank(classPath)) {
			File file = new File(parjectPath + classPaht + classPath + "\\" + page.getCategory() + "\\action");
			file.mkdirs();
			System.out.println(file.getAbsolutePath());
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(
						new FileWriter(file.getAbsolutePath() + "\\" + page.getClassName() + PRO_ZH_CN));
				for (String key : map.keySet()) {
					bw.write(key + "=" + writeComments(map.get(key)));
					bw.newLine();
				}
				bw.flush();
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private static String writeComments(String comments) throws IOException {
		// bw.write("#");
		String result = "";
		int len = comments.length();
		int current = 0;
		int last = 0;
		char[] uu = new char[6];
		uu[0] = '\\';
		uu[1] = 'u';
		while (current < len) {
			char c = comments.charAt(current);
			if (c > '\u00ff' || c == '\n' || c == '\r') {
				if (last != current)
					result += comments.substring(last, current);
				if (c > '\u00ff') {
					uu[2] = toHex((c >> 12) & 0xf);
					uu[3] = toHex((c >> 8) & 0xf);
					uu[4] = toHex((c >> 4) & 0xf);
					uu[5] = toHex(c & 0xf);
					result += new String(uu);
				} else {
					// bw.newLine();
					if (c == '\r' && current != len - 1 && comments.charAt(current + 1) == '\n') {
						current++;
					}
					if (current == len - 1
							|| (comments.charAt(current + 1) != '#' && comments.charAt(current + 1) != '!')) {
					}
				}
				last = current + 1;
			}
			current++;
		}
		if (last != current)
			comments.substring(last, current);
		return result;
	}

	/**
	 * Convert a nibble to a hex character
	 * 
	 * @param nibble
	 *            the nibble to convert.
	 */
	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	/** A table of hex digits */
	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	private static void waveFormInput(Map<String, String> Tag, StringBuilder tagStringb, String tagType) {
		for (String prokey : Tag.keySet()) {
			if (Tag.get(prokey) != null && !Arrays.toString(EXCLUDEPRO).contains(prokey)) {
				tagStringb.append(" " + prokey + "=\"" + Tag.get(prokey) + "\"");
			}
		}
		if ("select".equals(tagType)) {
			tagStringb.append(" prompt=\"\"");
		}
		tagStringb.append(" />\n");
	}

	private static void waveDatatable(PageInfo page, StringBuilder datatagsString, Map<String, String> Tag,
			StringBuilder DatatagString) {
		if ("Y".equals(Tag.get("isDatatable"))) {
			if ("uploadButton".equals(Tag.get("type"))) {
				DatatagString.append("  url = \"" + page.getPagePath() + "!downloadFj\"");
			}
			for (String prokey : Tag.keySet()) {
				if (StringUtils.isNotBlank(Tag.get(prokey)) && Arrays.toString(DATACOLUMNEPRO).contains(prokey)) {
					DatatagString.append(" " + prokey + "=\"" + Tag.get(prokey) + "\"");
				}
			}
			
			DatatagString.append(" />\n");
			datatagsString.append(DatatagString.toString());
		}
	};

}
class PageInfo {
	private String commonClassPath;
	private String title; // 标题
	private String category; // 类别
	private String pagePath;	// 页面路径
	private String className;	// 类名称
	private String entityName;	// 实体名称
	
	private List<Map<String, String>> tags;

	public String getPagePath() {
		return pagePath;
	}

	public void setPagePath(String pagePath) {
		this.pagePath = pagePath;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}



	public List<Map<String, String>> getTags() {
		return tags;
	}

	public void setTags(List<Map<String, String>> tags) {
		this.tags = tags;
	}


	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCommonClassPath() {
		return commonClassPath;
	}

	public void setCommonClassPath(String commonClassPath) {
		this.commonClassPath = commonClassPath;
	}
}


