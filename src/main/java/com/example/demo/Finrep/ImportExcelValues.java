package com.example.demo.Finrep;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.example.demo.Exception.InvalidCellValueException;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;



public class ImportExcelValues {

    public static List<Integer> subList(List<Integer> lista, int i, int j){
        List<Integer> newList = new ArrayList<>();
        for(; i <= j; i++){
            newList.add(lista.get(i));
        }
        return newList;
    }

    // procura o maior grupo de numeros consecutivos, ou seja, o resultado obtido sera os indices da tabela filtrando assim ruido
    public static List<Integer> findLargestConsecutiveNumbers(List<Integer> numbers){
        if(numbers.isEmpty()){
            return Collections.emptyList();
        }
        
        int nMaxElements = 1;
        
        int startIndex = 0;
        int memStartIndex = startIndex;
        int endIndex = 0;
        int memEndIndex = endIndex;
        
        int memPreviousNumber = numbers.get(0);
        for(int number : numbers){
            
            // se a diferenca dos numeros for superior a 1
            if(Math.abs((number - memPreviousNumber)) > 1){
                int nElements = ((endIndex - 1) - startIndex) + 1;
                if(nElements > nMaxElements){
                    nMaxElements = nElements;
                    memStartIndex = startIndex;
                    memEndIndex = (endIndex - 1);
                }
                
                startIndex = endIndex;
            }
            
            memPreviousNumber = number;
            endIndex++;
        }
        
        //caso o maior numero de elementos esteja no fim da lista
        int nElements = ((endIndex - 1) - startIndex) + 1;
        if(nElements > nMaxElements){
            memStartIndex = startIndex;
            memEndIndex = (endIndex - 1);
        }
        
        return subList(numbers, memStartIndex, memEndIndex);
    }

    public static List<Integer> getAllRowsIndexs(XSSFSheet sheet){

        List<Integer> rows = new ArrayList<>();

        // obtem os indices das linhas com conteudo
        for(Row row : sheet){
            rows.add(row.getRowNum());
        }
        return findLargestConsecutiveNumbers(rows);
    }

    public static void initExcelPointsFUR(ExcelPoints excel){
        excel.setColumnLabelsRow(-1);
        excel.setFirstColumn(-1);
        excel.setFirstRow(-1);
        excel.setLastColumn(-1);
        excel.setLastRow(-1);
    }

    public static void setExcelPointsLabelsFUR(XSSFSheet sheet, ExcelPoints excel) throws Exception{
        int columnLabelsRowIndex = (excel.getColumnLabelsRow() - 1);
        int firstColumnIndex = (excel.getFirstColumn() - 1);
        int previousFirstColumnIndex = (firstColumnIndex - 1);
        int lastColumnIndex = (excel.getLastColumn()- 1);
        int firstRowIndex = (excel.getFirstRow() - 1);
        int lastRowIndex = (excel.getLastRow() - 1);
        try{
            excel.setFirstColumnLabel(getCellValue(sheet, columnLabelsRowIndex, firstColumnIndex)); // (x,y) -> (columnLabelsRow, firstColumn)
            excel.setLastColumnLabel(getCellValue(sheet, columnLabelsRowIndex, lastColumnIndex)); // (x,y) -> (columnLabelsRow, lastColumn)
            excel.setFirstRowLabel(getCellValue(sheet, firstRowIndex, previousFirstColumnIndex)); // (x,y) -> (firstRow, firstColumn - 1)
            excel.setLastRowLabel(getCellValue(sheet, lastRowIndex, previousFirstColumnIndex)); // (x,y) -> (lastRow, firstColumn - 1)
        } catch(InvalidCellValueException icve){
            System.out.println("Exception on ImportExcelValues.getExcelPointsFUR(): " + icve.toString());
            icve.printStackTrace(); 
            throw new Exception();
        }
    }

    // infelizmente nao existe esta funcao na versao 3.10 no jar, dai ter criado
    public static List<CellRangeAddress> getMergedRegions(Sheet sheet){
        List<CellRangeAddress> regionsList = new ArrayList<>();
        for(int i = 0; i < sheet.getNumMergedRegions(); i++) {
            regionsList.add(sheet.getMergedRegion(i));
        }
        return regionsList;
    }

    public static boolean isMergedCell(List<CellRangeAddress> mergedCells, int rowIndex, int columnIndex) {
        for (CellRangeAddress range : mergedCells) {
            if (range.isInRange(rowIndex, columnIndex)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMergedCell(List<CellRangeAddress> mergedCells, Cell cell) {
        for (CellRangeAddress range : mergedCells) {
            if (range.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                return true;
            }
        }
        return false;
    }

    // de uma determinada linha do excel procura o utlimo numero da coluna que nao e uma cell merged
    public static void setExcelPointsLastColumnFUR(ExcelPoints excel, Row row, List<CellRangeAddress> mergedCells){
        int columnNumber = -1;
        for(Cell cell : row){
            columnNumber = (cell.getColumnIndex() + 1);
            if(isMergedCell(mergedCells, cell)){
                columnNumber--; //previous column
                break;
            }
        }
        excel.setLastColumn(columnNumber);
    }

    public static void getExcelPointsFUR(XSSFSheet sheet, ExcelPoints excelPointStruct) throws Exception{

        initExcelPointsFUR(excelPointStruct);

        //int firstRowIndex = sheet.getFirstRowNum();
        //int lastRowIndex = sheet.getLastRowNum();

        //System.out.println("first row: " + (firstRowIndex + 1) + ", last row: " + (lastRowIndex + 1));

        // contem todas as celulas do excel que sao merged
        List<CellRangeAddress> mergedCells = getMergedRegions(sheet);

        // contem todos os indices das linhas da area que a tabela ocupa no excel
        List<Integer> rowsIndexs = getAllRowsIndexs(sheet);
        if(!rowsIndexs.isEmpty()){
            // para cada indice da linha do excel com conteudo
            for(int indexRow : rowsIndexs){
                Row row = sheet.getRow(indexRow);

                int firstIndexColumn = row.getFirstCellNum(); // indica o indice da primeira coluna do excel que tem conteudo
                //int lastIndexColumn = row.getLastCellNum() - 1; // indica o indice da ultima coluna do excel que tem conteudo

                int rowNumber = indexRow + 1; // indica o numero da linha do excel
                int firstColumn = firstIndexColumn + 1; // indica o numero da primeira coluna do excel que tem conteudo
                //int lastColumn = lastIndexColumn + 1; // indica o numero da ultima coluna do excel que tem conteudo

                // se nunca actualizou a info das colunas da tabela
                if(excelPointStruct.getFirstColumn() == -1){
                    excelPointStruct.setFirstColumn(firstColumn);
                    setExcelPointsLastColumnFUR(excelPointStruct, row, mergedCells);
                }
                // se nunca actualizou e existe uma diferenca negativa no numero da coluna do excel entao e porque se trata das linhas da tabela
                else if(excelPointStruct.getFirstRow() == -1 && firstColumn < excelPointStruct.getFirstColumn()){
                    excelPointStruct.setFirstRow(rowNumber);
                    excelPointStruct.setColumnLabelsRow(rowNumber - 1);
                }
                // se nunca actualizou e o numero da coluna do excel for diferente da linha anterior
                else if(excelPointStruct.getLastRow() == -1){
                    int previousFirstColumn = sheet.getRow(indexRow - 1).getFirstCellNum() + 1;
                    if(firstColumn != previousFirstColumn){
                        excelPointStruct.setLastRow(rowNumber - 1);
                    }
                }
            }

            // caso tenha chega ao final da tabela sem ter alterado o lastRow, entao guarda
            if(excelPointStruct.getLastRow() == -1){
                int lastRowIndex = rowsIndexs.get(rowsIndexs.size() - 1);
                excelPointStruct.setLastRow(lastRowIndex + 1);
            }

            setExcelPointsLabelsFUR(sheet, excelPointStruct);
        }
    }

    // Method that receives a woksheet and structure and fills the structure with the delimiter cells of the table
    public static void getExcelPoints(XSSFSheet workSheet, ExcelPoints excelPointStruct) throws Exception{
        try {
            XSSFRow row;
            String cellValue;

            // Get first and last columns
            outerloop1:
            for (int i = 2; i < 11; i++) {
                row = workSheet.getRow(i);
              
                if (row != null) {
                    for (int j = 2; j < 10; j++) {
                        cellValue = ImportExcelValues.getCellValue(workSheet, i, j);
                        if (cellValue != null) {
                            if (isInteger(cellValue) && cellValue.length() >= 3 && cellValue.length() <= 5) {
                                // Get first column
                                excelPointStruct.setFirstColumn(j + 1);
                                excelPointStruct.setFirstColumnLabel(cellValue);
                                excelPointStruct.setColumnLabelsRow(i + 1);

                                // Get last column
                                excelPointStruct.setLastColumn(row.getLastCellNum());
                                cellValue = ImportExcelValues.getCellValue(workSheet, i, row.getLastCellNum() - 1);
                                if (isInteger(cellValue) && cellValue.length() >= 3 && cellValue.length() <= 5) {
                                    excelPointStruct.setLastColumnLabel(cellValue);
                                    break outerloop1;
                                }
                            }

                            for (int k = excelPointStruct.getLastColumn() - 2; k >= excelPointStruct.getFirstColumn() - 1; k--) {
                                cellValue = ImportExcelValues.getCellValue(workSheet, i, k);
                                if (cellValue != null) {
                                    if (isInteger(cellValue) && cellValue.length() >= 3 && cellValue.length() <= 5) {
                                        excelPointStruct.setLastColumn(k + 1);
                                        excelPointStruct.setLastColumnLabel(cellValue);
                                        break outerloop1;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Search first row
            for (int i = excelPointStruct.getColumnLabelsRow(); i <= workSheet.getLastRowNum(); i++) {
                cellValue = ImportExcelValues.getCellValue(workSheet, i, excelPointStruct.getFirstColumn() - 2);
                if (cellValue != null) {
                    if (isInteger(cellValue) && cellValue.length() >= 3 && cellValue.length() <= 5) {
                        excelPointStruct.setFirstRow(i + 1);
                        excelPointStruct.setFirstRowLabel(cellValue);
                        break;
                    }
                }
            }

            // Get last row
            excelPointStruct.setLastRow(workSheet.getLastRowNum() + 1);
            cellValue = ImportExcelValues.getCellValue(workSheet, excelPointStruct.getLastRow() - 1, excelPointStruct.getLastRow() - 1);

            if (cellValue != null) {
                if (isInteger(cellValue) && cellValue.length() >= 3 && cellValue.length() <= 5) {
                    excelPointStruct.setLastRowLabel(cellValue);
                    return;
                }
            }
            
            for (int i = workSheet.getLastRowNum(); i > excelPointStruct.getColumnLabelsRow() - 1; i--) {
                cellValue = ImportExcelValues.getCellValue(workSheet, i, excelPointStruct.getFirstColumn() - 2);
                if (cellValue != null) {
                    if (isInteger(cellValue) && cellValue.length() >= 3 && cellValue.length() <= 5) {
                        excelPointStruct.setLastRow(i + 1);
                        excelPointStruct.setLastRowLabel(cellValue);
                        break;
                    }
                }
            }
            
            
        } catch (Exception ex) {
            String excp = "Exception on ImportExcelValues.getExcelPoints(): " + ex.toString();
            System.out.println(excp);   
            ex.printStackTrace(); 
            throw new Exception();
         }
    }

    // Function that receives a worksheet, row index i and column index j and returns the correspondant cell value
    //verificar impacto nas importaçoes 
    public static String getCellValue(XSSFSheet workSheet, int i, int j) throws InvalidCellValueException {

        String value = null;
        XSSFRow row = workSheet.getRow(i);
               
        if (row != null) {
            XSSFCell cell = row.getCell(j);
            if (cell != null) {
                value = getCellFormulaValue(cell, cell.getCellType().ordinal());
            }
        }
        return value;
    }

    private static String getCellFormulaValue(Cell cell, int type) throws InvalidCellValueException {

        //DATE
        if (type == CellType.NUMERIC.ordinal() && DateUtil.isCellDateFormatted(cell)) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            return sdf.format(cell.getDateCellValue());
        }
        else if(type == CellType.BOOLEAN.ordinal())
        {
            return cell.getBooleanCellValue() + "";
        }
        else if(type == CellType.NUMERIC.ordinal())
        {
            Double extractedDouble = cell.getNumericCellValue();
            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
            otherSymbols.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("#.################", otherSymbols); // número décimal e não notação científica (utilizam-se até 16 casas decimais, valor acima das 15 máximas permitidas pelo excel)
            return df.format(extractedDouble);
        }
        else if(type == CellType.STRING.ordinal())
        {
            String value = cell.getStringCellValue().trim();

            if ("".equals(value)) {
                return null;
            }

            return value;
        }
        else if(type == CellType.BLANK.ordinal())
        {
            return null;
        }
        else if(type == CellType.ERROR.ordinal())
        {
            FormulaError formulaError = FormulaError.forInt(cell.getErrorCellValue());
            CellReference cellReference = new CellReference(cell.getRow().getRowNum(), cell.getColumnIndex());
            throw new InvalidCellValueException(cell.getSheet().getSheetName(), cellReference.formatAsString(), formulaError.getString());
        }
        else if(type == CellType.FORMULA.ordinal())
        {
            return getCellFormulaValue(cell, cell.getCachedFormulaResultType().ordinal());
        }
        else
        {
            return null;
        }
    }

    public static int searchColumnLabelIndex(XSSFSheet workSheet, ExcelPoints excelPointStruct, String columnLabel) throws InvalidCellValueException {

        String valueSearch;
        int columnIndex = -1;

        for (columnIndex = excelPointStruct.getFirstColumn() - 1; columnIndex < excelPointStruct.getLastColumn(); columnIndex++) {
            valueSearch = getCellValue(workSheet, excelPointStruct.getColumnLabelsRow() - 1, columnIndex);
            if (valueSearch.equals(columnLabel)) {
                break;
            }
        }
        return columnIndex;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
}
