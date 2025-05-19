with operationVersionByModule as (
    select ov.*, os.isactive, os.fromsubmissiondate
    from moduleversion mv
    inner join operationscopecomposition osc on osc.modulevid = mv.modulevid
    inner join operationscope os on os.operationscopeid = osc.operationscopeid
    inner join operationversion ov on ov.operationvid = os.operationvid
    where mv.modulevid = :moduleVId
        and os.isactive = 1
        and os.fromsubmissiondate <= strftime(:format, :refdate)
)
--select * from operationVersionByModule;

, operandReferenceFromOperations as (
    select ovm.operationvid, opn.nodeid, opr.variableid, oprl.cellid, oprl."Table"
    from operationVersionByModule ovm 
    left join operationnode opn on ovm.operationvid = opn.operationvid
    left join operandreference opr on opn.nodeid = opr.nodeid
    inner join operandreferencelocation oprl on opr.operandreferenceid = oprl.operandreferenceid
    
)
--select * from operandReferenceFromOperations;

, cellsFromTables as (
    select tv.tablevid, tvc.cellcode, tvc.cellid
    from moduleversioncomposition mvc
    inner join tableversion tv on mvc.tableid = tv.tableid and mvc.tablevid = tv.tablevid
    inner join tableversioncell tvc on tvc.tablevid = tv.tablevid
    where tv.tablevid = :tableVId and mvc.modulevid = :moduleVId
)
--select * from cellsFromTables;

, referenceMatchedCells as (
    select orfo.operationvid, orfo.nodeid, cft.cellcode, cft.tablevid
    from operandReferenceFromOperations orfo
    inner join cellsFromTables cft on orfo.cellid = cft.cellid
)
--select * from referenceMatchedCells;

, operationMatchedTable as (
    select rmc.operationvid, rmc.tablevid
    from referenceMatchedCells rmc
    group by rmc.operationvid, rmc.tablevid
)
--select * from operationMatchedTable;

, nodes as (
    select opn.*, omt.tablevid
    from operationMatchedTable omt 
    inner join operationnode opn on omt.operationvid = opn.operationvid
)   
--select * from nodes;

, hierarquia (tablevid, operationvid, nodeid, parentNodeid, nodeLevel) as(
    select tablevid, operationvid, nodeid, parentNodeid, 1 as nodeLevel
    from nodes 
    where parentNodeid is null
    union ALL
    select t.tablevid, t.operationvid, t.nodeid, t.parentNodeid, h.nodeLevel+1 
    from nodes t 
    inner join hierarquia h on t.parentNodeid = h.nodeid and t.operationvid = h.operationvid
)
--select * from hierarquia order by operationvid, nodelevel;
, arvore as (
    select op.*, t.nodeLevel
    from hierarquia t
    left join operationnode op on op.nodeid = t.nodeid
)
select * from arvore ar