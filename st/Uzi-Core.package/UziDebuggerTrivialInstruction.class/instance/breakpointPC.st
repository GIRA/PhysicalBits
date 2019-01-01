accessing
breakpointPC
	| branch |
	branch := self branchInstruction.
	^ self = branch
		ifTrue: [super breakpointPC]
		ifFalse: [branch breakpointPC]