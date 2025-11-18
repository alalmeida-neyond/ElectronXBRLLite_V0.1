with ioValidation as (
    select io.*
    from DPM_OD.io io
    where ioid = ?ioid
)
, ioValidations as (
    select io.*
    from DPM_OD.io io
    inner join ioValidation on ioValidation.referencedate = io.referencedate
        and ioValidation.entityid = io.entityid 
        and ioValidation.domain = io.domain
        and ioValidation.modulevid = io.modulevid 
        and io.ioid <= ioValidation.ioid 
    inner join DPM_OD.io_state ioe on ioe.io_stateid = io.io_stateid
    where io.actionid = ?actionValidateId and ioe.io_typestateid = ?typeStateOk
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
    inner join DPM_OD.out_validationtable vt on io.ioid = vt.ioid
    where vt.stateid = ?stateOk
)
, validationResults as (
    select mv.modulevid, io.referenceDate as refDate, mv.code as module, ce.bdpid as entity, io.domain, tv.code as relatorio, results.* 
    from (
        select max(vr.validationresultid) validationresultid, operationvid
        from tablesValidated tv
        inner join DPM_OD.out_validationtableresult vtr on tv.validationtableid = vtr.validationtableid
        inner join DPM_OD.out_validationresult vr on vtr.validationresultid = vr.validationresultid
        group by operationvid
    ) results
    inner join DPM_OD.out_validationtableresult vtr on vtr.validationresultid = results.validationresultid
    inner join DPM_OD.out_validationtable vt on vt.validationtableid = vtr.validationtableid
    inner join DPM_OD.io io on io.ioid = vt.ioid
    inner join moduleversion mv on mv.modulevid = io.modulevid
    inner join tableversion tv on tv.tablevid = vt.tablevid
    inner join DPM_OD.conf_entities ce on ce.entityid = io.entityid
)
, resultsDetailsRunnedRules as (
    select to_char(trunc(rv.refDate), 'yyyy-mm-dd') referenceDate, rv.module module, rv.entity, rv.domain, rv.relatorio mapa, op.code regraCode, severity,
    vrd.domain regraDomain, opv.expression as regra, vrd.expression regraExecutada, 'EBA' as origem,
    sd.description as resultado, to_char(vrd.timestamp, 'yyyy-MM-dd HH24?MI?SS') dataProcessamento, coalesce(TO_CHAR(vrd.difference),'-') as difference,
    case when vrd.usedmargin = '1' then 'TRUE' else 'FALSE' end as usedmargin
    from validationResults rv
    inner join DPM_OD.out_validationresult vr on vr.validationresultid = rv.validationresultid
    inner join DPM_OD.out_validationresultdetails vrd on vrd.validationresultid = vr.validationresultid
    inner join DPM_OD.io_state sd on vrd.stateid = sd.io_stateid
    inner join operationVersion opv on opv.operationVid = vr.operationVid
    inner join operation op on op.operationId = opv.operationId
    inner join operationScope os on os.operationvid = opv.operationvid
    inner join operationScopecomposition osc on osc.operationscopeid = os.operationscopeid and osc.modulevid = rv.modulevid
)
, resultsDetailsCommonDatapointRules as (
    SELECT to_char(trunc(rv.refDate), 'yyyy-mm-dd') referenceDate, rv.module module, rv.entity, rv.domain, rv.relatorio mapa, 'Common Datapoint' regraCode, 'Error',
    vrd.domain regraDomain, to_clob('Common Datapoint') as regra, vrd.expression regraExecutada, '-' as origem,
    sd.description as resultado, to_char(vrd.timestamp, 'yyyy-MM-dd HH24?MI?SS') dataProcessamento, coalesce(TO_CHAR(vrd.difference),'-') as difference,
    'FALSE' as usedmargin
    from validationResults rv
    inner join DPM_OD.out_validationresult vr on vr.validationresultid = rv.validationresultid
    inner join DPM_OD.out_validationresultdetails vrd on vrd.validationresultid = vr.validationresultid
    inner join DPM_OD.io_state sd on vrd.stateid = sd.io_stateid
    where vr.operationVid is null
)
, resultsDetailsNotRunnedRules as (
    select
        to_char(trunc(rv.refDate), 'yyyy-mm-dd') referenceDate, rv.module module, rv.entity, rv.domain, rv.relatorio mapa, op.code regraCode, severity,
        null regraDomain, opv.expression as regra, null regraExecutada, 'EBA' as origem,
        sr.description as resultado, null as dataProcessamento, null as difference, to_char('FALSE') as usedmargin
    from validationResults rv
    inner join DPM_OD.out_validationresult vr on vr.validationresultid = rv.validationresultid
    left join DPM_OD.out_validationresultdetails vrd on vrd.validationresultid = vr.validationresultid
    inner join DPM_OD.io_state sr on vr.stateid = sr.io_stateid
    inner join operationVersion opv on opv.operationVid = vr.operationVid
    inner join operation op on op.operationId = opv.operationId
    inner join operationScope os on os.operationvid = opv.operationvid
    inner join operationScopecomposition osc on osc.operationscopeid = os.operationscopeid and osc.modulevid = rv.modulevid
    where vrd.validationresultid is null 
)
select * from resultsDetailsRunnedRules
union all
select * from resultsDetailsNotRunnedRules
union all
Select * from resultsDetailsCommonDatapointRules 