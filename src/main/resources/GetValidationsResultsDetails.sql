WITH lastIo AS (
    SELECT io.ioid
    FROM io
    WHERE io.actionid = 2 and io.ioid= :ioId
    
),
ruleDetailIo AS (
    SELECT io.ioid, io.modulevid, io.entityid, io.domain, io.referenceDate
    FROM out_validationtableresult vtr
    INNER JOIN out_validationtable vt ON vt.validationtableid = vtr.validationtableid
    INNER JOIN io io ON io.ioid = vt.ioid
    INNER JOIN lastIo lio ON io.ioid = lio.ioid
),
ios AS (
    SELECT * FROM ruleDetailIo
),
inputs AS (
    SELECT io.referencedate, mv.code AS module, ce.bdpid AS entity, io.domain
    FROM ios io
    INNER JOIN moduleversion mv ON mv.modulevid = io.modulevid
    INNER JOIN conf_entities ce ON ce.entityid = io.entityid
    GROUP BY io.referencedate, mv.code, ce.bdpid, io.domain
),
tablesValidated AS (
    SELECT vt.tablevid, tv.code, MAX(validationTableId) AS validationTableId
    FROM ios io
    INNER JOIN out_validationtable vt ON io.ioid = vt.ioid
    INNER JOIN tableversion tv ON tv.tablevid = vt.tablevid
    GROUP BY vt.tablevid, tv.code
),
maxValidationResultPerOperation AS (
    SELECT MAX(vr.validationresultid) AS validationresultid, operationvid
    FROM tablesValidated tv
    INNER JOIN out_validationtableresult vtr ON tv.validationTableId = vtr.validationTableId
    INNER JOIN out_validationresult vr ON vtr.validationresultid = vr.validationresultid
    WHERE operationvid IS NOT NULL
    GROUP BY operationvid
),
operationsWithSeverity AS (
    SELECT maxV.validationresultid, maxV.operationvid, MAX(os.severity) AS severity
    FROM maxValidationResultPerOperation maxV
    LEFT JOIN operationVersion opv ON opv.operationVid = maxV.operationVid
    LEFT JOIN operation op ON op.operationId = opv.operationId
    LEFT JOIN operationScope os ON os.operationvid = opv.operationvid
    LEFT JOIN operationScopecomposition osc ON osc.operationscopeid = os.operationscopeid
    GROUP BY maxV.validationresultid, maxV.operationvid
),
resultsPerOperation AS (
    SELECT owv.validationresultid, owv.operationvid, op.code AS regraCode, opv.expression AS regra, owv.severity, 'EBA' AS source
    FROM operationsWithSeverity owv
    LEFT JOIN operationVersion opv ON opv.operationVid = owv.operationVid
    LEFT JOIN operation op ON op.operationId = opv.operationId
),
validationResults AS (
    SELECT inputs.module, inputs.entity, inputs.domain, inputs.referenceDate, mvresult.*
    FROM resultsPerOperation mvresult
    CROSS JOIN inputs
),
resultsDetailsRunnedRules AS (
    SELECT rv.module, rv.entity, rv.domain, 
           STRFTIME('%Y-%m-%d', rv.referenceDate) AS referenceDate,
           rv.regraCode, rv.regra, rv.severity, 'EBA' AS origem,
           vrd.domain AS regraDomain, vrd.expression AS regraExecutada, 
           sd.description AS resultado,
           datetime(vrd.timestamp / 1000.0, 'unixepoch') AS dataProcessamento,
           COALESCE(CAST(vrd.difference AS TEXT), '-') AS difference,
           vrd.usedmargin as usedMargin
    FROM out_validationresultdetails vrd
    INNER JOIN validationResults rv ON vrd.validationresultid = rv.validationresultid
    INNER JOIN io_state sd ON vrd.stateid = sd.io_stateid
),
resultsDetailsNotRunnedRules AS (
    SELECT rv.module, rv.entity, rv.domain, 
           STRFTIME('%Y-%m-%d', rv.referenceDate) AS referenceDate,
           rv.regraCode, rv.regra, rv.severity, 'EBA' AS origem,
           NULL AS regraDomain, NULL AS regraExecutada, 
           sr.description AS resultado,
           NULL AS dataProcessamento, NULL AS difference, '0' AS usedMargin
    FROM validationResults rv
    INNER JOIN out_validationresult vr ON vr.validationresultid = rv.validationresultid
    LEFT JOIN out_validationresultdetails vrd ON vrd.validationresultid = vr.validationresultid
    INNER JOIN io_state sr ON vr.stateid = sr.io_stateid
    WHERE vrd.validationresultid IS NULL
)
SELECT * FROM (
    SELECT * FROM resultsDetailsRunnedRules
    UNION ALL
    SELECT * FROM resultsDetailsNotRunnedRules
);
