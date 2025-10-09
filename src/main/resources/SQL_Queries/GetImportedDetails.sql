with tablesValidated as (
    select mv.code code, ce.leicode entity, ioTable.domain, strftime(:format, ioTable.referenceDate) referenceDate, importProcess.description, strftime(:dateTimeFormat, importProcess.timestamp / 1000, 'unixepoch') timestamp
    from io ioTable 
    inner join log_importprocess importProcess on importProcess.ioid = ioTable.ioid
    inner join moduleversion mv on mv.modulevid = ioTable.modulevid
    inner join conf_entities ce on ce.entityid = ioTable.entityId
	where ioTable.ioid = :ioid
    order by importProcess.timestamp desc
)
select * from tablesValidated
