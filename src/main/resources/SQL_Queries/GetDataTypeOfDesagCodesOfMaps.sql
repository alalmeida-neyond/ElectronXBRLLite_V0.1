with tablesFromModule as (
    select tv.tablevid, t.hasopensheets, tv.code
    from moduleversioncomposition mvc
    inner join tableversion tv on mvc.tablevid = tv.tablevid
    inner join "TABLE" t on tv.tableid = t.tableid
    where mvc.modulevid = :moduleVID
)

, tablesWithDesagCodeNormal as (
    select tfm.tablevid, :desagregationCodeTypeNormal as DesagCodeType
    from tablesFromModule tfm
    where tfm.hasopensheets = :trueNumber
)

, dataTypeOfDesagCode as (
    select t.tablevid, ic.code, dt.datatypeid
    from tablesWithDesagCodeNormal t
    inner join tableversionheader tvh on t.tablevid = tvh.tablevid
    inner join headerversion hv on tvh.headervid = hv.headervid
    inner join header h on hv.headerid = h.headerid
    inner join itemcategory ic on hv.propertyID = ic.itemid
    left join release sr on sr.releaseid = ic.startreleaseid
    left join release er on er.releaseid = ic.endreleaseid
    inner join variableversion vv on hv.keyvariablevid = vv.variablevid
    inner join property p on vv.propertyid = p.propertyid
    inner join datatype dt on p.datatypeid = dt.datatypeid
    where 1=1
        and h.direction = :directionZ
        and ((sr."Date" <= strftime(:formatDate, :referenceDate) and (er."Date" >= strftime(:formatDate, :referenceDate) or er.releaseid is null)) or (sr.releaseid is null and er.releaseid is null))
    order by t.tablevid, hv.code
)
select * from dataTypeOfDesagCode