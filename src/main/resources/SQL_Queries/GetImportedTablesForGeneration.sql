with importedTabledFiltered as (
    select impTable.* 
    from in_importedtablestemp impTable
    where impTable.ioid = ?ioId
)
, maxImportedTableIdPerTableWithoutDesagCode as ( 
    select max(impTable.importedTableId) importedTableId, impTable.tablevid, null as desagCode
    from importedTabledFiltered impTable 
    where impTable.importkeyid is null 
    group by impTable.tablevid, impTable.importKeyId 
)
, maxImportedTableIdPerTableWithDesagCode as (
    select importedtableid, tablevid, listagg(desagCode, '|' ) within group (order by desagCode) desagCode
    from (
        select max(impTable.importedtableid) importedtableid, impTable.tablevid, keyA.propertyvalue as desagCode  
        from DPM_OD.in_importedtablestemp impTable 
        inner join DPM_OD.in_importkey impK on impK.importkeyid = imptable.importkeyid 
        inner join DPM_OD.in_keyassociation keyA on keyA.importkeyid = impK.importkeyid 
        where impK.keytypeid = ?desagregationCodeType
        group by impTable.tablevid, keyA.propertyvalue
        ) 
    group by importedtableid, tablevid
)
, tablesImportedWithoutCode as ( 
    select impTable.* 
    from DPM_OD.in_importedtablestemp impTable 
    inner join maxImportedTableIdPerTableWithoutDesagCode maxImported on maxImported.importedTableId = impTable.importedTableId 
    inner join DPM_OD.io io on io.ioid = impTable.ioid 
    inner join tableVersion tableV on tableV.tablevid = impTable.tablevid 
    inner join moduleVersion module on module.modulevid = io.modulevid 
    inner join DPM_OD.conf_entities entity on entity.entityId = io.entityId 
)
, tablesImportedWithCode as ( 
    select impTable.* 
    from DPM_OD.in_importedtablestemp impTable 
    inner join maxImportedTableIdPerTableWithDesagCode maxImported on maxImported.importedTableId = impTable.importedTableId 
    inner join DPM_OD.io io on io.ioid = impTable.ioid 
    inner join tableVersion tableV on tableV.tablevid = impTable.tablevid 
    inner join moduleVersion module on module.modulevid = io.modulevid 
    inner join DPM_OD.conf_entities entity on entity.entityId = io.entityId 
    inner join DPM_OD.in_importkey impK on impK.importkeyid = imptable.importkeyid 
    inner join DPM_OD.in_keyassociation keyA on keyA.importkeyid = impK.importkeyid 
    where impK.keytypeid = ?desagregationCodeType 
)
select impTable.*
from (
    select * from tablesImportedWithCode 
    union 
    select * from tablesImportedWithoutCode
) impTable
where impTable.io_stateid != ?processOkDeleted and impTable.io_stateid != ?processOkEmpty