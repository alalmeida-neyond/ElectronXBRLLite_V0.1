select tv.*
from moduleversioncomposition mvc
inner join tableversion tv on mvc.tablevid = tv.tablevid
inner join "TABLE" t on t.tableid = tv.tableid
where 1 = 1
    and mvc.modulevid = ?modulevid
    and t.isabstract = 0
order by tv.code

