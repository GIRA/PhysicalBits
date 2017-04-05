instance creation
script: anUziScriptRefNode withReturnValue: aBoolean
	^ self basicNew 
		initializeWithScript: anUziScriptRefNode
		acceptReturnValue: aBoolean