package com.example.demo.Finrep;

public class ExcelPoints {

    public int getFirstColumn() {
        return firstColumn;
    }

    public void setFirstColumn(int firstColumn) {
        this.firstColumn = firstColumn;
    }

    public int getLastColumn() {
        return lastColumn;
    }

    public void setLastColumn(int lastColumn) {
        this.lastColumn = lastColumn;
    }

    public String getFirstColumnLabel() {
        return firstColumnLabel;
    }

    public void setFirstColumnLabel(String firstColumnLabel) {
        this.firstColumnLabel = firstColumnLabel;
    }

    public String getLastColumnLabel() {
        return lastColumnLabel;
    }

    public void setLastColumnLabel(String lastColumnLabel) {
        this.lastColumnLabel = lastColumnLabel;
    }

    public int getColumnLabelsRow() {
        return columnLabelsRow;
    }

    public void setColumnLabelsRow(int columnLabelsRow) {
        this.columnLabelsRow = columnLabelsRow;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public int getLastRow() {
        return lastRow;
    }

    public void setLastRow(int lastRow) {
        this.lastRow = lastRow;
    }

    public String getFirstRowLabel() {
        return firstRowLabel;
    }

    public void setFirstRowLabel(String firstRowLabel) {
        this.firstRowLabel = firstRowLabel;
    }

    public String getLastRowLabel() {
        return lastRowLabel;
    }

    public void setLastRowLabel(String lastRowLabel) {
        this.lastRowLabel = lastRowLabel;
    }

    int firstColumn; // numero da primeira coluna
    int lastColumn; // numero da ultima coluna
    int firstRow; // numero da primeira linha
    int lastRow; // numero da ultima linha
    String firstColumnLabel; // conteudo do identificador da primeira coluna
    String lastColumnLabel; // conteudo do identificador da ultima coluna
    String firstRowLabel; // conteudo do identificador da primeira linha
    String lastRowLabel; // conteudo do identificador da ultima linha
    int columnLabelsRow; // numero da linha onde estao os identificadores das colunas
}
