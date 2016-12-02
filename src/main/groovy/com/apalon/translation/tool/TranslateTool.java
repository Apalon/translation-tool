/*******************************************************************************
 * Copyright 2016, Apalon Apps, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.apalon.translation.tool;

import com.apalon.translation.tool.utils.EscapingUtils;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Set;

class TranslateTool {

    public static final String STRINGS_FILE_NAME = "auto-translated-strings.xml";
    public static final String GSPREADSHEET_SERVICE_NAME = "APNSpreadsheet-Integration";
    public static final String GSPREADSHEET_VISIBILITY = "public";
    public static final String GSPREADSHEET_PROJECTION = "values";
    public static final String GSPREADSHEET_KEY_HEADER = "KEY";

    private DocumentBuilder builder;
    private File outResDir;
    private PrintStream out;
    private ImportConfig mConfig;

    public TranslateTool(PrintStream out) throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        builder = dbf.newDocumentBuilder();
        this.out = out == null ? System.out : out;
    }

    public static void run(String projectName, String path, String spreadsheetId) throws Exception {
        ImportConfig config = new ImportConfig();
        config.outputDirPath = path;
        String outputDirPath = config.outputDirPath;

        if (config.outputFileName == null)
            config.outputFileName = STRINGS_FILE_NAME;

        TranslateTool tool = new TranslateTool(null);
        List<ListEntry> listEntries = tool.getRemoteXLSData(projectName, spreadsheetId);
        if (listEntries == null || listEntries.size() <= 0)
            return;
        tool.mConfig = config;
        tool.outResDir = new File(outputDirPath);
        tool.outResDir.mkdirs();
        tool.parse(listEntries);
    }

    private static void addEmptyKeyValue(Document dom, Element root, String key) {
        root.appendChild(dom.createComment(String.format(" TODO: string name=\"%s\" ", key)));
    }

    private List<ListEntry> getRemoteXLSData(String project, String spreadsheetId) throws Exception {
        SpreadsheetService service =
                new SpreadsheetService(GSPREADSHEET_SERVICE_NAME);

        URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl(spreadsheetId, GSPREADSHEET_VISIBILITY, GSPREADSHEET_PROJECTION);
        WorksheetFeed feed = service.getFeed(url, WorksheetFeed.class);
        List<WorksheetEntry> worksheetList = feed.getEntries();
        WorksheetEntry worksheetEntry = null;
        for (WorksheetEntry we : worksheetList) {
            if (we.getTitle().getPlainText().equalsIgnoreCase(project)) {
                worksheetEntry = we;
                break;
            }
        }
        if (worksheetEntry == null)
            return null;

        ListFeed listFeed = service.getFeed(worksheetEntry.getListFeedUrl(), ListFeed.class);
        return listFeed.getEntries();
    }

    private void parse(List<ListEntry> entries) throws TransformerException, IOException, SAXException, ParserConfigurationException {
        Set<String> langs = entries.get(0).getCustomElements().getTags();
        String[] languageArray = new String[langs.size()];
        langs.toArray(languageArray);
        // Iterate over the remaining columns, and print each cell value
        for (int i = 1; i < languageArray.length; i++)
            generateLang(entries, languageArray[i]);
    }

    private void generateLang(List<ListEntry> entries, String lang) throws TransformerException, ParserConfigurationException, SAXException, IOException {
        Document dom = builder.newDocument();
        dom.appendChild(dom.createComment(" IMPORTANT: THIS FILE IS AUTO-GENERATED. DO NOT MODIFY THIS FILE "));
        Element root = dom.createElement("resources");
        dom.appendChild(root);

        for (ListEntry row : entries) {
            String key = row.getCustomElements().getValue(GSPREADSHEET_KEY_HEADER);
            if (key == null || "".equals(key)) {
                root.appendChild(dom.createTextNode(""));
                continue;
            }
            if (key.startsWith("/**")) {
                root.appendChild(dom.createComment(key.substring(3, key.length() - 3)));
                continue;
            }

            if (key.startsWith("//")) {
                root.appendChild(dom.createComment(key.substring(2)));
                continue;
            }
            //string
            String valueCell = row.getCustomElements().getValue(lang);
            if (valueCell == null) {
                //addEmptyKeyValue(dom, root, key);
                continue;
            }
            String value = valueCell.toString();// value

            if (value.isEmpty()) {
                addEmptyKeyValue(dom, root, key);
            } else if ( value.matches(".*\\<[^>]+>.*")) { //if value has html tags
                String xmlString = "<string name=\""+key+"\">"+ EscapingUtils.escapeWithBackslash(value)+"</string>";
                addContentAsHtml(dom, root, xmlString );
            } else {
                value = prepareOutputValue(value);
                addContent(dom, root, value, "string", key);
            }
        }

        save(dom, lang);
    }

    private void addContent(Document dom, Element root, String value, String nodeName, String key) throws IOException, SAXException, ParserConfigurationException {
        addContentAsString(dom, root, value, nodeName, key);
    }

    private void addContentAsHtml(Document dom, Element root, String xmlString) throws ParserConfigurationException, IOException, SAXException {
        Document tempDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmlString)));
        NodeList nodeList = tempDocument.getChildNodes();
        Node node = dom.importNode(nodeList.item(0), true);
        root.appendChild(node);
    }

    private void addContentAsString(Document dom, Element root, String value, String nodeName, String key) {
        Element node = dom.createElement(nodeName);
        if (key != null) {
            node.setAttribute("name", key);
        }
        node.setTextContent(value);
        root.appendChild(node);
    }

    private String prepareOutputValue(String value) {
        value = EscapingUtils.escapeWithBackslash(value);
        return value;
    }

    private void save(Document doc, String lang) throws TransformerException {
        File dir;
        if ("default".equals(lang) || lang == null || "".equals(lang)) {
            dir = new File(outResDir, "values");
        } else {
            dir = new File(outResDir, "values-" + lang);
        }
        dir.mkdir();

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(dir, mConfig.outputFileName));

        transformer.transform(source, result);
    }
}

