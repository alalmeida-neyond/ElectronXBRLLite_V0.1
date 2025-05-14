with tableColumns as (
    select tv.tableid, tv.tablevid, tvh.headerid, tvh.headervid
    from tableversion tv
    inner join tableversionheader tvh on tvh.tablevid = tv.tablevid
    where tv.tablevid = :tableVId
)
, keyColumns as (
    select 
        ic.code "xbrlHeader",
        hv.code,
        h.iskey
    from tableColumns tc
    inner join headerversion hv on hv.headervid = tc.headervid
    inner join header h on h.headerid = tc.headerid
    left join item i on i.itemid = hv.propertyID
    left join itemcategory ic on i.itemid = ic.itemid 
    left join release sr on sr.releaseid = ic.startreleaseid
    left join release er on er.releaseid = ic.endreleaseid
    where h.direction = 'X'
        and i.isactive = 1
        and (sr."Date" <= strftime(:format, :referencedate) and (er."Date" >= strftime(:format, :referencedate) or er.releaseid is null))
)
select *
from keyColumns pv
order by pv.code