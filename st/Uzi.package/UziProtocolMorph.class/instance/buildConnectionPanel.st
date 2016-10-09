building
buildConnectionPanel
	^ self newColumn 
		addMorphBack: (self newRow
			addMorphBack: 'Connected:' asMorph;
			addMorphBack: (connectedText := self isConnected asString asMorph));
		addMorphBack: (self newRow
			addMorphBack: 'Port:' asMorph;
			addMorphBack: (portText := self portName asMorph))