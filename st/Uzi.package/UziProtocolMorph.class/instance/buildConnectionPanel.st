building
buildConnectionPanel
	^ self newColumn 
		addMorphBack: (self newRow
			addMorphBack: (self newLabel: 'Connected:');
			addMorphBack: (connectedText := self isConnected asString asMorph));
		addMorphBack: (self newRow
			addMorphBack: (self newLabel: 'Port:');
			addMorphBack: (portText := self portName asMorph))