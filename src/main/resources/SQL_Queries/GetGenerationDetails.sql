with tablesGenerated as (
    select mv.code, ce.bdpid entity, ioV.domain, strftime(:format, ioV.referenceDate) referenceDate, generationInfo.ioid, genlog.log_description as description, strftime(:dateTimeFormat, genlog.timestampcreated / 1000, 'unixepoch') timestamp
    from io ioV
    inner join out_xbrlgenerated generationInfo on generationInfo.ioid = ioV.ioid
    inner join generatelog genlog on genlog.xbrl_id = generationInfo.xbrl_id
    inner join moduleversion mv on mv.modulevid = ioV.modulevid
    inner join conf_entities ce on ce.entityid = ioV.entityId
	where ioV.ioid = :ioid
    order by genlog.timestampcreated desc
)
select code, entity, domain, referenceDate, description, timestamp
from tablesGenerated