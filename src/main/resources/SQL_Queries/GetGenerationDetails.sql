with tablesGenerated as (
    select mv.code module, ce.bdpid entity, io.domain, to_char(trunc(referenceDate), 'yyyy-mm-dd') referenceDate, generationInfo.ioid, genlog.log_description as description, to_char(genlog.timestampcreated, 'yyyy-MM-dd HH24:MI:SS') timestamp
    from io ioV
    inner join DPM_ED.out_xbrlgenerated generationInfo on generationInfo.ioid = ioV.ioid
    inner join DPM_ED.generatelog genlog on genlog.xbrl_id = generationInfo.xbrl_id
    inner join moduleversion mv on mv.modulevid = ioV.modulevid
    inner join DPM_ED.conf_entities ce on ce.entityid = ioV.entityId
	where ioV.ioid = ?ioid
    order by genlog.timestampcreated desc
)
select code, entity, domain, referenceDate, description, timestamp
from tablesGenerated