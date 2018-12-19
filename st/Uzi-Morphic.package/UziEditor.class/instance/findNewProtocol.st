accessing
findNewProtocol
	| existing |
	existing := UziControlPanelMorph allInstances select: [:each | each isInWorld].
	^ existing 
		ifEmpty: [nil] 
		ifNotEmpty: [existing 
			detect: [:each | each isConnected] 
			ifNone: [existing anyOne]]