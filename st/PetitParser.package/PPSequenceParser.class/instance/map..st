operators-mapping
map: aBlock
	^ aBlock numArgs = self children size
		ifTrue: [ self ==> [ :nodes | aBlock valueWithArguments: nodes ] ]
		ifFalse: [ self error: aBlock numArgs asString , ' arguments expected.' ]