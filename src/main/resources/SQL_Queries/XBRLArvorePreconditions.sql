with operationVersionByModule as (
    select distinct ov.preconditionoperationvid
    from moduleversion mv
    inner join operationscopecomposition osc on osc.modulevid = mv.modulevid
    inner join operationscope os on os.operationscopeid = osc.operationscopeid
    inner join operationversion ov on ov.operationvid = os.operationvid
    where mv.modulevid = ?moduleVId
        and os.isactive = 1
        and os.fromsubmissiondate <= TO_DATE(?refdate, ?format)
)
-- select * from operationVersionByModule;

, preconditionsToValidate as (
    select ov.*
    from operationVersionByModule ovm
    inner join operationversion ov on ovm.preconditionoperationvid = ov.operationvid
)
-- select * from preconditionsToValidate;


, nodes as (
    select opn.*
    from preconditionsToValidate ptv
    inner join operationnode opn on ptv.operationvid = opn.operationvid
)   
-- select * from nodes;

, hierarquia (operationvid, nodeid, parentNodeid, nodeLevel) as(
    select operationvid, nodeid, parentNodeid, 1 as nodeLevel
    from nodes 
    where parentNodeid is null
    union ALL
    select t.operationvid, t.nodeid, t.parentNodeid, h.nodeLevel+1 
    from nodes t 
    inner join hierarquia h on t.parentNodeid = h.nodeid and t.operationvid = h.operationvid
)
-- select * from hierarquia order by operationvid, nodelevel;


, arvore as (
    select op.*, t.nodeLevel
    from hierarquia t
    left join operationnode op on op.nodeid = t.nodeid
)
select * from arvore ar