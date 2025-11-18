with importedTabledFiltered as (
    select impTable.* 
    from DPM_OD.in_importedtablestemp impTable
    where impTable.ioid = ?ioId
)
, maxImportedTableIdPerTable as ( 
    select max(impTable.importedTableId) importedTableId, impTable.tablevid
    from importedTabledFiltered impTable 
    where impTable.ioid = ?ioId
    group by impTable.tablevid
)
select impTable.*
from DPM_OD.in_importedtablestemp impTable 
inner join maxImportedTableIdPerTable maxImported on maxImported.importedTableId = impTable.importedTableId
inner join DPM_OD.io io on io.ioid = impTable.ioid
left join dpm_md.tableversion tv on impTable.tablevid = tv.tablevid
where impTable.io_stateid = ?stateOk and io.io_stateid <> ?processOkDeleted
order by tv.code