factory-chars
newline
	^ self chars: (String with: Character cr with: Character lf) message: 'newline expected'