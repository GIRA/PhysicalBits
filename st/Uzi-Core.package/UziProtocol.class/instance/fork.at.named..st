fork
fork: aBlock at: priority named: aString
	| name |
	name := ('Uzi - {1} ({2})' format: { aString. self hash asString }).
	^ aBlock forkAt: priority named: name