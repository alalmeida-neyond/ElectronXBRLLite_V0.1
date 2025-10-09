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

, tableWithDesagCodeEnumerated as (
    select t.tablevid, ic.code "XBRLCode", hv.subcategoryvid, hv.code "HeaderCode"
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
        and dt.datatypeid = :dataTypeEnumeration
        and ((sr."Date" <= strftime(:formatDate, :referenceDate) and (er."Date" >= strftime(:formatDate, :referenceDate) or er.releaseid is null)) or (sr.releaseid is null and er.releaseid is null))
    order by t.tablevid, hv.code
)

, possibleValuesForDesagCodeNormal as (
    select tdc.tablevid "TableVID", tdc."XBRLCode" as "XBRLCode", tdc."HeaderCode" "HeaderCode", ic.code "ValueCode" , ic.signature as signature, i.name as name
    from tableWithDesagCodeEnumerated tdc
    left join subcategoryitem sci on tdc.subcategoryvid = sci.subcategoryvid
    left join item i on sci.itemid = i.itemid
    left join itemcategory ic on sci.itemid = ic.itemid
    order by tdc.tablevid, tdc."HeaderCode", ic.code
)

, tablesWithDesagCodeFix as (
    select tfm.tablevid
    from tablesFromModule tfm
    inner join tableversionheader tvh on tfm.tablevid = tvh.tablevid
    inner join headerversion hv on tvh.headervid = hv.headervid
    inner join header h on hv.headerid = h.headerid
    where 1=1
        and h.direction = :directionZ 
        and tfm.hasopensheets = :falseNumber 
    group by tfm.tablevid
)

, possibleValuesForDesagCodeFix as (
    select tdc.tableVID "TableVID", :sheetCode as "XBRLCode", hv.code "HeaderCode", null "ValueCode" , null as signature, null as name 
    from tablesWithDesagCodeFix tdc
    inner join tableversionheader tvh on tdc.tablevid = tvh.tablevid
    inner join headerversion hv on tvh.headervid = hv.headervid
    inner join header h on hv.headerid = h.headerid
    where h.direction = :directionZ 
)

, allPossibleValues as (
    select * from possibleValuesForDesagCodeNormal
    union all
    select * from possibleValuesForDesagCodeFix
)

select * from allPossibleValues order by "TableVID", "HeaderCode", "ValueCode"
