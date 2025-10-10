UPDATE io 
SET  actionid = :actionIgnoreID 
WHERE	ModuleVID = :ModuleVID 
    and Domain = :Domain 
    and EntityID = :EntityID 
    and ReferenceDate = :ReferenceDate