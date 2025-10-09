package com.example.demo.Resources;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.util.Pair;

public class Constants {

    //TimeFormatters
    public static final String DATEFORMATISO8601STRING = "yyyy-MM-dd";
    public static final String ISOBASEFORMAT = "YYYYMMDD";
    public static final String ISOBASEFORMATSQlite = "%Y%m%d";

    public static final DateTimeFormatter DATEFORMATISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter dateFormatFileName = DateTimeFormatter.ofPattern("uuuuMMdd");
    public static final DateTimeFormatter DATEFORMATUSEDBYVALIDATIONS = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIMEDATEFORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    public static final DateTimeFormatter dateFormatScreen = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter generationDateFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    public static final DateTimeFormatter parametizationDateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    public static final DateTimeFormatter DATETIMEFORMATISO8601 = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static String ISOBASEFORMAT8601 = "yyyy-MM-dd";
    public static String ISOBASEFORMAT8601SQLite = "%Y-%m-%d";
    public static String DATETIMEFORMATSQLite = "%Y-%m-%d %H:%M:%S";
    public static final DateTimeFormatter[] DATEFORMATTERARRAY = new DateTimeFormatter[]{
        dateFormat,
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("yyyy.MM.dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy")
    };
    
    public static final DateTimeFormatter[] DATETIMEFORMATTERARRAY = new DateTimeFormatter[]{
        DATETIMEFORMATISO8601,
        dateFormatScreen,
        DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy.MM.dd hh:mm:ss"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss")
    };
    
    //Region
    public static final Locale LOCALPT = Locale.of("pt", "PT");
    public static final ZoneId LISBON = ZoneId.of("Europe/Lisbon");

    public static final String VersionsAll = "VersionsAll";
    public static final String DomainsAll = "DomainsAll";
    public static final String EntitiesAll = "ConfEntitiesAll";
    public static final String PERIODICITYAll = "ConfPeriodicityAll";
    public static final String AppConfigsAll = "AppConfigsAll";
    public static final String OperatorAll = "OperatorAll";
    public static final String OperatorArgumentAll = "OperatorArgumentAll";
    public static final String DATATYPEALL = "DataTypeAll";
    public static final String ModuleVersionAll = "ModuleVersionAll";
    public static final String IOStateAll = "IOStateAll";
    public static final String ConfActionAll = "ConfActionAll";
    public static final String SEVERITYWARNING = "warning";
    public static final String SEVERITYERROR = "Error";
    public static final String ConfImportRulesAll = "ConfImportRulesAll";

    //File Extensions
    public static final String XLSX = "XLSX";
    public static final String CSV = "CSV";

    public static final String admin = "admin";
    public static final String Admin = "Admin";

    public static final String WINDOWSDISK = "WINDOWSDISK";
    public static final String IMPORTFILESPATH = "IMPORTFILESPATH";
    public static final String MandatoryFILESPATH = "MANDATORYFILESPATH";
    public static final String XBRLGENERATEDPATH = "XBRLGENERATEDPATH";
    public static final String JSONFILEFORXBRLPATH = "JSONFILEFORXBRLPATH";
    public static final String TEMPLATEFILESPATH = "TEMPLATEFILESPATH";

    //Operadores
    public static final int UNARYPLUS = 1;
    public static final int ADDITION = 2;
    public static final int DIVISION = 3;
    public static final int UNARYMINUS = 4;
    public static final int SUBSTRACTION = 5;
    public static final int ABSOLUTEVALUE = 6;
    public static final int NUMERICMINIMUM = 7;
    public static final int MULTIPLICATION = 8;
    public static final int NUMERICMAXIMUM = 9;
    public static final int SQUAREROOT = 10;
    public static final int AGGREGATEMAXIMUM = 11;
    public static final int AGGREGATEMINIMUM = 12;
    public static final int EQUALSTO = 13;
    public static final int LESSTHANEQUALTO = 14;
    public static final int GREATERTHANEQUALTO = 15;
    public static final int ELEMENTOF = 16;
    public static final int ISNULL = 17;
    public static final int GREATERTHAN = 18;
    public static final int LESSTHAN = 19;
    public static final int NOTEQUALTO = 20;
    public static final int MATCHCARACTERS = 21;
    public static final int AND = 22;
    public static final int OR = 23;
    public static final int NOT = 24;
    public static final int EXCLUSIVEOR = 25;
    public static final int SUM = 26;
    public static final int COUNT = 27;
    public static final int WHERE = 28;
    public static final int GET = 29;
    public static final int IFTHENELSE = 30;
    public static final int FILTER = 31;
    public static final int TIMESHIFT = 32;
    public static final int RENAME = 33;
    public static final int RENAMENODE = 34;
    public static final int GROUPINGCLAUSE = 35;
    public static final int PERSISTENTASSIGNMENT = 36;
    public static final int PARENTHESISEXPRESSION = 37;

    public static final String XBRL = "XBRL";
    public static final String XML = "XML";
    public static final String lock = "Lock";
    public static final String LOCK = "LOCK";
    public static final Integer lockInt = 0;
    public static final String unlock = "Unlock";
    public static final String UNLOCK = "UNLOCK";
    public static final Integer unlockInt = 1;
    public static final String importLow = "Import";
    public static final String IMPORT = "IMPORT";
    public static final String GENERATE = "GENERATE";
    public static final String VIEW = "VIEW";
    public static final String VALIDATE = "VALIDATE";
    public static final String GENERATION = "GENERATION";
    public static final String GEN = "GEN";
    public static final int VIEWID = 1;
    public static final int IMPORTID = 2;
    public static final int VALIDATEID = 3;
    public static final int GENERATEID = 4;
    public static final int LOCKID = 5;
    public static final int UNLOCKID = 6;

    public static final String emptyMessage = "Sem resultados encontrados";
    public static final String processoFalhou = "Processo falhou! Contactar administrator ou tentar novamente";
    public static final String erroEliminar = "Ocorreu um erro ao tentar eliminar";
    public static final String DATAINCOMPLETASTRING = "Data incompleta, foi ignorada na pesquisa";
    public static final String NOPERMISSIONS = "Sem permissões";
    public static final String INCORRECTFILE = "Ficheiro incorreto";
    public static final String DBERROR = "Erro na Base de dados";
    public static final String CONTACTADMIN = "Contactar Administrador";
    public static final String deleteMapError = "Erro no processo de deletes de mapas.";
    public static final String emptyMapsToDeleteEmpty = "Lista de mapas para apagar encontra-se vazia.";
    public static final String generationError = "Erro no processo de Geracao";
    public static final String missingFilters = "Necessário fornecer valor para todos os filtros";
    public static final String concurrentOperations = "Operacoes concorrentes";
    public static final String concurrentOperationsDesc = "Existem operacoes relacionadas com o módulo escolhido em progresso. Tente mais tarde, contacte um administrador ou cancele a operacao a ser realizada";
    public static final String generationStarted = "Geracao Iniciada";
    public static final String generationStartedDesc = "Geracao iniciada com sucesso";
    public static final String generationConfigError = "Erro no processo de Geração, não foi possivel obter o ponto de entrada para a Taxonomia! Contacte o admistrador";
    public static final String generationEndedDesc = "Geração Concluída com sucesso";
    public static final String missingData = "Não existe dados para os filtros selecionados";
    public static final String dataInvalida = "Data inválida.";
    public static final String moduloInvalido = "Módulo inválido.";
    public static final String entidadeInvalida = "Entidade inválida.";
    public static final String dominioInvalido = "Dominio inválido.";
    public static final String extensaoInvalida = "Ficheiro com extensão inválida.";
    public static final String noLockPermissions = "Não tem permissões de lock para a geracao selecionada";
    public static final String noUnlockPermissions = "Não tem permissões de unlock para a geracao selecionada";
    public static final String downloadError = "Erro no download do ficheiro pretendido";
    public static final String IMPORTSMASHLOGMESSAGE = "Os dados desta tabela foram atualizados devido a uma nova importacao.";
    public static final String missingRefDate = "Para iniciar a pesquisa precisará fornecer valores para o ano e mês";
    public static final String NOPRIVILEGES = "Sem privilégios";
    public static final String NOPRIVILEGESDETAILS = "Sem privilégios para ver os detalhes de importacao existentes";
    public static final String validationError = "Erro no processo de Validacao.";
    public static final String NOMAPSSELECTED = "Deve selecionar pelo menos 1 mapa antes de iniciar a validação.";
    public static final String IMPOSSIBLEVALUE = "Foram descartados valores inválidos";
    public static final String IMPOSSIBLECELLS = "Foram descartados células inválidas";
    public static final String IMPOSSIBLEROWKEYS = "Foram descartados chaves inválidas";
    public static final String IMPOSSIBLEMAP = "Foram descartados mapas inválidos";
    public static final String IMPOSSIBLEUPLOAD = "Importação falhou.";
    public static final String NOSELECTEDFILE = "Não foi selecionado ficheiro para importação.";
    public static final String ERROR = "Erro: ";
    public static final String FILEREADY = "Ficheiro pronto";
    public static final String FILEREADYDESC = "Ficheiro encontra-se pronto para ser importado";
    public static final String UPLOADFAILED = "Upload de Ficheiro falhou";
    public static final String UPLOADFAILEDDESC = "Falha na importação do ficheiro para a diretoria de importação!";
    public static final String FILENOTONSERVER = "Ficheiro não se encontra no servidor.";
    public static final String MESSAGEINVALIDDESAGCODE = "Ocorreu um erro no Desagregation Code.";
    public static final String MESSAGEINVALIDROWKEY = "Ocorreu um erro na RowKey.";

    public static final String MESSAGEERRORDESAGCODE(String mapcode){
        return "Alerta! Inseriu um valor inválido para o DesagregationCode, no mapa "+mapcode+".";
    }

    public static final String MESSAGEERRORINCOMPLETEDESAGCODE(String mapcode){
        return "Alerta! Inseriu valores insuficientess para o DesagregationCode, no mapa "+mapcode+".";
    }

    public static final String MESSAGEERRORUNKOWNDATATYPEDESAGCODE(String mapcode){
        return "Alerta!  Tipo de dados desconhecido para o DesagregationCode, no mapa "+mapcode+". "+CONTACTADMIN+".";
    }

    public static final String MESSAGEERRORUNEXPECTEDDESAGCODE(String mapcode){
        return "Alerta! Não é esperado um DesagregationCode, no mapa "+mapcode+".";
    }
    
	public static final int FALSEASNUMBER = 0;
    public static final int TRUEASNUMBER = 1;
    
    public static final int INVALIDNUMBEROFROWS = 0;
    //AppConfigs
    public static final String TOLERANCE = "TOLERANCE";
    public static final String PRECISIONOFDIVISION = "PRECISIONOFDIVISION";
    public static final String MONETARYPRECISION = "MONETARY";
    public static final String INTEGERPRECISION = "INTEGER";
    public static final String PERCENTAGEPRECISION = "PERCENTAGE";
    public static final String DECIMALPRECISION = "DECIMAL";

    //DataTypes
    public static final int DATATYPENOTAPPLICABLE = 0;
    public static final int DATATYPEINTEGER = 1;
    public static final int DATATYPEDECIMAL = 2;
    public static final int DATATYPESTRINGNONEMPTY = 3;
    public static final int DATATYPEBOOLEAN = 4;
    public static final int DATATYPETRUE = 5;
    public static final int DATATYPEDATETIME = 6;
    public static final int DATATYPEDATE = 7;
    public static final int DATATYPEENUMERATION = 8;
    public static final int DATATYPEMONETARY = 9;
    public static final int DATATYPEPERCENTAGE = 10;
    public static final int DATATYPEURI = 11;
    public static final int DATATYPEORDINALS = 12;
    public static final int DATATYPESTRINGINCLUDINGEMPTY = 13;

    //DataTypes
    public static final int NOTAPPLICABLE = 0;
    public static final int INTEGER = 1;
    public static final int DECIMAL = 2;
    public static final int STRINGNONEMPTY = 3;
    public static final int BOOLEAN = 4;
    public static final int TRUE = 5;
    public static final int DATETIME = 6;
    public static final int DATE = 7;
    public static final int ENUMERATION = 8;
    public static final int MONETARY = 9;
    public static final int PERCENTAGE = 10;
    public static final int URI = 11;
    public static final int ORDINALS = 12;
    public static final int STRINGINCLUDINGEMPTY = 13;

    //Subgrupos Operacoes
    public static final int ARITHMETICSUBGROUP = 1;
    public static final int COMPARISONSUBGROUP = 2;
    public static final int LOGICALSUBGROUP = 3;
    public static final int INDIVIDUALNUMERICSUBGROUP = 4;
    public static final int INDIVIDUALBOOLEANSUBGROUP = 5;

    public static final String ARGUMENTLEFT = "left";
    public static final String ARGUMENTRIGHT = "right";
    public static final String ARGUMENTOPERAND = "operand";
    public static final String ARGUMENTGROUPINGCLAUSE = "grouping_clause";
    public static final String ARGUMENTCONDITION = "condition";
    public static final String ARGUMENTSET = "set";
    public static final String ARGUMENTTHEN = "then";
    public static final String ARGUMENTELSE = "else";
    public static final String ARGUMENTSELECTION = "selection";
    public static final String ARGUMENTPATTERN = "pattern";
    public static final String ARGUMENTCOMPONENT = "component";
    public static final String ARGUMENTPERIOD = "period_indicator";
    public static final String ARGUMENTNUMBER = "shift_number";
    public static final String ARGUMENTDIMENSION = "dimension";
    

    //Propertys
    public static final String PROPERTYROW = "r";
    public static final String PROPERTYCOLUMN = "c";
    public static final String PROPERTYSHEET = "s";
    public static final String VALUE = "Value";
    public static final String PROPERTY = "Property";
    public static final String ITEM = "Item";
    public static final String REFPERIOD = "refPeriod";
    
    //Time Period
    public static final String PERIODYEAR = "A";
    public static final String PERIODSEMESTER = "S";
    public static final String PERIODQUARTER = "Q";
    public static final String PERIODMONTH = "M";
    public static final String PERIODWEEK = "W";
    public static final String PERIODDAY = "D";
    
    public static final int DOMAINLENGTH = 3;

    public static String INCORRECTFILEDESC = "O ficheiro não cumpre os requisitos";

    public static final int FIRSTRESULT = 0;
    public static final String TRUERESULT = "true";
    public static final String FALSERESULT = "false";
    
    //Operating System - Used in differenes for each operating System
    public static final String OS = "os.name";
    public static final String WINDOWS = "Windows";
    public static final String LINUX = "Linux";
    public static final String MAC = "Mac";

    //Import Status
    public static final String UNDIFINED = "Indefinido";
    public static final String LEGACYIMPORT = "legacy";
    public static final String IMPORTPATHKEY = "IMPORTFILESPATH";

    //Domain
    //Short
    public static final String CON = "CON";
    public static final String IND = "IND";
    //Long
    public static final String Consolidado = "Consolidado";
    public static final String Individual = "Individual";

    //REGEX
    //DATATYPES
    public static final String URIPATTERN = "^(https?|ftp)://[\\w.-]+(?:\\.[\\w.-]+)+[/#?]?.*$";
    public static final String BOOLEANPATTERN ="(?i)^(true|false)$";
    public static final String TRUEPATTERN ="(?i)^(true)$";
    public static final String DATEPATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
    public static final String DATETIMEPATTERN = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?(Z|[+-]\\d{2}:\\d{2})$";
    
    //DATAS
    public static final String YEARMONTH = "[0-9]{6}";
    public static final String YEARMONTHDAY = "[0-9]{8}";

    //BOOLEAN
    public static final String FALSEBOOLEANPATTERN = "(?i)^(false|'false|n|n - no|no|não|0|n - não|nao|n - nao)$";
    public static final String TRUEBOOLEANPATTERN = "(?i)^(true|'true|y|y - yes|yes|sim|1|s - sim)$";


    //User
    public static final String ACTIVEUSER = "ActiveUser";

    //IMPORT
    //Headers
    public static final String SASTEMPLATE = "Template";
    public static final String SASCOLUMN = "Column";
    public static final String SASROW = "Row";
    public static final String SASSHEET = "Sheet";
    public static final String SASVALUE = "Value";
    public static final String SASIGNORE = "Ignore";
    //Header NAMES
    public static final String HEADERTEMPLATE = "TEMPLATE";
    public static final String HEADERCOLUMN = "COLUMN";
    public static final String HEADERROW = "ROW";
    public static final String HEADERSHEET = "SHEET";
    public static final String HEADERVALUE = "VALUE";
    public static final String HEADERVAL = "VAL";
    public static final String HEADERCELL = "CELL";
    //Status
    public static final String IMPORTOK = "OK";
    public static final String IMPORTNOTOK = "NOK";
    public static final String IMPORTCANCEL = "CANCEL";
    public static final String IMPORTERROR = "OKE";

    public static final String IMPORT_REGEX = "^((\\d{4})_(?i)(.*.)_(?i)(ind|con)_((\\d{4})(\\d{2})(\\d{2})).*.(xlsx))$";
    public static final int REGEX_GROUP_1 = 1;
    public static final int REGEX_GROUP_2 = 2;
    public static final int REGEX_GROUP_3 = 3;
    public static final int REGEX_GROUP_4 = 4;
    public static final int REGEX_GROUP_5 = 5;
    public static final int REGEX_GROUP_6 = 6;
    public static final int REGEX_GROUP_7 = 7;
    public static final int REGEX_GROUP_8 = 8;
    public static final int REGEX_GROUP_9 = 9;

    //INDEX
    public static int FIRSTOCCURENCE = 0;
    public static int UNIQUEELEMENTONLIST = 1;

    //Separators
    public static char DOTSEPARATOR = '.';
    public static String UNDERSCORESEPARATOR = "_";
    public static String EBASEPARATOR = "eba_";

    //Types
    public static String INVALIDVALUE = "invalidValue";
    public static String SHEETCODE = "SheetCODE";
    public static int DESAGREGATIONCODETYPE = 1;
    public static int DESAGREGATIONCODEFIXEDTYPE = 3;
    public static int ROWKEYTYPE = 2;
    
    //Coordinates
    public static String SHEETCOORDINATE = "Z";
    public static String ColumnCoordinate = "X";
    public static String RowCoordinate = "Y";

    //ioTypeState
    public static final int tipoStateOK = 1;
    public static final int tipoStateNotOk = 2;
    public static final int tipoStatePending = 3;
    public static final int tipoStateCanceled = 4;
    
    public static char SheetCoordinateAsChar = 'Z';
    public static char ColumnCoordinateAsChar = 'X';
    public static char RowCoordinateAsChar = 'Y';
    
    //ioState
    public static final int processoOk = 1;
    public static final int processoOkWithError = 2;
    public static final int processoNotOk = 3;
    public static final int processoPending = 4;
    public static final int processoCanceled = 5;
    public static final int ruleOk = 7;
    public static final int processoOkDeleted = 10;
    public static final int processoOkEmpty = 12;
    

    public static final Pair<Integer, Integer> RULEDONOTRUNPREREQUISITE = new Pair<>(6, tipoStateOK);
    public static final Pair<Integer, Integer> RULEOK = new Pair<>(7, tipoStateOK);
    public static final Pair<Integer, Integer> RULENOTOK = new Pair<>(8, tipoStateOK);
    public static final Pair<Integer, Integer> RULEOKWITHNOTOK = new Pair<>(11, tipoStateNotOk);
    public static final Pair<Integer, Integer> RULEDONOTRUN = new Pair<>(13, tipoStateOK);

    //confAction
    public static final short actionImport = 1;
    public static final short actionValidation = 2;
    public static final short actionGeneration = 3;
    public static final short actionLock = 4;
    public static final short actionUnlock = 5;
    
    public static String IMPORTNORMAL = "IMPORTNORMAL";
    
    public static String EMPTTYROWSAS = "0000";
    
    public static String OPENROWCODE = "999";

    public static int COMMITSPERSAS = 1000;
    
    public static int IMPORTRULEDATE = 1;
    public static int IMPORTRULEDATETIME = 3;
    public static int IMPORTRULEBOOLEAN = 4;
    public static int IMPORTRULE = 2;
    
    
    //MandatoryReport
    public static int MANDATORYMODULEINDEX = 0;
    public static int MANDATORYENTITYINDEX = 1;
    public static int MANDATORYMAPINDEX = 2;
    public static int MANDATORYSTARTDATEINDEX = 3;
    public static int MANDATORYENDDATEINDEX = 4;
    public static int MANDATORYPERIODOCITYINDEX = 5;
    public static int MANDATORYTIMESTAMPINDEX = 6;
    public static int MANDATORYUSERINDEX = 7;
    public static String MANDATORY = "MANDATORY";
    
    public static String reportsPackage = "reportsPackage.json";
    public static String reports = "reports";
    public static String reportJSON = "report.json";
    public static String metaInf = "META-INF";
    public static String REPORTJSONCONTENTPARTBEGIN = "{\n\t\"documentInfo\": {\n\t\t\"documentType\": \"https://xbrl.org/2021/xbrl-csv\",\n\t\t\"extends\": [\n\t\t\t\"";
    public static String REPORTJSONCONTENTPARTEND = "\"\n\t\t]\n\t}\n}";
    
    public static String OK = "OK";
    public static String OKMapasComErros = "OK C/ MAPAS COM ERROS";
    public static String PENDENTE = "PENDENTE";
    public static String NOTOK = "NOT OK";
    public static String OKComMapasVazios = "OK C/ MAPAS VAZIOS";

    public static Integer GENERATIONBASEDONCOLLUMN = 1;
    public static Integer GENERATIONBASEDONDATAPOINTS = 2;

    //Special Char Threatment
    public static String LESSTHANOREQUALSCHAR = "≤";
    public static String GREATERTHANOREQUALSCHAR = "≥";
    public static String LESSTHANOREQUALSSTRING = "<=";
    public static String GREATERTHANOREQUALSSTRING = ">=";
    
    public static Object[] VALIDATIONRESULTSHEADER = new Object[]{"Ref. Date", "Módulo", "Entidade", "Domínio", "Relatório", "Severidade", "Domínio Regra",
                 "Origem Regra", "Regra com valores", "Origem",	"Resultado", "Data processamento", "Diferença", "Margem"};
    public static String VALIDATIONSHEETNAME = "Validações";
    public static String VALIDATIONFILENAME = "Validations_";
    
    //Arelle XBRL Validator
    public static Path arellePath = Paths.get("D:", File.separator, "Arelle", File.separator, "arelle-win", File.separator, "arelleCmdLine.exe");
    public static Path fullTaxonomyPath = Paths.get("D:", File.separator, "Taxonomy");
    public static Path reportsDirectory = Paths.get("D:", File.separator, "Taxonomy", File.separator, "reports");
    
    //XBRL CSV Templates
    public static String FILLINGINDICATORSFILENAME = "FilingIndicators.csv";
    public static String FILLINGINDICATORSLABELS = "templateID,reported";
    public static String PARAMETERSFILENAME = "parameters.csv";
    public static String PARAMETERSLABELS = "name,value";
    public static String PARAMETERSKEYENTITY = "entityID,rs:";  
    public static String PARAMETERSKEYREFERENCEDATE = "refPeriod,";  
    public static String PARAMETERSKEYCURRENCY = "baseCurrency,";  
    public static String PARAMETERSKEYMONETARY = "decimalsMonetary,";
    public static String PARAMETERSKEYPERCENTAGE = "decimalsPercentage,";
    public static String PARAMETERSKEYDECIMAL = "decimalsDecimal,";
    public static String PARAMETERSKEYINTEGER = "decimalsInteger,";
    public static String APPCONFIGCURRENCY = "CURRENCY";
    public static String APPCONFIGMONETARY = "MONETARY";
    public static String APPCONFIGPERCENTAGE = "PERCENTAGE";
    public static String APPCONFIGDECIMAL = "DECIMAL";
    public static String APPCONFIGINTEGER = "INTEGER";

    public static int MONETARYVARIABLEWITHUNITITEMID = 4646;
    public static String PARAMETERSKEYUNIT = "unit";
    public static String CURRENCYDESAGREGATIONCODEHEADERNAME = "CUS";
    public static String CURRENCYUNITPREFIX = "iso4217:";
    public static String CURRENCYDESAGREGATIONCODEPREFIX = "eba_CU:";
    public static String MAPPEDCSVFIXEDHEADER = "datapoint,factValue";

    public static String publicKeyDirectory = "public.pem";
    public static String licenseStringDirectory = "licenseString.dat";
    public static String charSet = "UTF-8";
    public static String signatureString = "signature";
    public static String licenseString = "license";
    public static String algoritmString = "RSASSA-PSS";
    public static String mdNameString = "SHA-256";
    public static String mgfNameString = "MGF1";
    public static String LEICodeKeyString = "LEICode";
    public static String BDPIDKeyString = "BDPID";
    public static String hardwareIDKeyString = "hardware";
    public static String expirationDateString = "expiry";
    public static String beginRegex = "-----BEGIN (.*)-----";
    public static String endRegex = "-----END (.*)-----";
    public static String whiteSpaceRegex = "\\s";
    public static String RSAAlgoritmString = "RSA";

    public static String fileUrl = "https://blobstoragexbrl.blob.core.windows.net/xbrldatabaseblob/UNMANAGEDPROCESS.db";
    public static String UPLOAD_DIR = System.getProperty("user.dir");
    public static String localFilePath = UPLOAD_DIR + File.separator + "/src/UNMANAGEDPROCESS.db"; 
    
    public static String folderUrl = "https://blobstoragexbrl.blob.core.windows.net/xbrldatabaseblob/JSONs";

    public static String folderTemplateUrl = "https://blobstoragexbrl.blob.core.windows.net/xbrldatabaseblob/Templates";

    public static String sourceFilePath = System.getProperty("user.dir") + File.separator + "/src/UNMANAGEDPROCESS.db";
    public static String targetFilePath = System.getProperty("user.dir") + File.separator + "UNMANAGEDPROCESS.db";

    public static String connectionStringSqlite = "jdbc:sqlite:UNMANAGEDPROCESS.db";
    
}
