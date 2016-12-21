accessing
at: key put: aRESTResource
	aRESTResource parent ifNotNil: [^ Error signal: 'The resource already has a parent'].
	(children includesKey: key) ifTrue: [^ Error signal: ('A resource already exists with key {1}' format: { key })].
	children at: key put: aRESTResource.
	aRESTResource setParent: self