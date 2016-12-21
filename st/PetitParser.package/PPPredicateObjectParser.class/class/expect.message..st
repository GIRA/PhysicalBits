factory-objects
expect: anObject message: aString
	^ self 
		on: [ :each | each = anObject ] message: aString
		negated: [ :each | each ~= anObject ] message: 'no ' , aString