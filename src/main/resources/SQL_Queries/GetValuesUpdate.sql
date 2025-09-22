with modulesApplicable as (
    select mv.modulevid
    from operationversion opv
    inner join operationscope ops on opv.operationvid = ops.operationvid
    inner join operationscopecomposition opsc on ops.operationscopeid = opsc.operationscopeid
    inner join moduleversion mv on opsc.modulevid = mv.modulevid
        where (mv.fromreferencedate <= strftime(:format, :refdate) AND IFNULL(mv.toreferencedate, strftime(:format, '9999-12-31')) >= strftime(:format, :refdate) 
          AND mv.fromreferencedate <> IFNULL(mv.toreferencedate, strftime(:format, '9999-12-31'))) 
          AND opv.operationvid = :operationVId
)

, tablesFromModulesApplicable as (
    select mvc.tablevid
    from modulesApplicable ma
    inner join moduleversioncomposition mvc on ma.modulevid = mvc.modulevid
)

, operandReferences as ( 
    select opn.nodeid, opr.operandreferenceid, opr.variableid, oprl.cellid
    from operationversion opv 
    inner join operationnode opn on opv.operationvid = opn.operationvid
    inner join operandreference opr on opn.nodeid = opr.nodeid
    left join operandreferencelocation oprl on opr.operandreferenceid = oprl.operandreferenceid
    where opv.operationvid = :operationVId
)

, operandReferencesVariableVID as (
    select opr.nodeid, opr.operandreferenceid, tvc.tablevid, tvc.cellid, vv.variablevid
    from operandReferences opr
    inner join variable v on opr.variableid = v.variableid
    inner join variableversion vv on v.variableid = vv.variableid
    inner join tableversioncell tvc on tvc.cellid = opr.cellid and tvc.variablevid = vv.variablevid
    inner join tablesFromModulesApplicable tfma on tvc.tablevid = tfma.tablevid
)
--select * from operandReferencesVariableVID;

, variablesWithDataType as (
    select distinct orv.variablevid, dt.datatypeid
    from operandReferencesVariableVID orv
    inner join variableversion vv on vv.variablevid = orv.variablevid
    inner join property p on vv.propertyid = p.propertyid
    inner join datatype dt on p.datatypeid = dt.datatypeid
)


, importedTabledFiltered as (
    select impTable.* 
    from in_importedtablestemp impTable
	where impTable.ioid = :ioId
)

, maxImportedTableIdPerTableWithoutDesagCode as ( 
    select max(it.importedTableId) importedTableId, it.tablevid, it.importkeyid
    from operandReferencesVariableVID orv
    inner join importedTabledFiltered it on orv.tablevid = it.tablevid 
    where it.importkeyid is null and it.io_stateid = :stateOk
    group by it.tablevid, it.importKeyId 
)

, maxImportedTableIdPerTableWithDesagCode as (
    select results.importedtableid, results.tablevid
    from (
        select max(it.importedtableid) importedtableid, it.tablevid, keyA.propertyvalue as desagCode  
        from operandReferencesVariableVID orv
        inner join importedTabledFiltered it on orv.tablevid = it.tablevid
        inner join in_importkey impK on impK.importkeyid = it.importkeyid 
        inner join in_keyassociation keyA on keyA.importkeyid = it.importkeyid 
        where impK.keytypeid = :desagregationCodeType or impK.keytypeid = :desagregationTypeFixed
        group by it.tablevid, keyA.propertyvalue
        ) results 
    inner join in_importedtablestemp it on it.importedtableid = results.importedtableid
    where it.io_stateid = :stateOk
    group by results.importedtableid, results.tablevid
)
 
, tablesImportedApplicable as (
    select mt.importedtableid, mt.tablevid, it.importkeyid
    from maxImportedTableIdPerTableWithDesagCode mt
    inner join importedTabledFiltered it on mt.importedtableid = it.importedtableid
    union
    select * from maxImportedTableIdPerTableWithoutDesagCode
)

, referencesTablesImportedNotDesagCodeFixed as (
    select orv.nodeid as "NodeID", orv.operandreferenceid as "OperandReferenceID", orv.cellid as "CellID", orv.variablevid as "VariableVID", tia.tablevid as "TableVID", tia.importedtableid as "TableID", tia.importkeyid as "DesagregationCode"
    from operandReferencesVariableVID orv
    left join tablesImportedApplicable tia on tia.tablevid = orv.tablevid
    left join in_importkey ik on tia.importkeyid = ik.importkeyid 
    where ik.keytypeid is null or ik.keytypeid <> :desagregationTypeFixed
)

, tablesImportedApplicableWithDesagCodeFixed as (
    select tia.*, ka.propertyvalue
    from tablesImportedApplicable tia
    left join in_importedtablestemp it on tia.importedtableid = it.importedtableid
    left join in_importkey ik on it.importkeyid = ik.importkeyid 
    left join in_keyassociation ka on ka.importkeyid = ik.importkeyid
    where ik.keytypeid = :desagregationTypeFixed
)

, referencesTablesImportedDesagCodeFixed as (
    select orv.nodeid as "NodeID", orv.operandreferenceid as "OperandReferenceID", orv.cellid as "CellID", orv.variablevid as "VariableVID", tiaDCF.tablevid as "TableVID", tiaDCF.importedtableid as "TableID", tiaDCF.importkeyid as "DesagregationCode"
    from operandReferencesVariableVID orv
    left join tableversioncell tvc on tvc.tablevid = orv.tablevid and tvc.cellid = orv.cellid and tvc.variablevid = orv.variablevid
    left join cell c on c.cellid = tvc.cellid
    left join header h on h.headerid = c.sheetid
    left join headerversion hv on hv.headerid = h.headerid
    left join tablesImportedApplicableWithDesagCodeFixed tiaDCF on orv.tablevid = tiaDCF.tablevid and tiaDCF.propertyvalue = hv.code
    where (h.direction = :directionZ)     
)

, referencesTablesImported as (
    select *
    from referencesTablesImportedNotDesagCodeFixed 
    
    union 
    
    select *
    from referencesTablesImportedDesagCodeFixed 
)
--select * from referencesTablesImported;

, rowsApplicableImported as (
    select distinct tia.importedtableid as "TableID", tia.tablevid as "TableVID", tia.importkeyid as "DesagregationCode", iv.importkeyid as "RowKey"
    from tablesImportedApplicable tia
    inner join in_importedvaluestemp iv on tia.importedtableid = iv.importedtableid
)

, allReferences as (
    select rti."NodeID", rti."OperandReferenceID", rti."CellID", rti."VariableVID", rti."TableVID", rti."TableID", rti."DesagregationCode", rai."RowKey"
    from referencesTablesImported rti
    left join rowsApplicableImported rai 
        on rti."TableID" = rai."TableID" 
        and rti."TableVID" = rai."TableVID" 
        and (rti."DesagregationCode" = rai."DesagregationCode" or (rti."DesagregationCode" is null and rai."DesagregationCode" is null))    
)

, valuesImportedModulesApplied as (
    select tia.importedtableid as "TableID", tia.tablevid as "TableVID", iv.variablevid as "VariableVID", iv.rulevalue as "Value", tia.importkeyid as "DesagregationCode", iv.importkeyid as "RowKey"
    from tablesImportedApplicable tia
    inner join in_importedvaluestemp iv on tia.importedtableid = iv.importedtableid
)

, propertiesOnOperation as (
    select opr.operandreferenceid "RefID", opr.nodeid "NodeID", dt.*, ic.code "Value", -1 RowKeyID, -1 RowKeyTypeID, -1 DesagregationCodeID, -1 DesagregationCodeTypeID, null "X", null "Y",  null "Z", 'Property' "Type"
    from operandReferences ors
    left join operandreference opr on ors.operandreferenceid = opr.operandreferenceid
    inner join property p on opr.propertyid = p.propertyid
    inner join itemcategory ic on ic.itemid = p.propertyid
    inner join datatype dt on dt.datatypeid = p.datatypeid
    where opr.propertyid is not null
)

, refPeriodOperandReferences as (
    select opr.operandreferenceid "RefID", opr.nodeid "NodeID", dt.*, opr.operandreference "Value", -1 RowKeyID, -1 RowKeyTypeID, -1 DesagregationCodeID, -1 DesagregationCodeTypeID, null "X", null "Y",  null "Z", 'refPeriod' "Type"
    from operandReferences ors
    left join operandreference opr on ors.operandreferenceid = opr.operandreferenceid
    left join datatype dt on dt.datatypeid = :dataTypeDate
    where opr.operandreference = :refPeriodString
)

, itemOnOperation as (
    select opr.operandreferenceid "RefID", opr.nodeid "NodeID", dt.* , ic.signature "Value", -1 RowKeyID, -1 RowKeyTypeID, -1 DesagregationCodeID, -1 DesagregationCodeTypeID, null "X", null "Y",  null "Z", 'Item' "Type"
    from operandReferences ors
    left join operandreference opr on ors.operandreferenceid = opr.operandreferenceid
    inner join itemcategory ic on ic.itemid = opr.itemid
    left join property p on p.propertyid = opr.itemid
    left join datatype dt on (dt.datatypeid = p.datatypeid or (p.datatypeid is null and dt.datatypeid = :dataTypeEnumeration))
    where opr.itemid is not null
)

, indexOnOperations as (
    select opr.operandreferenceid "RefID", opr.nodeid "NodeID", dt.*, opr.operandreference "Value", -1 RowKeyID, -1 RowKeyTypeID, -1 DesagregationCodeID, -1 DesagregationCodeTypeID, null "X", null "Y",  null "Z", 'Property' "Type"
    from operandReferences ors
    left join operandreference opr on ors.operandreferenceid = opr.operandreferenceid
    left join datatype dt on (dt.datatypeid = :dataTypeEnumeration)
    where opr.operandreference in (:referenceRow, :referenceColumn, :referenceSheet)
)

, extraValues as (
    select * from propertiesOnOperation 
    union all
    select * from itemOnOperation
    union all 
    select * from refPeriodOperandReferences
    union all 
    select * from indexOnOperations
)

, valuesWithRef as (
    select ROW_NUMBER() OVER () AS ValueID, valuesToOperation.*
    from (
        select 
            ar."OperandReferenceID" "RefID",  
            ar."NodeID" as "NodeID", 
            dt.*, 
            vima."Value" as "Value", 
            iRK.IMPORTKEYID AS RowKeyID, 
            iRK.KEYTYPEID AS RowKeyTypeID,
            iDC.IMPORTKEYID AS DesagregationCodeID,
            iDC.KEYTYPEID AS DesagregationCodeTypeID, 
            opr.x as "X", 
            opr.y as "Y", 
            opr.z as "Z",
            'Value' as "Type"
        from allReferences ar
        left join operandreference opr on ar."OperandReferenceID" = opr.operandreferenceid
        left join valuesImportedModulesApplied vima 
            on ar."VariableVID" = vima."VariableVID"
            and ar."TableVID" = vima."TableVID"
            and (ar."DesagregationCode" = vima."DesagregationCode" or (ar."DesagregationCode" is null and vima."DesagregationCode" is null))
            and (ar."RowKey" = vima."RowKey" or (ar."RowKey" is null and vima."RowKey" is null) )
        left join variablesWithDataType vdt on ar."VariableVID" = vdt.variablevid
        left join datatype dt on vdt.datatypeid = dt.datatypeid
        left join in_importkey iRK on (iRK.importkeyid = ar."RowKey" or (ar."RowKey" is null and iRK.importkeyid = -1))
        left join in_importkey iDC on (iDC.importkeyid = ar."DesagregationCode" or (ar."DesagregationCode" is null and iDC.importkeyid = -1))
        
        union all
        
        select * from extraValues
    ) valuesToOperation 
    order by "NodeID", "X" , "Y", "Z"
)
--select * from valuesWithRef;
, rowKeyValues as (
    select 
        vr.ValueID,
        case 
            when vr.RowKeyID <> -1 then 'r('
            else null 
        end || 
        GROUP_CONCAT(
            case 
                when vr.RowKeyID <> -1 then keyA.propertyvalue
                else null 
            end
        , ',' order by keyA.propertyvalue) ||
        case 
            when vr.RowKeyID <> -1 then ')'
            else null 
        end RowKeyValue
    from valuesWithRef vr
    left join in_importkey impK on impK.importkeyid = vr.RowKeyID
    left join in_keyassociation keyA on keyA.importkeyid = impK.importkeyid 
    group by vr.ValueID, vr.RowKeyID
)

--select * from rowKeyValues;
, desagregationCodesValues as (
    select 
        vr.ValueID, vr.DesagregationCodeID, 
        case 
            when vr.DesagregationCodeTypeID = :desagregationCodeType then '('
            else null 
        end || 
        GROUP_CONCAT(
            case 
                when vr.DesagregationCodeTypeID = :desagregationCodeType then (keyA.propertyname || '=' || keyA.propertyvalue)
                else null 
            end
        , ',' order by keyA.propertyvalue) ||
        case 
            when vr.DesagregationCodeTypeID = :desagregationCodeType then ')'
            else null 
        end DesagregationCodeValue
    from valuesWithRef vr
    left join in_importkey impK on impK.importkeyid = vr.DesagregationCodeID
    left join in_keyassociation keyA on keyA.importkeyid = impK.importkeyid 
    group by vr.ValueID, vr.DesagregationCodeID, vr.DesagregationCodeTypeID
)

--select * from desagregationCodesValues;
select 
    vr.*,
    case 
        when oprl.operandreferenceid is null then null
        else (oprl."Table" || 
                case 
                    when dcv.DesagregationCodeValue is not null then dcv.DesagregationCodeValue
                    else '' 
                end || 
                case 
                    when rkv.RowKeyValue is not null then ',' || rkv.RowKeyValue 
                    else ',r' || oprl."Row" 
                end || 
                case 
                    when oprl."Column" is not null then ',c' || oprl."Column" 
                    else ''
                end ||
                case 
                    when dcv.DesagregationCodeValue is null and oprl.sheet is not null then ',s' || oprl.sheet 
                    else ''
                end)
        end ValueDomain
from valuesWithRef vr
left join operandreferencelocation oprl on vr."RefID" = oprl.operandreferenceid
left join rowKeyValues rkv on vr.ValueID = rkv.ValueID
left join desagregationCodesValues dcv on vr.ValueID = dcv.ValueID
