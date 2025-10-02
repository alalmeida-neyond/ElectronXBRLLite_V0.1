/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package com.example.demo.Resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import java.io.Reader;

import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.Validation.ValResult;

public final class Utils {

    private static Map<String, String> resourceCache = new HashMap<String, String>();

    public static String getResource(String path) {
        String query = resourceCache.get(path);
        if (query == null) {
            StringBuilder result = new StringBuilder();
            try (InputStream in = Utils.class.getClassLoader().getResourceAsStream(path)) {
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    result.append(line).append("\n");
                }
                in.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            query = result.toString();
            resourceCache.put(path, query);
        }
        return query;
    }

    public static String getClassName(Object object) {
        return object.getClass().getSuperclass().getSimpleName();
    }

    public static String pad2(int number) {
        return (number < 10 ? "0" : "") + number;
    }

    public static String pad4(Integer number) {
        if (number == null) {
            return "";
        }
        return String.format("%04d", number);
    }

    /**
     * Method to get the List of Months in a written form (Janeiro / Fevereiro /
     * etc)
     *
     * @return
     */
    public static List<Object[]> getMonths() {
        String[] monthsDescription = new DateFormatSymbols(Constants.LOCALPT).getMonths();
        List<Object[]> months = new ArrayList<>();
        for (int i = 0; i < monthsDescription.length; i++) {
            if (!monthsDescription[i].isEmpty()) {
                Object[] obj = new Object[]{monthsDescription[i], i + 1};
                months.add(obj);
            }
        }
        return months;
    }

    /**
     * Method to get the List of year, where the range is 2 year before the
     * current year till 2 year after the current year
     *
     * @return
     */
    public static List<Integer> getYears() {
        int currYear = Year.now().getValue();
        return IntStream.rangeClosed(currYear - 2, currYear + 2).mapToObj(Integer::valueOf).collect(Collectors.toList());
    }

    /**
     * Separator method use this when connect Paths to another Path or file as
     * the separator between the 2
     *
     * @return
     */
    public static String getSeparator() {
        String separator = "\\";
        if (!System.getProperty(Constants.OS).contains(Constants.WINDOWS)) {

            separator = "/";
        }

        return separator;
    }

//    /**
//     * method used to get the corresponding number of the month based on the
//     * long form of the month (Dezembro -> 12 Marco -> 03) Add a 0 when the moth
//     * is inferior to 10
//     *
//     * @param MonthName name of the month as Long form (Janeiro)
//     * @return
//     */
//    public static String getMonthNumber(String MonthName) {
//        int monthNumber = getMonths().indexOf(MonthName) + 1;
//        return pad2(monthNumber);
//    }
    public static String getNullIfEmpty(String string) {
        if (string != null && string.equals("")) {
            return null;
        }
        return string;
    }

    public static String getStringOrOperatorPercentage(String str) {
        if (str == null || str.isEmpty()) {
            return "%";
        }
        return str;
    }

    public static String getStringWithOrOperatorPercentage(String str) {
        if (str == null || str.isEmpty()) {
            return "%";
        }
        return "%" + str;
    }

    /**
     * method used to check if regex is applicable to a certain string
     *
     * @param string string string with the value to apply
     * @param regex string with the regex to apply
     * @return true if is a number, otherwise false
     */
    public static boolean evaluateRegex(String string, String regex) {
        return string.matches(regex);
    }

    /**
     * method used to check if string is a number (uses . as separator)
     *
     * @param str string with the possible number
     * @return true if is a number, otherwise false
     */
    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    /**
     * method used to check if string is a number (uses , as separator)
     *
     * @param str string with the possible number
     * @return true if is a number, otherwise false
     */
    public static boolean isNumericWithComma(String str) {
        return str.matches("-?\\d+(\\,\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static String buildDateFromYearMonth(int year, int month, String format) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return lastDayOfMonth.format(formatter);
    }

    public static String convertTimeStampToDate(Timestamp timestamp) {
        LocalDateTime localDateTime = timestamp.toLocalDateTime();
        DateTimeFormatter formatter = Constants.dateFormat.withLocale(Constants.LOCALPT);
        String formattedDate = localDateTime.format(formatter).toUpperCase();
        return formattedDate;
    }

    public static String buildReferenceDateWithYearAndMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
        DateTimeFormatter formatter = Constants.dateFormat;
        return lastDayOfMonth.format(formatter);
    }

    public static String addACharBetweenTheReferenceDate(String referenceDate, String separator) {
        return referenceDate.substring(0, 4) + separator + referenceDate.substring(4, 6) + separator + referenceDate.substring(6, 8);
    }

    public static String applyVersionString(ModuleVersion module) {
        return module.getCode().split("_")[0] + applyVersionCode(module.getVersionNumber().trim());
    }

    public static String applyVersionCode(String versionCode) {
        String[] splittedString = versionCode.split("\\.");
        String Aux = "";
        for (String num : splittedString) {
            if (num.length() == 1) {
                Aux += "0" + num;
            } else {
                Aux += num;
            }
        }
        return Aux.trim();
    }

    public static String getEmptyMessage() {
        return Constants.emptyMessage;
    }

    public static LocalDate dateToLocalDate(Date date) {
        return date == null ? LocalDate.MAX : date.toInstant().atZone(Constants.LISBON).toLocalDate();
    }

	
//    public static void addLogOfOperations(Integer operationid, Integer operationnodeid, String result, String margins, String key, String type){
//        List<LogOperationTemp> logsList = Info.getInstance().getLogOperationsTempList();
//        logsList.add(new LogOperationTemp(operationid, operationnodeid, result, margins, key, type, Timestamp.valueOf(LocalDateTime.now())));
//    }
    

    public static String findOneByRegex(String regex, String scopeParsed) {
        return findOneByRegex(regex, scopeParsed, 0);
    }

    public static String findOneByRegex(String regex, String scopeParsed, int group) {
        List<String> lst = findByRegex(regex, scopeParsed, group, 1);
        if (lst != null && !lst.isEmpty()) {
            return lst.get(0);
        }
        return "";
    }

    public static List<String> findByRegex(String regex, String scopeParsed, int group) {
        return findByRegex(regex, scopeParsed, group, -1);
    }

    public static List<String> findByRegex(String regex, String scopeParsed, int group, int limit) {
        List<String> list = new ArrayList<>();
        Pattern formulaValuesPatern = Pattern.compile(regex);
        Matcher matchPattern = formulaValuesPatern.matcher(scopeParsed);

        while ((limit == -1 || limit-- > 0) && matchPattern.find()) {
            if (group <= matchPattern.groupCount()) {
                list.add(matchPattern.group(group));
            }
        }
        return list;
    }

    public static List<String> findAllGroupsByRegex(String regex, String scopeParsed, int minGroup) {
        return findAllGroupsByRegex(regex, scopeParsed, minGroup, -1);
    }

    public static List<String> findAllGroupsByRegex(String regex, String scopeParsed, int minGroup, int maxGroup) {
        List<String> list = new ArrayList<>();
        Pattern formulaValuesPatern = Pattern.compile(regex);
        Matcher matchPattern = formulaValuesPatern.matcher(scopeParsed);

        if (matchPattern.find()) {
            for (int i = minGroup; (maxGroup == -1 && i <= matchPattern.groupCount()) || i <= maxGroup; i++) {
                list.add(matchPattern.group(i));
            }
        }

        return list;
    }

    public static boolean dateIsValid(String year, String month, String day) {
        try {
            NumberFormat nf = new DecimalFormat("00");
            LocalDate.parse(year + "-" + nf.format(Long.valueOf(month)) + "-" + nf.format(Long.valueOf(day)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static LocalDate stringToDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, Constants.dateFormat);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDate stringToDate(String dateStr, DateTimeFormatter format) {
        try {
            return LocalDate.parse(dateStr, format);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMonthNumber(String month) {
        return Month.valueOf(month).toString();
    }

    public static String getCurrentTimeStampAsString(DateTimeFormatter format) {
        return LocalDateTime.now().format(format);
    }

    public static boolean validateFromARegex(String value, String regex) {
        return Pattern.compile(regex).matcher(value).matches();
    }

    public static String dateTreatment(String value) {
        String returnString = null;
        for (DateTimeFormatter formatter : Constants.DATEFORMATTERARRAY) {
            try {
                return LocalDate.parse(value, formatter).format(Constants.DATEFORMATISO8601);
            } catch (DateTimeParseException ignored) {
                // Try the next format
            }
        }

        return returnString;
    }

    public static String dateTimeTreatment(String value) {
        String returnString = null;
        for (DateTimeFormatter formatter : Constants.DATETIMEFORMATTERARRAY) {
            try {
                return LocalDate.parse(value, formatter).format(Constants.DATETIMEFORMATISO8601);
            } catch (DateTimeParseException ignored) {
                // Try the next format
            }
        }
        return returnString;
    }

    public static String roundToZero(String value) throws Exception {
        double num = Double.parseDouble(value);
        return Math.abs(num) < 1e-6 ? "0" : value;
    }

    public static Map<Integer, Object[]> createHashMapForExcel(List<Object[]> data, Object[] header) {
        int i = 0;
        Map<Integer, Object[]> hashmap = new TreeMap<Integer, Object[]>();
        hashmap.put(i, header);

        for (Object[] o : data) {
            i++;
            hashmap.put(i, o);
        }
        return hashmap;
    }

    public static SXSSFWorkbook createSpreadSheet(String sheetname, Map<Integer, Object[]> data, SXSSFWorkbook workbook) {

        Sheet spreadsheet = workbook.createSheet(sheetname);

        Row xssfRow;

        Set<Integer> keyid = data.keySet();

        int rowid = 0;

        // writing the data into the sheets...
        for (Integer key : keyid) {

            xssfRow = spreadsheet.createRow(rowid++);
            Object[] objectArr = data.get(key);
            int cellid = 0;

            for (Object obj : objectArr) {
                String objString = "";
                if (obj instanceof BigDecimal) {
                    BigDecimal objBigDecimal = (BigDecimal) obj;
                    objString = objBigDecimal.toString();
                } else if (obj == null) {
                    objString = "null";
                } else if (obj instanceof Clob) {
                    StringBuilder sb = new StringBuilder();
                    try (Reader reader = ((Clob) obj).getCharacterStream();
                        BufferedReader br = new BufferedReader(reader)) {
                        
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append(System.lineSeparator());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to convert CLOB to String", e);
                    }
                    objString = sb.toString().trim();
                } else {
                    objString = obj.toString();
                }

                Cell cell = xssfRow.createCell(cellid++);
                cell.setCellValue(objString);
            }
        }
        return workbook;
    }

    public static String applyParametersToQuery(String queryStr, Object... parameters) {
        String stringAux = queryStr;
        if (parameters != null && (parameters.length & 1) == 0) // número par de parâmetros
        {
            for (int i = 0; i < parameters.length; i = i + 2) {
                if (parameters[i + 1] != null) {
                    try {
                        stringAux = stringAux.replace("?" + parameters[i], "\'"+parameters[i + 1] + "\'");
                    } catch (Exception e) {

                    }
                }

            }
        }
        return stringAux;
    }
    
    public static String getFinalPathOfOS(String mainPath, String windowsDisk){
        if (System.getProperty(Constants.OS).contains(Constants.WINDOWS)) {
            return windowsDisk+mainPath;
        }
        return mainPath;
    }

    public static boolean valuesAreAllNull(List<ValResult> values) {
        for(ValResult value : values){
            if(value != null && !value.valueIsNull()){
                return false;
            }
        }
        return true;
    }
}
