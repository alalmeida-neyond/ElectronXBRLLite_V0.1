package com.example.demo.controller.Objects;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;


public class XExcelFileReader implements IDataFileReader {
	private int rowNum = 0;
	private OPCPackage opcPkg;
	private ReadOnlySharedStringsTable stringsTable;
	private XMLStreamReader xmlReader;
        private String sheetName;

	public XExcelFileReader(String excelPath) throws Exception {
		opcPkg = OPCPackage.open(excelPath, PackageAccess.READ);
		this.stringsTable = new ReadOnlySharedStringsTable(opcPkg);

		XSSFReader xssfReader = new XSSFReader(opcPkg);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		InputStream inputStream = xssfReader.getSheetsData().next();
                
		XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
                while (iter.hasNext()) {
                    iter.next(); 
                    sheetName = iter.getSheetName();
                }
                
                xmlReader = factory.createXMLStreamReader(inputStream);
                while (xmlReader.hasNext()) {
                    xmlReader.next();
                    if (xmlReader.isStartElement()) {
                        if (xmlReader.getLocalName().equals("sheetData"))
                            break;
                    }
                }
	}
    
	@Override
	public int rowNum() {
		return rowNum;
	}

	@Override
	public List<String[]> readRows(int batchSize) throws XMLStreamException {
		String elementName = "row";
		List<String[]> dataRows = new ArrayList<>();
                    while (xmlReader.hasNext()) {
                            xmlReader.next();
                            if (xmlReader.isStartElement()) {
                                    if (xmlReader.getLocalName().equals(elementName)) {
                                            rowNum++;
                                            dataRows.add(getDataRow());
						if (dataRows.size() == batchSize)
							break;
                                    }
                            }
                    }

		return dataRows;
	}

	private String[] getDataRow() throws XMLStreamException {
		List<String> rowValues = new ArrayList<>();
		while (xmlReader.hasNext()) {
			xmlReader.next();
			if (xmlReader.isStartElement()) {
				if (xmlReader.getLocalName().equals("c")) {
					CellReference cellReference = new CellReference(xmlReader.getAttributeValue(null, "r"));
					// Fill in the possible blank cells!
					while (rowValues.size() < cellReference.getCol()) {
						rowValues.add("");
					}
					String cellType = xmlReader.getAttributeValue(null, "t");
					rowValues.add(getCellValue(cellType));
				}
			} else if (xmlReader.isEndElement() && xmlReader.getLocalName().equals("row")) {
				break;
			}
		}
		return rowValues.toArray(new String[rowValues.size()]);
	}

	private String getCellValue(String cellType) throws XMLStreamException {
		String value = ""; // by default
		while (xmlReader.hasNext()) {
			xmlReader.next();
			if (xmlReader.isStartElement()) {
				if (xmlReader.getLocalName().equals("v")) {
					if (cellType != null && cellType.equals("s")) {
						int idx = Integer.parseInt(xmlReader.getElementText());
						return new XSSFRichTextString(stringsTable.getItemAt(idx).getString()).toString();
					} else {
						return xmlReader.getElementText();
					}
				}
			} else if (xmlReader.isEndElement() && xmlReader.getLocalName().equals("c")) {
				break;
			}
		}
		return value;
	}

	@Override
    public void finalize() throws IOException {
        if (opcPkg != null) {
            opcPkg.close();
        }
    }

    /**
     * @return the sheetName
     */
    public String getSheetName() {
        return sheetName;
    }

    /**
     * @param sheetName the sheetName to set
     */
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }
        public XMLStreamReader getXmlReader() {
        return xmlReader;
    }

    public void setXmlReader(XMLStreamReader xmlReader) {
        this.xmlReader = xmlReader;
    }

}
