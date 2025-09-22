with tableColumns as (
    select tv.tableid, tv.tablevid, tvh.headerid, tvh.headervid
    from tableversion tv
    inner join tableversionheader tvh on tvh.tablevid = tv.tablevid
    where tv.tablevid = :tableVId
)
--select * from tableColumns;
, keyColumns as (
    select ic.code "xbrlHeader",
        CASE 
            WHEN h.direction = 'X' THEN 2
            WHEN h.direction = 'Z' THEN 1
            ELSE -1
        END AS KeyType
    from tableColumns tc
    inner join headerversion hv on hv.headervid = tc.headervid
    inner join header h on h.headerid = tc.headerid
    left join item i on i.itemid = hv.propertyID
    left join itemcategory ic on i.itemid = ic.itemid 
    left join release sr on sr.releaseid = ic.startreleaseid
    left join release er on er.releaseid = ic.endreleaseid
    where h.iskey = 1 and i.isactive = 1
        and (sr."Date" <= strftime(:format, :referenceDate) and (er."Date" >= strftime(:format, :referenceDate) or er.releaseid is null))
)

select *
from keyColumns pv