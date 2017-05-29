accessing
protocol
	"Use the current protocol, but if it was deleted try to find a new one"
	^ (protocol notNil and: [protocol isInWorld])
		ifTrue: [protocol]
		ifFalse: [protocol := self findNewProtocol]