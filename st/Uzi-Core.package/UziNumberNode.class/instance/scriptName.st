accessing
scriptName 
	UziScriptRefNode. "Added this reference so that I don't miss this method when cleaning up"
	self flag: #Richo. "Delete this when we remove the UziScriptRefNode"
	^ self value asString