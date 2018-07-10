as yet unclassified
close
	socket ifNotNil: [:s | s closeAndDestroy].
	socket := nil