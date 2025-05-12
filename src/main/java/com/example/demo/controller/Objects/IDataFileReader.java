package com.example.demo.controller.Objects;

import java.util.List;

public interface IDataFileReader {
	public int rowNum();

	public List<String[]> readRows(int batchSize) throws Exception;
}
