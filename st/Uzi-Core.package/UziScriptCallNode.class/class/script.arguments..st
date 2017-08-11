instance creation
script: scriptName arguments: anArrayOfAssociations
	^ self basicNew 
		initializeWithScript: scriptName
		arguments: anArrayOfAssociations