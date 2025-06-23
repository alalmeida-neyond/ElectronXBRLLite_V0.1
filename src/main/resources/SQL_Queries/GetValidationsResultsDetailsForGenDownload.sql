with ioValidations as (
    select io.*
    from io io
    inner join io_state ioe on ioe.io_stateid = io.io_stateid
    where io.actionid = :actionValidateId and ioe.io_typestateid = :typeStateOk and io.ioid = :ioId
)
, ios as (
    select * from ioValidations
)
, inputs as (
    select io.referencedate, io.modulevid, io.entityid, io.domain
    from ios io
    group by io.referencedate, io.modulevid, io.entityid, io.domain
)

, tablesValidated as (
    select tablevid, validationTableId
    from ios io
    inner join out_validationtable vt on io.ioid = vt.ioid
    where vt.stateid = :stateOk
)
, validationResults as (
    select mv.modulevid, io.referenceDate as refDate, mv.code as module, ce.bdpid as entity, io.domain, tv.code as relatorio, results.* 
    from (
        select max(vr.validationresultid) validationresultid, operationvid
        from tablesValidated tv
        inner join out_validationtableresult vtr on tv.validationtableid = vtr.validationtableid
        inner join out_validationresult vr on vtr.validationresultid = vr.validationresultid
        group by operationvid
    ) results
    inner join out_validationtableresult vtr on vtr.validationresultid = results.validationresultid
    inner join out_validationtable vt on vt.validationtableid = vtr.validationtableid
    inner join io io on io.ioid = vt.ioid
    inner join moduleversion mv on mv.modulevid = io.modulevid
    inner join tableversion tv on tv.tablevid = vt.tablevid
    inner join conf_entities ce on ce.entityid = io.entityid
)
, resultsDetailsRunnedRules as (
    SELECT 
    strftime('%Y-%m-%d', rv.refDate) AS referenceDate,
    rv.module AS module,
    rv.entity,
    rv.domain,
    rv.relatorio AS mapa,
    op.code AS regraCode,
    severity,
    vrd.domain AS regraDomain,
    opv.expression AS regra,
    vrd.expression AS regraExecutada,
    'EBA' AS origem,
    sd.description AS resultado,
    datetime(vrd.timestamp / 1000.0, 'unixepoch') AS dataProcessamento,
    COALESCE(CAST(vrd.difference AS TEXT), '-') AS difference,
    vrd.usedmargin
	FROM validationResults rv
	INNER JOIN out_validationresult vr ON vr.validationresultid = rv.validationresultid
	INNER JOIN out_validationresultdetails vrd ON vrd.validationresultid = vr.validationresultid
	INNER JOIN io_state sd ON vrd.stateid = sd.io_stateid
	INNER JOIN operationVersion opv ON opv.operationVid = vr.operationVid
	INNER JOIN operation op ON op.operationId = opv.operationId
	INNER JOIN operationScope os ON os.operationvid = opv.operationvid
	INNER JOIN operationScopecomposition osc ON osc.operationscopeid = os.operationscopeid 
    AND osc.modulevid = rv.modulevid
)
, resultsDetailsNotRunnedRules as (
    select
        strftime('%Y-%m-%d', rv.refDate) AS referenceDate,
		rv.module AS module,
		rv.entity,
		rv.domain,
		rv.relatorio AS mapa,
		op.code AS regraCode,
		severity,
        null regraDomain, opv.expression as regra, null regraExecutada, 'EBA' as origem,
        sr.description as resultado, null as dataProcessamento, null as difference, '0' as usedmargin
    from validationResults rv
    inner join out_validationresult vr on vr.validationresultid = rv.validationresultid
    left join out_validationresultdetails vrd on vrd.validationresultid = vr.validationresultid
    inner join io_state sr on vr.stateid = sr.io_stateid
    inner join operationVersion opv on opv.operationVid = vr.operationVid
    inner join operation op on op.operationId = opv.operationId
    inner join operationScope os on os.operationvid = opv.operationvid
    inner join operationScopecomposition osc on osc.operationscopeid = os.operationscopeid and osc.modulevid = rv.modulevid
    where vrd.validationresultid is null 
)
select * from resultsDetailsRunnedRules
union all
select * from resultsDetailsNotRunnedRules