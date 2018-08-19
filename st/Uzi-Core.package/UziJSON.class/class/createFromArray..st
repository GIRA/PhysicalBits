decode
createFromArray: anArray
	^ anArray collect: [:each | self createObject: each]