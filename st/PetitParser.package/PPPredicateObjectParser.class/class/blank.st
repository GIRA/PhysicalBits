factory-chars
blank
	^ self chars: (String with: Character space with: Character tab) message: 'blank expected'