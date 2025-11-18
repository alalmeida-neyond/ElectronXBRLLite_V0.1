with preconditionReferences as (
    select opn.nodeid, opr.operandreferenceid, opr.variableid
    from dpm_md.operationversion opv 
    inner join dpm_md.operationnode opn on opv.operationvid = opn.operationvid
    inner join dpm_md.operandreference opr on opn.nodeid = opr.nodeid
    left join dpm_md.operandreferencelocation oprl on opr.operandreferenceid = oprl.operandreferenceid
    where opv.operationvid = ?preconditionVId
)

, operandReferencesVariableVID as (
    select opr.nodeid, vv.variablevid
    from preconditionReferences opr
    inner join dpm_md.variable v on opr.variableid = v.variableid
    inner join dpm_md.variableversion vv on v.variableid = vv.variableid
)

, importedIOs as ( 
    select io.*
    from DPM_OD.io io
    inner join DPM_OD.io_state ioe on ioe.io_stateid = io.io_stateid 
    where ioe.io_typestateid = ?typeStateOk
        and actionId = ?actionId
        and referencedate = to_date(?refdate,?format)
        and domain = ?domain
        and entityId = ?entityId
)
, importedTabledFiltered as (
    select impTable.* 
    from DPM_OD.in_importedtablestemp impTable
    inner join importedIOs io on io.ioid = impTable.ioid
)
, tablesImported as (
    select maxTables.* from (
        select max(it.importedtableid) importedtableid, it.tablevid, it.importkeyid, it.variablevid
        from importedTabledFiltered itF
        inner join  DPM_OD.in_importedtablestemp it on itF.importedtableid = it.importedtableid
        group by it.tablevid, it.importKeyId, it.variablevid
    )maxTables
    inner join DPM_OD.in_importedtablestemp itt on maxTables.importedtableid = itt.importedtableid
    where itt.io_stateid <> ?processoOkDeleted
)
, nodeValues as (
    select opr.nodeid, opr.variablevid, CASE WHEN ti.variablevid IS NOT NULL THEN 'true' ELSE 'false' END as existsInTablesImported
    from operandReferencesVariableVID opr
    left join tablesImported ti on opr.variablevid = ti.variablevid
    group by opr.nodeid, opr.variablevid, ti.variablevid
)

select 
        ROWNUM as ValueID, 
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
left join dpm_md.datatype dt on dt.datatypeid = 4
left join DPM_OD.in_importkey iRK on iRK.importkeyid = -1
left join DPM_OD.in_importkey iDC on iDC.importkeyid = -1

