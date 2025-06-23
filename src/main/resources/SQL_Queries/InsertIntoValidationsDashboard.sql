INSERT INTO "OUT_VALIDATIONSDASHBOARD" (
    ENTITYID, 
    DOMAIN, 
    MODULEVID, 
    REFERENCEDATE, 
    MANDATORYREPORTSIMPORTED, 
    OK, 
    DNRR, 
    WARNING, 
    ERROR, 
    PROCESSNOTOK, 
    TOTAL, 
    EXPECTEDTORUN,
    TIMESTAMP
)
with periodicityFromReferenceDate as (
	SELECT :referenceDate AS referenceDate, CASE WHEN CAST(strftime('%m%',strftime(:format, :referenceDate)) AS INTEGER) % 12 = 0 THEN 3 ELSE NULL END AS periodicityId
		UNION
	SELECT :referenceDate AS referenceDate, CASE WHEN CAST(strftime('%m%',strftime(:format, :referenceDate)) AS INTEGER) % 4 = 0 THEN 1 ELSE NULL END AS periodicityId
		UNION
	SELECT :referenceDate AS referenceDate, CASE WHEN CAST(strftime('%m%',strftime(:format, :referenceDate)) AS INTEGER) % 3 = 0 THEN 4 ELSE NULL END AS periodicityId
		UNION
	SELECT :referenceDate AS referenceDate, CASE WHEN CAST(strftime('%m%',strftime(:format, :referenceDate)) AS INTEGER) % 6 = 0 THEN 2 ELSE NULL END AS periodicityId
		UNION
	SELECT :referenceDate AS referenceDate, CASE WHEN CAST(strftime('%m%',strftime(:format, :referenceDate)) AS INTEGER) % 1 = 0 THEN 5 ELSE NULL END AS periodicityId
)
, importedTabledFiltered as (
    select impTable.*, io.modulevid, io.domain, io.entityId, io.referenceDate
    from in_importedtablestemp impTable
    inner join io io on io.ioid = impTable.ioid
    where impTable.io_stateid = :processoOk and impTable.ioid = :ioId
)
, maxImportedTableIdPerTable as ( 
    select max(impTable.importedTableId) importedTableId, impTable.tablevid tablevid, impTable.modulevid modulevid, impTable.domain domain, impTable.entityId entityId, impTable.referenceDate referenceDate
    from importedTabledFiltered impTable 
    group by tablevid, modulevid, domain, entityId, referenceDate
)
, importedTablesWithoutDeleted as (
    select mit.tablevid, mit.modulevid, mit.domain, mit.entityId, mit.referenceDate, io.endtimestamp
    from maxImportedTableIdPerTable mit
    inner join in_importedtablestemp it on it.importedTableId = mit.importedTableid
    inner join io io on io.ioid = it.ioid
    where io.io_stateid <> :processOkDeleted
)
,iosValidation AS (
    SELECT io.ioid, io.referencedate, mv.code, io.domain, confEn.leicode, io.endtimestamp, io.inittimestamp, io.entityid, io.modulevid, io.userid
    FROM io io
    INNER JOIN moduleversion mv ON io.modulevid = mv.modulevid
    INNER JOIN conf_entities confEn ON io.entityid = confEn.entityid
    WHERE io.ioid = :ioId
)
,validationContext as (
    select referenceDate, modulevid, entityId, domain
    from iosValidation
    group by referenceDate, modulevid, entityId, domain
)  
, tablesOfModules as (
    select vc.*, vm.moduleid, tv.tableid, tv.tablevid
    from moduleversion vm
    inner join validationContext vc on vc.modulevid = vm.modulevid
    inner join moduleVersionComposition mvc on mvc.modulevid = vc.modulevid
    inner join "TABLE" t on mvc.tableid = t.tableid 
    inner join tableVersion tv on tv.tablevid = mvc.tablevid
    where t.isabstract = 0
)
,mandatoryReports as (
    select cmr.* 
    from conf_mandatoryreport cmr
    inner join periodicityFromReferenceDate periodicity on periodicity.periodicityId = cmr.periodicityId
    where (cmr.fromDate <= strftime(:format, :referenceDate) AND IFNULL(cmr.toDate, strftime(:format, '9999-12-31')) >= strftime(:format, :referenceDate)
          AND cmr.fromDate <> IFNULL(cmr.toDate, strftime(:format, '9999-12-31')))
)
, tablesWithMandatory as (
    select modulevid, tablevid, entityid, domain from (
        select vt.modulevid, vt.tablevid, mr.entityid, mr.domain, tv.code, case when mr.mandatoryReportId is not null then 1 else 0 end as isMandatory--, tv.code
        from tablesOfModules vt
        left join mandatoryReports mr on vt.moduleid = mr.moduleid and mr.tableid = vt.tableid
        inner join tableversion tv on tv.tablevid = vt.tablevid
    )
    where ismandatory = 1
)
,mandatoryImportedPerModule as (
    select
        results.referencedate, 
        results.entityid, 
        results.domain, 
        results.modulevid,
        case when results.isMandatory = 1 and results.isImported = 0 then 1 else 0 end shouldBeenImportedButWasnt
    from (
        select tom.referencedate, tom.entityid, tom.domain, tom.modulevid, 
            case when itwd.tablevid is not null then 1 else 0 end isImported,
            case when twm.tablevid is not null then 1 else 0 end isMandatory
        from tablesOfModules tom 
        left join importedTablesWithoutDeleted itwd on 1 = 1
                                                    and itwd.tablevid = tom.tablevid
                                                    and itwd.referencedate = tom.referencedate 
                                                    and itwd.entityid = tom.entityid
                                                    and itwd.domain = tom.domain
                                                    and itwd.modulevid = tom.modulevid
        left join tablesWithMandatory twm on 1 = 1
                                        and tom.tablevid = twm.tablevid
                                        and tom.entityid = twm.entityid
                                        and tom.domain = twm.domain
                                        and tom.modulevid = twm.modulevid
    ) results 
)
, hasAnyMandatoryToImport as (
    select 
        mipm.referencedate, 
        mipm.entityid, 
        mipm.domain, 
        mipm.modulevid,
        max(mipm.shouldBeenImportedButWasnt) anyMandatoryToImport
    from mandatoryImportedPerModule mipm  
    group by mipm.referencedate, mipm.entityid, mipm.domain, mipm.modulevid
)
, operationVersionByModule as (
    select vc.modulevid, ov.operationvid, max(os.severity) severity
    from validationContext vc
    inner join operationscopecomposition osc on osc.modulevid = vc.modulevid
    inner join operationscope os on os.operationscopeid = osc.operationscopeid
    inner join operationversion ov on ov.operationvid = os.operationvid
    where os.isactive = 1 and os.fromsubmissiondate <= strftime(:format, :referenceDate)
    group by vc.modulevid, ov.operationvid
)
, operandReferenceFromOperations as (
    select ovm.modulevid, ovm.operationvid, opn.nodeid, opr.variableid, oprl.cellid, oprl."Table"
    from operationVersionByModule ovm 
    left join operationnode opn on ovm.operationvid = opn.operationvid
    left join operandreference opr on opn.nodeid = opr.nodeid
    inner join operandreferencelocation oprl on opr.operandreferenceid = oprl.operandreferenceid
)
, cellsFromTablesImported as (
    select itc.*, tvc.cellid, tvc.cellcode
    from importedTablesWithoutDeleted itc 
    inner join moduleversioncomposition mvc on itc.modulevid = mvc.modulevid and itc.tablevid = mvc.tablevid
    inner join tableversion tv on mvc.tableid = tv.tableid and mvc.tablevid = tv.tablevid
    inner join tableversioncell tvc on tvc.tablevid = tv.tablevid
)
, referenceMatchedCells as (
    select orfo.operationvid, orfo.nodeid, cft.tablevid, cft.referenceDate, cft.modulevid, cft.domain, cft.entityid
    from operandReferenceFromOperations orfo
    inner join cellsFromTablesImported cft on orfo.cellid = cft.cellid
)
, operationVersionByTablesAndContext as (
    select rmc.operationvid, rmc.tablevid, referenceDate, modulevid, domain, entityid
    from referenceMatchedCells rmc
    group by rmc.operationvid, rmc.tablevid, referenceDate, modulevid, domain, entityid
)
, operationsExpected as (
    select count(operationvid) nRegrasEsperadas, referenceDate, modulevid, domain, entityid from (
        select otc.operationvid, referenceDate, modulevid, domain, entityid
        from operationVersionByTablesAndContext otc 
        group by otc.operationvid, referenceDate, modulevid, domain, entityid
    ) group by referenceDate, modulevid, domain, entityid
) 
, tablesValidated as (
    select io.modulevid, io.referenceDate, io.domain, io.entityid, vt.validationtableid, vt.tablevid, io.ioid, io.inittimestamp
    from iosValidation io
    inner join out_validationtable vt on io.ioid = vt.ioid
    where vt.stateid = :typeStateOk
)
, maxTablesValidated as (
    select max(tv.validationtableid) valTableId
    from tablesValidated tv
    group by tv.modulevid, tv.referenceDate, tv.domain, tv.entityid, tv.tablevid
)
, tablesValidatedFinalInfo as (
    select tv.*
    from maxTablesValidated rtv
    inner join tablesValidated tv on rtv.valTableId = tv.validationtableid
)
, tablesValidatedBasedOnImport as (
    select itc.*, tvf.validationtableid
    from importedTablesWithoutDeleted itc 
    inner join tablesValidatedFinalInfo tvf on itc.tablevid = tvf.tablevid
)
,validationResults as (
    select results.*, vr.stateid from (
        select max(vr.validationresultid) validationresultid, operationvid, modulevid, referenceDate, domain, entityId
        from tablesValidatedBasedOnImport tv
        inner join out_validationtableresult vtr on tv.validationtableid = vtr.validationtableid
        inner join out_validationresult vr on vtr.validationresultid = vr.validationresultid
        group by operationvid, modulevid, referenceDate, domain, entityId
    ) results
    inner join out_validationresult vr on results.validationresultid = vr.validationresultid
)
,resultCounts AS (
    SELECT
        vr.referenceDate,
        vr.modulevid,
        vr.entityId,
        vr.domain,
        COUNT(*) AS Total,
        SUM(CASE WHEN stateid = :stateRuleOk THEN 1 ELSE 0 END) AS OKS,
        SUM(CASE WHEN stateid = :stateRuleDNRR OR stateid = :stateRuleDNRRPrequisite THEN 1 ELSE 0 END) AS DNRR,
        SUM(CASE WHEN (stateid = :stateRuleNotOk or stateid = :stateRuleOkWithNotOk) and (ovm.severity = :warningSeverity) THEN 1 ELSE 0 END) AS Warnings,
        SUM(CASE WHEN (stateid = :stateRuleNotOk or stateid = :stateRuleOkWithNotOk) and (ovm.severity = :errorSeverity) THEN 1 ELSE 0 END) AS Errors,
        SUM(CASE WHEN stateid = :stateProcessNotOk THEN 1 ELSE 0 END) AS ProcessNotOk
    FROM validationResults vr
    left join operationVersionByModule ovm on vr.operationvid = ovm.operationvid and vr.modulevid = ovm.modulevid
    where vr.operationvid is not null
    GROUP BY vr.referenceDate, vr.modulevid, vr.entityId, vr.domain
), finalResults as (
    select 
        vc.entityid AS Entity,
        vc.domain as Domain,
        vc.modulevid AS Module,
        vc.referenceDate as RefDate, 
        mim.anyMandatoryToImport as MandatoryReportImported, 
        COALESCE(rc.OKS, 0) AS nOKS, 
        COALESCE(rc.DNRR, 0) AS nDNRR, 
        COALESCE(rc.Warnings, 0) AS nWarnings, 
        COALESCE(rc.Errors, 0) AS nErrors, 
        COALESCE(rc.ProcessNotOk, 0) AS nProcessNotOk, 
        COALESCE(rc.Total, 0) AS nTotal,
        COALESCE(oe.nRegrasEsperadas, 0) AS nRegrasEsperadas 
    from validationContext vc 
    left join hasAnyMandatoryToImport mim on mim.modulevid = vc.modulevid 
                                            and mim.entityid = vc.entityid
                                            and mim.domain = vc.domain
                                            and mim.referencedate = vc.referencedate
    left join operationsExpected oe on oe.modulevid = vc.modulevid 
                                       and oe.entityid = vc.entityid
                                       and oe.domain = vc.domain
                                       and oe.referencedate = vc.referencedate
    left join resultCounts rc on rc.modulevid = vc.modulevid 
                                 and rc.entityid = vc.entityid
                                 and rc.domain = vc.domain
                                 and rc.referencedate = vc.referencedate
    inner join moduleversion mv on vc.modulevid = mv.modulevid
    inner join conf_entities ce on vc.entityid = ce.entityid 
)
select 
    fr.Entity,
    fr.domain,
    fr.Module,
    fr.refDate,
    fr.MandatoryReportImported,
    fr.noks,
    fr.ndnrr,
    fr.nwarnings,
    fr.nerrors,
    fr.nprocessnotok,
    fr.ntotal,
    fr.nregrasesperadas,
    CURRENT_TIMESTAMP
from finalResults fr