with importedIOs as ( 
    select io.ioid
    from IO io 
    inner join io_state ioe on ioe.io_stateid = io.io_stateid 
    where ioe.io_typestateid = :typeStateOk
    and io.actionId = :actionId
    and (io.referencedate = strftime(:format, :referenceDate) OR :referenceDate IS NULL)
    and (io.domain = :domain OR :domain IS NULL)
    and (io.entityId = :entityId OR :entityId IS NULL)
    and (io.moduleVID = :moduleVID OR :moduleVID IS NULL)
),
importedVariables as (
    select vv.variablevid, propertyid, contextid
    from in_importedtablestemp it
    inner join importedIOs ios on it.ioid = ios.ioid
    inner join in_importedvaluestemp iv on iv.importedtableid = it.importedtableid
    inner join tableversioncell tvc on tvc.tablevid = it.tablevid and tvc.cellid = iv.cellid
    inner join variableversion vv on vv.variablevid = tvc.variablevid
    group by vv.variablevid, propertyid, contextid
),
nonDefaultMoneratyVariables as (
    select distinct vv.variablevid from tableversioncell tvc
    inner join variableversion vv on vv.variablevid = tvc.variablevid
    inner join property p on p.propertyid = vv.propertyid
    inner join contextcomposition cc on cc.contextid = vv.contextid
    where datatypeid = :monetaryDatatypeId 
    and cc.itemid = :itemId
), 
importedDatatypes as(
    select p.datatypeid, 
    case when aux.variablevid is null then '0' else '1' end as hasUnit
    from importedVariables iv
    left join nonDefaultMoneratyVariables aux on aux.variablevid = iv.variablevid
    inner join property p on p.propertyid = iv.propertyid
)

select datatypeid, hasunit from importedDatatypes
group by datatypeid, hasunit