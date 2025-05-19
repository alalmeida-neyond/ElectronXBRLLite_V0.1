with importIos as (
    select io.ioid
    from io io 
    where (actionid = :actionImportId  
			and (io.referencedate = strftime(:format, :referenceDate) OR :referenceDate IS NULL)  
			and (io.entityid = :entityID OR :entityID IS NULL)  
			and (io.domain = :domain OR :domain IS NULL)   
			and (io.modulevid = :moduleVID OR :moduleVID IS NULL)
		) 
)
, tablesValidated as (
    select mv.code code, ce.leicode entity, io.domain, strftime(:format, io.referenceDate) referenceDate, importProcess.description, strftime('%Y-%m-%d %H:%M:%S', importProcess.timestamp) timestamp
    from importIos ioP
    inner join io io on ioP.ioid = io.ioid
    inner join log_importprocess importProcess on importProcess.ioid = io.ioid
    inner join moduleversion mv on mv.modulevid = io.modulevid
    inner join conf_entities ce on ce.entityid = io.entityId
    order by importProcess.timestamp desc
)
select * from tablesValidated
