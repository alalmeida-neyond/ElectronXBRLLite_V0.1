with ioValidation as (
    select io.ioid, io.modulevid, io.entityid, io.domain, io.referenceDate
    from io io
    where io.ioid = :ioid
)
, inputs as (
    select io.referencedate, mv.code as  module, ce.bdpid as entity, io.domain
    from ioValidation io
    inner join moduleversion mv on mv.modulevid = io.modulevid
    inner join conf_entities ce on ce.entityid = io.entityid
    group by io.referencedate, mv.code, ce.bdpid, io.domain
)
, tablesValidated as (
    select vt.tablevid, tv.code, max(validationTableId) validationTableId
    from ioValidation io
    inner join out_validationtable vt on io.ioid = vt.ioid
    inner join tableversion tv on tv.tablevid = vt.tablevid
    where vt.stateid = 1
    group by vt.tablevid, tv.code
)
, maxValidationResultPerOperation as (
    select max(vr.validationresultid) validationresultid, operationvid, tv.code
    from tablesValidated tv
    inner join out_validationtableresult vtr on tv.validationtableid = vtr.validationtableid
    inner join out_validationresult vr on vtr.validationresultid = vr.validationresultid
    where operationvid is not null 
    group by operationvid, tv.code
)
,maxValidationResultForCommonDatapoint as (
    select max(vr.validationresultid) validationresultid, operationvid, 'Common Datapoint' as regraCode, 'Common Datapoint' as regra, 'Error' as severity, '-' as origem, tv.code
    from tablesValidated tv
    inner join out_validationtableresult vtr on tv.validationtableid = vtr.validationtableid
    inner join out_validationresult vr on vtr.validationresultid = vr.validationresultid
    where operationvid is null 
    group by operationvid, tv.code
)									   
, operationsWithSeverity as (
    select maxV.validationresultid, maxV.operationvid, max(os.severity) severity, maxV.code
    from maxValidationResultPerOperation maxV
    left join operationVersion opv on opv.operationVid = maxV.operationVid
    left join operation op on op.operationId = opv.operationId
    left join operationScope os on os.operationvid = opv.operationvid
    left join operationScopecomposition osc on osc.operationscopeid = os.operationscopeid and osc.modulevid = :moduleVID 
    group by maxV.validationresultid, maxV.operationvid, maxV.code
)
, resultsPerOperation as (
    select owv.validationresultid, owv.operationvid, op.code regraCode, opv.expression as regra, owv.severity severity, 'EBA' as origem, owv.code
    from operationsWithSeverity owv
    left join operationVersion opv on opv.operationVid = owv.operationVid
    left join operation op on op.operationId = opv.operationId 
)
, validationResults as (
    select inputs.module, inputs.entity, inputs.domain, inputs.referenceDate, mvresult.*
    from resultsPerOperation mvresult
    cross join inputs

	union all

    select inputs.module, inputs.entity, inputs.domain, inputs.referenceDate, mvresult.*
    from maxValidationResultForCommonDatapoint mvresult
    cross join inputs
)
, resultsDetailsRunnedRules as (
    select rv.module, rv.entity, rv.code as report, rv.domain, rv.referenceDate as referenceDate, rv.regraCode, rv.regra, rv.severity, 'EBA' as origem, 
        vrd.domain regraDomain, vrd.expression regraExecutada, sd.description as resultado,
        strftime('%Y-%m-%d %H:%M:%S', vrd.timestamp / 1000, 'unixepoch','localtime') as dataProcessamento, coalesce(CAST(vrd.difference as TEXT),'-') as difference, 
        case when vrd.usedmargin = '1' then 'TRUE' else 'FALSE' end as usedmargin
    from out_validationresultdetails vrd
    inner join validationResults rv on vrd.validationresultid = rv.validationresultid
    inner join io_state sd on vrd.stateid = sd.io_stateid
)
--select * from resultsDetailsRunnedRules;
, resultsDetailsNotRunnedRules as (
    select rv.module, rv.entity, rv.code as report, rv.domain, rv.referenceDate as referenceDate, rv.regraCode, rv.regra, rv.severity, 'EBA' as origem, 
        null regraDomain, null regraExecutada, sr.description as resultado, 
        coalesce(CAST(vrd.difference as TEXT),'-') as dataProcessamento, coalesce(CAST(vrd.difference as TEXT),'-') as difference, CAST('FALSE' AS TEXT) as usedmargin
    from validationResults rv
    inner join out_validationresult vr on vr.validationresultid = rv.validationresultid
    left join out_validationresultdetails vrd on vrd.validationresultid = vr.validationresultid
    inner join io_state sr on vr.stateid = sr.io_stateid
	where vrd.validationresultid is null
)
--select * from resultsDetailsNotRunnedRules;
select * from (
    select * from resultsDetailsRunnedRules
    union all
    select * from resultsDetailsNotRunnedRules
)