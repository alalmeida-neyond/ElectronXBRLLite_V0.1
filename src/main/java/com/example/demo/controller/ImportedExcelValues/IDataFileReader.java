package com.example.demo.controller.ImportedExcelValues;

import java.util.List;

public interface IDataFileReader {
	public int rowNum();

	public List<String[]> readRows(int batchSize) throws Exception;
}
