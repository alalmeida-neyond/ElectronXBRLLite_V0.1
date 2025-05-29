with ruleDetailIo as (
    select io.ioid, io.modulevid, io.entityid, io.domain, io.referenceDate
    from dpm_ed.out_validationtableresult vtr
    inner join dpm_ed.out_validationtable vt on vt.validationtableid = vtr.validationtableid
    inner join dpm_ed.io io on io.ioid = vt.ioid
    where io.ioid = :ioId
)
, ios as (
    select * from ruleDetailIo
)
--select * from ios;
, inputs as (
    select io.referencedate, mv.code as  module, ce.bdpid as entity, io.domain
    from ios io
    inner join moduleversion mv on mv.modulevid = io.modulevid
    inner join conf_entities ce on ce.entityid = io.entityid
    group by io.referencedate, mv.code, ce.bdpid, io.domain
)
, tablesValidated as (
    select vt.tablevid, tv.code, max(validationTableId) validationTableId
    from ios io
    inner join out_validationtable vt on io.ioid = vt.ioid
    inner join tableversion tv on tv.tablevid = vt.tablevid
    group by vt.tablevid, tv.code
)
, maxValidationResultPerOperation as (
    select max(vr.validationresultid) validationresultid, operationvid
    from tablesValidated tv
    inner join out_validationtableresult vtr on tv.validationtableid = vtr.validationtableid
    inner join out_validationresult vr on vtr.validationresultid = vr.validationresultid
    where operationvid is not null 
    group by operationvid
)
, operationsWithSeverity as (
    select maxV.validationresultid, maxV.operationvid, max(os.severity) severity
    from maxValidationResultPerOperation maxV
    left join operationVersion opv on opv.operationVid = maxV.operationVid
    left join operation op on op.operationId = opv.operationId
    left join operationScope os on os.operationvid = opv.operationvid
    left join operationScopecomposition osc on osc.operationscopeid = os.operationscopeid and osc.modulevid = :moduleVID 
    group by maxV.validationresultid, maxV.operationvid
)
, resultsPerOperation as (
    select owv.validationresultid, owv.operationvid, op.code regraCode, opv.expression as regra, owv.severity severity, 'EBA' as origem
    from operationsWithSeverity owv
    left join operationVersion opv on opv.operationVid = owv.operationVid
    left join operation op on op.operationId = opv.operationId
)
, validationResults as (
    select inputs.module, inputs.entity, inputs.domain, inputs.referenceDate, mvresult.*
    from resultsPerOperation mvresult
    cross join inputs
)
, resultsDetailsRunnedRules as (
    select rv.module, rv.entity, rv.domain, to_char(trunc(rv.referenceDate), 'yyyy-mm-dd') referenceDate, rv.regraCode, rv.regra, rv.severity, 'EBA' as origem, 
        vrd.domain regraDomain, vrd.expression regraExecutada, sd.description as resultado, 
        to_char(vrd.timestamp, 'yyyy-MM-dd HH24:MI:SS') dataProcessamento, coalesce(TO_CHAR(vrd.difference),'-') as difference, 
        vrd.usedmargin
    from out_validationresultdetails vrd
    inner join validationResults rv on vrd.validationresultid = rv.validationresultid
    inner join dpm_ed.io_state sd on vrd.stateid = sd.io_stateid
    where vrd.validationresultid = ?validationResultId or ?validationResultId is null
)
--select * from resultsDetailsRunnedRules;
, resultsDetailsNotRunnedRules as (
    select rv.module, rv.entity, rv.domain, to_char(trunc(rv.referenceDate), 'yyyy-mm-dd') referenceDate, rv.regraCode, rv.regra, rv.severity, 'EBA' as origem, 
        null regraDomain, null regraExecutada, sr.description as resultado, 
        null as dataProcessamento, null as difference, to_char(0) as usedmargin
    from validationResults rv
    inner join out_validationresult vr on vr.validationresultid = rv.validationresultid
    left join out_validationresultdetails vrd on vrd.validationresultid = vr.validationresultid
    inner join io_state sr on vr.stateid = sr.io_stateid
    where vrd.validationresultid is null and :validationResultId is null
)
--select * from resultsDetailsNotRunnedRules;
select * from (
    select * from resultsDetailsRunnedRules
    union all
    select * from resultsDetailsNotRunnedRules
)