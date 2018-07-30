requests
processGET: request	
	| id wait |
	id := (request fields at: #id ifAbsent: nil).
	wait := (request fields at: #wait ifAbsent: 0) asInteger.
	(id notNil and: [wait > 0 and: [changed not]])
		ifTrue: [(self semaphoreAt: id) waitTimeoutSeconds: wait].
	changed := false.
	^ super processGET: request