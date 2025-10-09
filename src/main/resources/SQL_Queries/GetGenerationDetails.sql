with tablesGenerated as (
    select mv.code module, ce.bdpid entity, ioV.domain, strftime(:format, ioV.referenceDate) referenceDate, generationInfo.ioid, genlog.log_description as description, strftime('%Y-%m-%d %H:%M:%S',genlog.timestampcreated) timestamp
    from io ioV
    inner join out_xbrlgenerated generationInfo on generationInfo.ioid = ioV.ioid
    inner join generatelog genlog on genlog.xbrl_id = generationInfo.xbrl_id
    inner join moduleversion mv on mv.modulevid = ioV.modulevid
    inner join conf_entities ce on ce.entityid = ioV.entityId
	where ioV.ioid = :ioId
    order by genlog.timestampcreated desc
)
select module, entity, domain, referenceDate, description, timestamp
from tablesGenerated