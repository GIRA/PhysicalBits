instance creation
on: aCollectionOfUziInstructions
	| subclass |
	subclass := self subclassFor: aCollectionOfUziInstructions.
	^ subclass basicNew initializeWithInstructions: aCollectionOfUziInstructions