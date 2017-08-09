accessing
scriptName
	self flag: #Richo. "Remove when removing UziScriptRefNode"
	^ (script isKindOf: UziScriptRefNode) 
		ifTrue: [script name] ifFalse: [script]