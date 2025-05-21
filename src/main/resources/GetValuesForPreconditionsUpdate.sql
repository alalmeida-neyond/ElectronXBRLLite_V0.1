with preconditionReferences as (
    select opn.nodeid, opr.operandreferenceid, opr.variableid
    from operationversion opv 
    inner join operationnode opn on opv.operationvid = opn.operationvid
    inner join operandreference opr on opn.nodeid = opr.nodeid
    left join operandreferencelocation oprl on opr.operandreferenceid = oprl.operandreferenceid
    where opv.operationvid = :preconditionVId
)

, operandReferencesVariableVID as (
    select opr.nodeid, vv.variablevid
    from preconditionReferences opr
    inner join variable v on opr.variableid = v.variableid
    inner join variableversion vv on v.variableid = vv.variableid
)

, importedTabledFiltered as (
    select impTable.* 
    from in_importedtablestemp impTable
	where impTable.ioid = :ioId
)

, tablesImported as (
    select max(it.importedtableid) importedtableid, it.tablevid, it.importkeyid, it.variablevid
    from importedTabledFiltered itF
    inner join  in_importedtablestemp it on itF.tablevid = it.tablevid
    where it.io_stateid = 1
    group by it.tablevid, it.importKeyId, it.variablevid
)


, nodeValues as (
    select opr.nodeid, opr.variablevid, CASE WHEN ti.variablevid IS NOT NULL THEN 'true' ELSE 'false' END as existsInTablesImported
    from operandReferencesVariableVID opr
    left join tablesImported ti on opr.variablevid = ti.variablevid
    group by opr.nodeid, opr.variablevid, ti.variablevid
) 

select 
        ROW_NUMBER() OVER () AS ValueID, 
        -1 "RefID",
        nv.nodeid as "NodeID", 
        dt.*, 
        nv.existsInTablesImported as "Value", 
        iRK.IMPORTKEYID AS RowKeyID, 
        iRK.KEYTYPEID AS RowKeyTypeID,
        iDC.IMPORTKEYID AS DesagregationCodeID,
        iDC.KEYTYPEID AS DesagregationCodeTypeID, 
        null as "X", 
        null as "Y", 
        null as "Z",
        'Value' as "Type",
        null ValueDomain
from nodeValues nv
left join datatype dt on dt.datatypeid = 4
left join in_importkey iRK on iRK.importkeyid = -1
left join in_importkey iDC on iDC.importkeyid = -1

