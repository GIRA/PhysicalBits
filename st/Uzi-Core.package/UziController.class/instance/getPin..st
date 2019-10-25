accessing-pins
getPin: pinName
	^ self pins 
		at: pinName
		ifAbsentPut: [UziPinWatcher
			name: pinName
			number: (self getPinNumber: pinName)
			idleTimeout: reportingIdleTimeout]