requests
processGET: request	
	| id wait changed sema |
	id := (request fields at: #id ifAbsent: nil).
	wait := (request fields at: #wait ifAbsent: 0) asInteger.
	changed := self changedAt: id.
	sema := self semaphoreAt: id.
	(id notNil and: [wait > 0 and: [changed not]])
		ifTrue: [sema waitTimeoutSeconds: wait].
	self changedAt: id put: false.
	^ super processGET: request