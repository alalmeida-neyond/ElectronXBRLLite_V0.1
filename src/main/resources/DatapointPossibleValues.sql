with tableColumns as (
    select tv.tableid, tv.tablevid, tvh.headerid, tvh.headervid
    from tableversion tv
    inner join tableversionheader tvh on tvh.tablevid = tv.tablevid
    where tv.tablevid = :tablevid
)
, keyColumns as (
    select tc.tablevid, tc.tableid, hv.headervid, hv.headerid, hv.code, h.direction, hv.subcategoryvid, ic.code "xbrlHeader"
    from tableColumns tc
    inner join headerversion hv on hv.headerid = tc.headerid and hv.headervid = tc.headervid
    inner join header h on h.tableid = tc.tableid and h.headerid = tc.headerid
    left join itemcategory ic on hv.propertyID = ic.itemid
    left join release sr on sr.releaseid = ic.startreleaseid
    left join release er on er.releaseid = ic.endreleaseid
    where h.direction = :direction 
        and (hv.code = :reportCoordinates or :reportCoordinates = '') 
        and ((sr."Date" <= :referenceDate and (er."Date" >= :referenceDate or er.releaseid is null)) or (sr.releaseid is null and er.releaseid is null))
)
--select * from keyColumns;
, possibleValues as (
    select kc."xbrlHeader" as xbrlHeader, kc.code "HeaderCode", ic.code "ValueCode" , ic.signature as signature, i.name as name
    from keyColumns kc
    left join subcategoryitem sci on kc.subcategoryvid = sci.subcategoryvid
    left join item i on sci.itemid = i.itemid
    left join itemcategory ic on sci.itemid = ic.itemid
)

select * 
from possibleValues pv