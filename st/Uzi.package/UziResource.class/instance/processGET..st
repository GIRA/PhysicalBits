requests
processGET: request	
	| wait |
	wait := (request fields at: #wait ifAbsent: 0) asInteger.
	(wait > 0 and: [changed not])
		ifTrue: [sema waitTimeoutSeconds: wait].
	changed := false.
	^ super processGET: request