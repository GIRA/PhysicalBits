accessing
children
	^ { self counterDeclaration.
		self start. self stop. self step. 
		self body }