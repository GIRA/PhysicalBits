instance creation
script: anUziScriptRefNode arguments: anArrayOfAssociations
	^ self basicNew 
		initializeWithScript: anUziScriptRefNode
		arguments: anArrayOfAssociations