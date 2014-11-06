accessing
compile: aString 
	| nodes scripts |
	nodes := UziParser parse: aString.
	scripts := nodes collect: [:node | self visit: node].
	self flag: #Richo. "I am not configuring the pins yet!"
	^ UziProgram inputs: #() scripts: scripts