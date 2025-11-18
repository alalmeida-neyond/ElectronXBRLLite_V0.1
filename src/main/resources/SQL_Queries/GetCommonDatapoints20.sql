with importIOs as(
    select io.ioid
    from DPM_OD.IO io 
    inner join DPM_OD.io_state ioe on ioe.io_stateid = io.io_stateid 
    where ioe.io_typestateid = ?typeStateOk 
        and actionId = ?actionId
        and referencedate = to_date(?referenceDate,?format)
        and domain = ?domain 
        and entityId = ?entityId
)
, tableVIDThatCanCross as (
    Select tvcother.TableVID 
    from DPM_MD.TableVersionCell tvc
    inner join DPM_MD.TableVersionCell tvcother on tvc.variablevid = tvcother.variablevid
    where tvc.TableVID = ?tableVID and tvcother.TableVID <> ?tableVID and tvc.CellID <> tvcother.cellid
    GROUP by tvcother.TableVID
)
, variableVIDThatCanCross as (
    Select tvcother.variablevid
    from DPM_MD.TableVersionCell tvc
    inner join DPM_MD.TableVersionCell tvcother on tvc.variablevid = tvcother.variablevid
    where tvc.TableVID = ?tableVID and tvcother.TableVID <> ?tableVID and tvc.CellID <> tvcother.cellid
    GROUP by tvcother.variablevid
)
, allImportedTables as (
    Select it.*
    from DPM_OD.IN_ImportedTablesTemp it
    inner join importIOs ai on ai.ioid = it.ioid
    inner join DPM_OD.io_state st on it.io_stateid = st.io_stateid
    where st.io_typestateid = ?typeStateOk 
)
, allValidTables as (
    select max(importedTableID) as importedTableID, TableVID, MapCode, desagregationcodeAlt
    from (
        Select it.importedTableID as importedTableID, it.TableVID,tv.code as MapCode,LISTAGG(ka.PropertyValue, '|') WITHIN GROUP (ORDER BY ka.propertyName) AS desagregationcodeAlt
        from allImportedTables it
        inner join DPM_MD.TableVersion tv on tv.TableVID = it.TableVID and tv.tableid <> ?tableID
        inner join tableVIDThatCanCross tvcc on tv.TableVID = tvcc.TableVID
        left join DPM_OD.IN_KeyAssociation ka ON ka.importKeyid = it.importKeyid
        where it.TableVID <> ?tableVID 
        group by it.importedTableID,it.TableVID,tv.code
    ) results
    group by TableVID, MapCode, desagregationcodeAlt
)
, validCurrentTable as (
    select max(importedTableID) as importedTableID, TableVID, MapCode, desagregationcodeAlt
    from (
        Select it.importedTableID as importedTableID, it.TableVID,tv.code as MapCode,LISTAGG(ka.PropertyValue, '|') WITHIN GROUP (ORDER BY ka.propertyName) AS desagregationcodeAlt
        from allImportedTables it
        inner join DPM_MD.TableVersion tv on tv.TableVID = it.TableVID and tv.tableid = ?tableID
        left join DPM_OD.IN_KeyAssociation ka ON ka.importKeyid = it.importKeyid
        where it.TableVID = ?tableVID 
        group by it.importedTableID,it.TableVID,tv.code
    ) results 
    group by TableVID, MapCode, desagregationcodeAlt
)
,importedCurrentValues as (
    Select it.MapCode, cel.cellID,cel."RowID",cel.columnID,cel.sheetID,
            it.tableVID,iv.IMPORTEDVALUESID,iv.IMPORTEDTABLEID,iv.RULEVALUE,iv.VARIABLEVID ,LISTAGG(ka.PropertyValue, '|') WITHIN GROUP (ORDER BY ka.propertyName) AS rowKeyAlt,it.desagregationcodeAlt
    from DPM_OD.IN_ImportedValuesTemp iv
    inner join validCurrentTable it  on iv.IMPORTEDTABLEID = it.importedTableID
    inner join variableVIDThatCanCross vv on vv.variablevid = iv.VARIABLEVID
    inner join DPM_MD.TableVersion tv on tv.TableVID = it.TableVID
    inner join DPM_MD.TableVersionCell tvc on tvc.VARIABLEVID = iv.VARIABLEVID and it.tableVID = tvc.tableVID and iv.cellid = tvc.cellid
    inner join DPM_MD.Cell cel on cel.cellID = tvc.cellID
    left join DPM_OD.IN_KeyAssociation ka ON ka.importKeyid = iv.importKeyid
    where it.TableVID = ?tableVID 
    group by it.MapCode,cel.cellID,cel."RowID",cel.columnID,cel.sheetID,
        it.tableVID,iv.IMPORTEDVALUESID,iv.IMPORTEDTABLEID,iv.RULEVALUE,iv.VARIABLEVID,it.desagregationcodeAlt
)
,otherTableValues as (
    Select vt.mapCode, cel.cellID,cel."RowID",cel.columnID,cel.sheetID,vt.tableVID, iv.IMPORTEDVALUESID,iv.IMPORTEDTABLEID,iv.RULEVALUE,iv.VARIABLEVID ,vt.desagregationcodeAlt, LISTAGG(ka.PropertyValue, '|') WITHIN GROUP (ORDER BY ka.propertyName) AS rowKeyAlt
    from DPM_OD.IN_ImportedValuesTemp iv
    inner join allValidTables vt on iv.IMPORTEDTABLEID = vt.importedTableID
    inner join variableVIDThatCanCross vv on vv.variablevid = iv.VARIABLEVID
    inner join DPM_MD.TableVersionCell tvc on tvc.VARIABLEVID = iv.VARIABLEVID and vt.tableVID = tvc.tableVID and iv.cellid = tvc.cellid
    inner join DPM_MD.Cell cel on cel.cellID = tvc.cellID
    left join DPM_OD.IN_KeyAssociation ka ON ka.importKeyid = iv.importKeyid
    group by vt.mapCode,cel.cellID,cel."RowID",cel.columnID,cel.sheetID,vt.tableVID,iv.IMPORTEDVALUESID,iv.IMPORTEDTABLEID,iv.RULEVALUE,iv.VARIABLEVID,vt.desagregationcodeAlt
)
,otherValidationHeaders as (
    Select tvh.HeaderVID, tvh.HeaderID
    from DPM_MD.TableVersionHeader tvh 
    inner join allValidTables avt on avt.TableVID = tvh.TableVID
    
)
,otherTableWithCode as (
    SELECT 
        imcv.*, 
        hvRow.code as RowCode, 
        hvCol.code as ColumnCode, 
        hvSheet.code as SheetCode
    FROM otherTableValues imcv
    LEFT JOIN DPM_MD.HeaderVersion hvRow ON hvRow.headerid = imcv."RowID"
    LEFT JOIN DPM_MD.HeaderVersion hvCol ON hvCol.headerid = imcv.columnID 
    LEFT JOIN DPM_MD.HeaderVersion hvSheet ON hvSheet.headerid = imcv.sheetID 
    LEFT JOIN otherValidationHeaders validHeadersRow ON hvRow.headervid = validHeadersRow.HeaderVID AND imcv."RowID" = validHeadersRow.HeaderID
    LEFT JOIN otherValidationHeaders validHeadersCol ON hvCol.headervid = validHeadersCol.HeaderVID AND imcv.columnID = validHeadersCol.HeaderID
    LEFT JOIN otherValidationHeaders validHeadersSheet ON hvSheet.headervid = validHeadersSheet.HeaderVID AND imcv.sheetID = validHeadersSheet.HeaderID
    WHERE 
        (validHeadersRow.HeaderVID IS NOT NULL OR hvRow.headervid IS NULL) AND
        (validHeadersCol.HeaderVID IS NOT NULL OR hvCol.headervid IS NULL) AND
        (validHeadersSheet.HeaderVID IS NOT NULL OR hvSheet.headervid IS NULL)
)
, importedCurrentValidHeaders as (
    Select tvh.HeaderVID, tvh.HeaderID
    from importedCurrentValues it  
    inner join variableVIDThatCanCross vvtcc on vvtcc.variableVID = it.variableVID
    inner join DPM_MD.TableVersionHeader tvh on it.TableVID = tvh.TableVID
    where it.TableVID = ?tableVID 
    group by tvh.HeaderVID, tvh.HeaderID
)   
, importedCurrentValuesWithCode as (
    SELECT 
        imcv.*, 
        hvRow.code as RowCode, 
        hvCol.code as ColumnCode, 
        hvSheet.code as SheetCode
    FROM importedCurrentValues imcv
    LEFT JOIN DPM_MD.HeaderVersion hvRow ON hvRow.headerid = imcv."RowID"
    LEFT JOIN DPM_MD.HeaderVersion hvCol ON hvCol.headerid = imcv.columnID 
    LEFT JOIN DPM_MD.HeaderVersion hvSheet ON hvSheet.headerid = imcv.sheetID 
    LEFT JOIN importedCurrentValidHeaders validHeadersRow ON hvRow.headervid = validHeadersRow.HeaderVID AND imcv."RowID" = validHeadersRow.HeaderID
    LEFT JOIN importedCurrentValidHeaders validHeadersCol ON hvCol.headervid = validHeadersCol.HeaderVID AND imcv.columnID = validHeadersCol.HeaderID
    LEFT JOIN importedCurrentValidHeaders validHeadersSheet ON hvSheet.headervid = validHeadersSheet.HeaderVID AND imcv.sheetID = validHeadersSheet.HeaderID
    WHERE 
        (validHeadersRow.HeaderVID IS NOT NULL OR hvRow.headervid IS NULL) AND
        (validHeadersCol.HeaderVID IS NOT NULL OR hvCol.headervid IS NULL) AND
        (validHeadersSheet.HeaderVID IS NOT NULL OR hvSheet.headervid IS NULL)
)
,crossedDatapoints as (
    Select cv.MapCode as OGmapCode, cv.rowCode as OGRow, cv.ColumnCode as OGColumn,cv.SheetCode as OGSheet,cv.rowKeyAlt as OGRowKey, cv.desagregationCodeAlt as OGDesagregationCode,cv.RuleValue as OGVALUE,OV.mapCode as COMMapCode ,OV.RuleValue as COMVALUE,ov.rowCode as COMRow, ov.ColumnCode as COMColumn,ov.SheetCode as COMSheet,ov.rowKeyAlt as COMRowKey, ov.desagregationCodeAlt as COMDesagregationCode
    from importedCurrentValuesWithCode cv 
    inner join otherTableWithCode ov on cv.RuleValue <> OV.RuleValue and cv.VariableVID = ov.VariableVID and (cv.rowKeyAlt = ov.rowKeyAlt or (cv.rowKeyAlt is null and ov.rowKeyAlt is null)) and (cv.desagregationcodeAlt = ov.desagregationcodeAlt or (cv.desagregationcodeAlt is null and ov.desagregationcodeAlt is null))
)
Select OGVALUE || ' = ' || COMVALUE as details, OGmapCode || ',r' || Case when OGRowKey is null then OGrow else '('|| OGRowKey || ')' End||',c'||OGColumn || Case when OGDesagregationCode is null then '' else ',s' ||OGDesagregationCode  End  ||' | ' || COMmapCode || ',r' || Case when COMRowKey is null then COMrow else '('|| COMRowKey || ')' End ||',c'||COMColumn ||Case when COMDesagregationCode is null then '' else ',s' ||COMDesagregationCode  End as domain
from crossedDatapoints