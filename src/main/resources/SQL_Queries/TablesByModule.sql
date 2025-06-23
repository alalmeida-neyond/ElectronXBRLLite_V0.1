select tv.*, mvc.modulevid
from moduleversioncomposition mvc
inner join tableversion tv on mvc.tableid = tv.tableid and mvc.tablevid = tv.tablevid