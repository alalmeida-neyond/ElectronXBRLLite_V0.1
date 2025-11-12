with tablesFromModule as (
    select tv.tablevid, t.hasopensheets, tv.code
    from moduleversioncomposition mvc
    inner join tableversion tv on mvc.tablevid = tv.tablevid
    inner join "TABLE" t on tv.tableid = t.tableid
    where mvc.modulevid = ?moduleVID
)

, tablesWithDesagCodeFix as (
    select tfm.tablevid, ?desagregationCodeTypeFix as DesagCodeType
    from tablesFromModule tfm
    inner join tableversionheader tvh on tfm.tablevid = tvh.tablevid
    inner join headerversion hv on tvh.headervid = hv.headervid
    inner join header h on hv.headerid = h.headerid
    where 1=1
        and h.direction = ?directionZ 
        and tfm.hasopensheets = ?falseNumber 
    group by tfm.tablevid
)

, tablesWithDesagCodeNormal as (
    select tfm.tablevid, ?desagregationCodeTypeNormal as DesagCodeType
    from tablesFromModule tfm
    where tfm.hasopensheets = ?trueNumber
)

, allTables as (
    select * from tablesWithDesagCodeNormal
    union all
    select * from tablesWithDesagCodeFix
)
select * from allTables