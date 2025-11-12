with tablesValidated as (
    select mv.code code, ce.leicode entity, ioTable.domain, to_char(trunc(ioTable.referenceDate), ?format) referenceDate, importProcess.description, to_char(importProcess.timestamp, 'yyyy-MM-dd HH24:MI:SS') timestamp
    from io ioTable 
    inner join DPM_ED.log_importprocess importProcess on importProcess.ioid = ioTable.ioid
    inner join moduleversion mv on mv.modulevid = ioTable.modulevid
    inner join DPM_ED.conf_entities ce on ce.entityid = ioTable.entityId
	where ioTable.ioid = ?ioid
    order by importProcess.timestamp desc
)
select * from tablesValidated