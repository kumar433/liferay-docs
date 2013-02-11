
package com.liferay.portal.tools.propertiesconverter;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertiesParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Configuration cfg = new Configuration();
		try {
			cfg.setDirectoryForTemplateLoading(new File(
				System.getProperty("user.dir") + "/code/properties-converter/src/com/liferay/portal/tools/propertiesconverter/dependencies"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		
		// Create a data model for Freemarker
		Map root = new HashMap();
		
		String pageTitle = "Portal Properties";
		if (!args[0].isEmpty()) {
			pageTitle = args[0];
		}
		root.put("pageTitle", pageTitle);
		
		boolean toc = true;
		if (!args[1].isEmpty()) {
			toc = Boolean.parseBoolean(args[1]);
		}
		root.put("toc", toc);
		
		// Parse properties file and create sections for the data model
		File propertiesFile = new File(System.getProperty("user.dir") + "/code/properties-converter/" + args[2]);
		String propertiesString = read(propertiesFile);
		String[] paragraphs = propertiesString.split("\n\n");
		ArrayList<Section> sections = new ArrayList<Section>();
		for (int i = 0; i < paragraphs.length; i++) {
			if (paragraphs[i].startsWith("##")) {
				Section section = new Section(true, paragraphs[i].replace("#", "").trim(), paragraphs[i], new ArrayList<String>(), new ArrayList<String>());
				sections.add(section);
			}
			else {
				ArrayList<String> properties = new ArrayList<String>();
				String[] lines = paragraphs[i].split("\n");
				for (int j = 0; j < lines.length; j++) {
					if (lines[j].trim().contains("=") &&
						!lines[j].trim().startsWith("# ")) {
						int equalsNdx = lines[j].indexOf("=");
						String property = lines[j].substring(0, equalsNdx);
						if (!properties.contains(property)) {
							properties.add(property);
						}
					}
				}
				Section section = new Section(false, "", paragraphs[i], properties, new ArrayList<String>());
				sections.add(section);
			}
		}
		
		// Populate sectionProperties field with correct properties (none for section titles)
		for (int i = 0; i < sections.size(); i++) {
			if (sections.get(i).isSectionTitle) {
				for (int j = i + 1; j < sections.size(); j++) {
					if (!sections.get(j).getProperties().isEmpty()) {
						for (int k = 0; k < sections.get(j).getProperties().size(); k++) {
							sections.get(i).getSectionProperties().add(sections.get(j).getProperties().get(k));
						}
					}
					else {
						break;
					}
				}
			}
		}
		root.put("sections", sections);

		// Get the Freemarker template and merge it with the data model
		try {
			Template temp = cfg.getTemplate("properties.ftl");
			File propertiesHtml = new File (System.getProperty("user.dir") + "/code/properties-converter/properties.html");
			Writer out = new FileWriter(propertiesHtml);
			try {
				temp.process(root, out);
			}
			catch (TemplateException e) {
				e.printStackTrace();
			}
			out.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static String read(File file) {

		StringBuilder contents = new StringBuilder();
		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return contents.toString();
	}

}
