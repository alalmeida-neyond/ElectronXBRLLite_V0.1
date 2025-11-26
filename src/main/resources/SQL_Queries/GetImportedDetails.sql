with tablesValidated as (
    select mv.code code, ce.leicode entity, ioTable.domain, to_char(trunc(ioTable.referenceDate), ?format) referenceDate, importProcess.description, to_char(importProcess.timestamp, 'yyyy-MM-dd HH24:MI:SS') timestamp
    from DPM_OD.io ioTable 
    inner join DPM_OD.log_importprocess importProcess on importProcess.ioid = ioTable.ioid
    inner join DPM_MD.moduleversion mv on mv.modulevid = ioTable.modulevid
    inner join DPM_OD.conf_entities ce on ce.entityid = ioTable.entityId
	where ioTable.ioid = ?ioid
    order by importProcess.timestamp desc
)
select * from tablesValidated