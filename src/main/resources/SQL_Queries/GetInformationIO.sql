with IOsPerModule as (
 select ModuleVID, Domain, EntityID, ReferenceDate 
 from DPM_OD.io
 where actionID != ?ignoreActionID --6
 group by ModuleVID, Domain, EntityID, ReferenceDate 
),
importIOsWithHasLog as(
 select io.ioid, ModuleVID, Domain, EntityID, ReferenceDate, IO_STATEID, CASE WHEN count(li.ioid) > 0 THEN 1 ELSE 0 END AS hasLogs
 from  DPM_OD.io io
 left join DPM_OD.LOG_IMPORTPROCESS li on io.ioid = li.ioid
 where io.ACTIONID = ?importActionID -- 1
 group by io.ioid, ModuleVID, Domain, EntityID, ReferenceDate, IO_STATEID
),
validationIOsWithHasLog as(
 select io.ioid, ModuleVID, Domain, EntityID, ReferenceDate, IO_STATEID, CASE WHEN count(vtr.VALIDATIONRESULTID) > 0 THEN 1 ELSE 0 END AS hasLogs
 from  DPM_OD.io io
 left join DPM_OD.OUT_VALIDATIONTABLE vt on vt.ioid = io.ioid
 left join DPM_OD.OUT_VALIDATIONTABLERESULT vtr on vtr.VALIDATIONTABLEID = vt.VALIDATIONTABLEID
 where io.ACTIONID = ?validationActionID -- and vtr.VALIDATIONTABLERESULTID is not null -- 2 
 group by io.ioid, ModuleVID, Domain, EntityID, ReferenceDate, IO_STATEID
),
generationIOsWithHasLog as(
 select io.ioid, io.ModuleVID, io.Domain, io.EntityID, io.ReferenceDate, IO_STATEID, CASE WHEN count(gl.XBRL_ID) > 0 THEN 1 ELSE 0 END AS hasLogs
 from  DPM_OD.io io
 left join DPM_OD.OUT_XBRLGENERATED oxbrl on  oxbrl.IOID = io.ioid
 left join DPM_OD.GENERATELOG gl ON gl.XBRL_ID = oxbrl.XBRL_ID
 where io.ACTIONID = ?generationActionID -- 3
 group by io.IOID, io.ModuleVID, io.Domain, io.EntityID, io.ReferenceDate, IO_STATEID
),
iosByReport as (
 select iom.*, 
  ioi.IOID as importIOID,  
  ioi.IO_StateID as importStateID,  
  ioi.hasLogs as importHasLogs, 
  iov.IOID as validationIOID, 
  iov.IO_StateID as validationStateID, 
  iov.hasLogs as validationHasLogs, 
  iog.IOID as generationIOID, 
  iog.IO_StateID as generationStateID, 
  iog.hasLogs as generationHasLogs 
 from IOsPerModule iom
 left join importIOsWithHasLog ioi on 
  ioi.ModuleVID = iom.ModuleVID and
  ioi.Domain = iom.Domain and
  ioi.EntityID = iom.EntityID and
  ioi.ReferenceDate = iom.ReferenceDate
 left join validationIOsWithHasLog iov on 
  iov.ModuleVID = iom.ModuleVID and
  iov.Domain = iom.Domain and
  iov.EntityID = iom.EntityID and
  iov.ReferenceDate = iom.ReferenceDate
 left join generationIOsWithHasLog iog on 
  iog.ModuleVID = iom.ModuleVID and
  iog.Domain = iom.Domain and
  iog.EntityID = iom.EntityID and
  iog.ReferenceDate = iom.ReferenceDate
)
Select 
 mv.code as module, 
 ce.leicode as entity, 
 io.Domain,
 io.ReferenceDate as referenceDate,
 io.importIOID,  
 io.importStateID,  
 io.importHasLogs, 
 io.validationIOID, 
 io.validationStateID, 
 io.validationHasLogs, 
 io.generationIOID, 
 io.generationStateID, 
 io.generationHasLogs 
from iosByReport io 
inner join DPM_MD.ModuleVersion mv on mv.modulevid = io.modulevid 
inner join DPM_OD.CONF_ENTITIES ce on ce.entityid = io.entityid
 