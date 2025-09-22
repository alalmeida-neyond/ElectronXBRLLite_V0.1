with monetaryVariables as (
    select vv.* from tableversioncell tvc
    inner join variableversion vv on vv.variablevid = tvc.variablevid
    inner join property p on p.propertyid = vv.propertyid
    where tvc.tablevid = :tableVID and p.datatypeid = :datatypeID
), 
nonDefaultMoneratyVariables as (
    select mv.variablevid from monetaryVariables mv
    inner join contextcomposition cc on cc.contextid = mv.contextid
    where cc.itemid = :itemID
)
select * from nonDefaultMoneratyVariables