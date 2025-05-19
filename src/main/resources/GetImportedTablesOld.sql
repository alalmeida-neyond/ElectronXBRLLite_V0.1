with importedIOs as ( 
    select io.ioid
    from IO io 
    inner join io_state ioe on ioe.io_stateid = io.io_stateid 
    where ioe.io_typestateid = :typeStateOk 
        and actionId = :actionId
        and (referencedate = strftime(:format, :referenceDate) OR :referenceDate IS NULL)
        and (domain = :domain OR :domain IS NULL)
        and (entityId = :entityId OR :entityId IS NULL)
        and (moduleVID = :moduleVID OR :moduleVID IS NULL)
)
, importedTabledFiltered as (
    select impTable.* 
    from in_importedtablestemp impTable
    inner join importedIOs io on io.ioid = impTable.ioid
)
, maxImportedTableIdPerTable as ( 
    select max(impTable.importedTableId) importedTableId, impTable.tablevid
    from importedTabledFiltered impTable 
    inner join importedIOs impIo on impIO.ioid = impTable.ioid 
    group by impTable.tablevid
)
select impTable.*
from in_importedtablestemp impTable 
inner join maxImportedTableIdPerTable maxImported on maxImported.importedTableId = impTable.importedTableId
inner join DPM_ED.io io on io.ioid = impTable.ioid
left join dpm_md.tableversion tv on impTable.tablevid = tv.tablevid
where impTable.io_stateid = :stateOk and io.io_stateid <> ?processOkDeleted
order by tv.code