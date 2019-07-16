accessing-pins
getPin: pinNumber
	^ self pins 
		at: pinNumber 
		ifAbsentPut: [UziPinWatcher number: pinNumber]