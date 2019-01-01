instance creation
on: aCollectionOfUziInstructions startingAt: startPC script: anUziDebuggerScript
	| subclass |
	subclass := self subclassFor: aCollectionOfUziInstructions.
	^ subclass basicNew
		initializeWithInstructions: aCollectionOfUziInstructions 
		start: startPC
		script: anUziDebuggerScript