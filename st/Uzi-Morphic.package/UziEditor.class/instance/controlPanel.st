accessing
controlPanel
	"Use the current control panel, but if it was deleted try to find a new one"
	^ (controlPanel notNil and: [controlPanel isInWorld])
		ifTrue: [controlPanel]
		ifFalse: [controlPanel := self findControlPanel]