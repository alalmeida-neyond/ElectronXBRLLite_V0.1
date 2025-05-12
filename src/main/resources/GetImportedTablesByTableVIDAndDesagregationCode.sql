with
    importedIOs
    as
    (
        select io.ioid
        from IO io
            inner join io_state ioe on ioe.io_stateid = io.io_stateid
        where ioe.io_typestateid = :typeStateOk
            and actionId = :actionId
            and referencedate = :referenceDate
            and domain = :domain
            and entityId = :entityId
            and moduleVID = :moduleVID
    )
,
    importedTabledFiltered
    as
    (
        select impTable.*
        from in_importedtablestemp impTable
            inner join importedIOs io on io.ioid = impTable.ioid
    )
,
    maxImportedTableIdPerTableWithoutDesagCode
    as
    (
        select max(impTable.importedTableId) importedTableId, impTable.tablevid, null as desagCode
        from importedTabledFiltered impTable
            inner join importedIOs impIo on impIO.ioid = impTable.ioid
        where impTable.importkeyid is null
        group by impTable.tablevid, impTable.importKeyId
    )
,
    maxImportedTableIdPerTableWithDesagCode
    as
    (
        select importedtableid, tablevid, listagg(desagCode, '|' ) within group (order by desagCode) desagCode
        from (
            select max(impTable.importedtableid) importedtableid, impTable.tablevid, keyA.propertyvalue as desagCode
                from importedTabledFiltered impTable
                    inner join importedIOs impIo on impIO.ioid = impTable.ioid
                    inner join in_importkey impK on impK.importkeyid = imptable.importkeyid
                    inner join in_keyassociation keyA on keyA.importkeyid = impK.importkeyid
                where impK.keytypeid = :desagregationCodeType
                group by impTable.tablevid, keyA.propertyvalue
        )
        group by importedtableid, tablevid
    )
,
    lockedInfo
    as
    (
        select lockA.lockassociationid
        from io
            inner join io_state ioe on ioe.io_stateid = io.io_stateid
            inner join lockassociation lockA on io.ioid = lockA.ioidgenerate
        where ioe.io_typestateid = :typeStateOk
            and actionId = :actionGenerateId
            and referencedate = :referenceDate
            and domain = :domain
            and entityId = :entityId
            and moduleVID = :moduleVID
    )
,
    lockedTables
    as
    (
        select lockA.importedTableId
        from lockedInfo lockI
            inner join lockimportassociation lockA on lockA.lockAssociationId = lockI.lockAssociationId
    )
,
    maxImportedTables
    as
    (
                    select *
            from maxImportedTableIdPerTableWithoutDesagCode
        union
            select *
            from maxImportedTableIdPerTableWithDesagCode
    )
select impTable.*
from maxImportedTables impTable
    left join lockedTables lockedTable
    on impTable.importedTableId = lockedTable.importedTableId
where lockedTable.importedTableId is null