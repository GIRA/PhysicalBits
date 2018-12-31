accessing
nextStatements
	^ super nextStatements , 
	self pre nextStatements, 
	self post nextStatements